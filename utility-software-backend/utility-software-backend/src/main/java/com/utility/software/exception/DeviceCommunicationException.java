package com.utility.software.exception;

/** Broker unreachable, publish failed, or the device sent a reply that cannot be read. */
public class DeviceCommunicationException extends RuntimeException {
    public DeviceCommunicationException(String message) {
        super(message);
    }
}
