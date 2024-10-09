package com.blackduck.integration.alert.api.event;

public interface AlertEventHandler<T extends AlertEvent> {
    void handle(T event);

}
