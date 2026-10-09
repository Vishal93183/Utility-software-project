package com.utility.software.mqtt;

import com.utility.software.exception.DeviceCommunicationException;
import com.utility.software.exception.DuplicateResourceException;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Request / reply over MQTT.
 *
 * 1. subscribe to the reply topic (the "Message ID" from the Request Builder)
 * 2. publish the request JSON on mqtt.request-topic
 * 3. wait for one message on the reply topic (or time out)
 *
 * Only created when mqtt.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttGateway implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttGateway.class);

    private final MqttClient client;
    private final MqttConnectOptions options;
    private final String requestTopic;
    private final int qos;
    private final long timeoutMs;

    /** reply topic -> future that is completed when the device answers */
    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public MqttGateway(
            @Value("${mqtt.broker-url}") String brokerUrl,
            @Value("${mqtt.client-id}") String clientId,
            @Value("${mqtt.username:}") String username,
            @Value("${mqtt.password:}") String password,
            @Value("${mqtt.request-topic}") String requestTopic,
            @Value("${mqtt.qos:1}") int qos,
            @Value("${mqtt.response-timeout-ms:5000}") long timeoutMs) throws MqttException {

        this.requestTopic = requestTopic;
        this.qos = qos;
        this.timeoutMs = timeoutMs;

        this.client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        this.client.setCallback(this);

        this.options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(5);
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        try {
            client.connect(options);
        } catch (MqttException e) {
            // do not stop the application; we retry on the first request
            log.warn("MQTT broker not reachable at {} ({}). Will retry on the next request.",
                    brokerUrl, e.getMessage());
        }
    }

    /**
     * Sends payload to the request topic and waits for the device's reply.
     *
     * @return the reply text, or empty if the device did not answer in time
     */
    public Optional<String> request(String replyTopic, String payload) {
        validateTopic(replyTopic);

        CompletableFuture<String> future = new CompletableFuture<>();
        if (pending.putIfAbsent(replyTopic, future) != null) {
            throw new DuplicateResourceException(
                    "A request for Message ID '" + replyTopic + "' is already waiting for a reply");
        }

        try {
            ensureConnected();
            client.subscribe(replyTopic, qos);

            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);
            client.publish(requestTopic, message);

            return Optional.of(future.get(timeoutMs, TimeUnit.MILLISECONDS));

        } catch (TimeoutException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeviceCommunicationException("Interrupted while waiting for the device");
        } catch (ExecutionException e) {
            throw new DeviceCommunicationException("Failed while waiting for the device: " + e.getMessage());
        } catch (MqttException e) {
            throw new DeviceCommunicationException("MQTT error: " + e.getMessage());
        } finally {
            pending.remove(replyTopic);
            try {
                if (client.isConnected()) {
                    client.unsubscribe(replyTopic);
                }
            } catch (MqttException ignored) {
                // nothing useful to do
            }
        }
    }

    private synchronized void ensureConnected() throws MqttException {
        if (!client.isConnected()) {
            client.connect(options);
        }
    }

    private void validateTopic(String topic) {
        if (topic == null || topic.isBlank() || topic.contains("+") || topic.contains("#")) {
            throw new IllegalArgumentException(
                    "Message ID must be a plain MQTT topic (no + or # wildcards), e.g. device/naineet/res");
        }
    }

    // ---------------- MQTT callbacks ----------------

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if (message.isRetained()) {
            return; // an old stored message, not the answer to our request
        }
        CompletableFuture<String> future = pending.get(topic);
        if (future != null) {
            future.complete(new String(message.getPayload(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT connected to {} (reconnect={})", serverURI, reconnect);
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause == null ? "unknown" : cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not needed
    }

    @PreDestroy
    void shutdown() {
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException e) {
            log.debug("MQTT shutdown: {}", e.getMessage());
        }
    }
}
