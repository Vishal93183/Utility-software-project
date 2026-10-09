"""
Tiny MQTT device simulator for testing the backend (optional).

  pip install paho-mqtt
  python tools/mqtt_simulator.py

It listens on the request topic, and answers on the topic given in the
request's "messageId" (the same value as the Message ID in the Request Builder).
"""
import json
import random
import paho.mqtt.client as mqtt

BROKER = "localhost"
PORT = 1883
REQUEST_TOPIC = "device/naineet/req"   # must match mqtt.request-topic in application.properties

# Pretend these meters exist in the device. READ answers with them, WRITE changes them.
meters = {}


def reading_for(meter_id):
    m = meters.setdefault(meter_id, {"reading": round(random.uniform(500, 2000), 2), "meterTypeId": 14})
    voltage = round(random.uniform(225, 235), 1)
    current = round(random.uniform(4, 7), 1)
    return {
        "meterId": meter_id,
        "meterTypeId": m["meterTypeId"],
        "data": {
            "reading": m["reading"],
            "voltage": voltage,
            "current": current,
            "power": round(voltage * current, 1),
        },
    }


def on_message(client, userdata, msg):
    req = json.loads(msg.payload.decode())
    meter_id = req["meterId"]
    print("request :", req)

    if req["opType"] == "WRITE":
        m = meters.setdefault(meter_id, {"reading": 0.0, "meterTypeId": 14})
        if req.get("meterTypeId") is not None:
            m["meterTypeId"] = req["meterTypeId"]
        if (req.get("data") or {}).get("reading") is not None:
            m["reading"] = req["data"]["reading"]

    item = reading_for(meter_id)
    if req["opType"] == "WRITE":
        for key in ("voltage", "current", "power", "reading"):
            if (req.get("data") or {}).get(key) is not None:
                item["data"][key] = req["data"][key]

    reply = {
        "messageId": req["messageId"],
        "opType": req["opType"],
        "ipAddress": req["ipAddress"],
        "ipStatus": True,
        "meters": [item],
    }
    client.publish(req["messageId"], json.dumps(reply))
    print("reply   :", req["messageId"])


try:
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)   # paho-mqtt 2.x
except AttributeError:
    client = mqtt.Client()                                   # paho-mqtt 1.x

client.on_message = on_message
client.connect(BROKER, PORT)
client.subscribe(REQUEST_TOPIC)
print(f"Simulator listening on {REQUEST_TOPIC} ...")
client.loop_forever()
