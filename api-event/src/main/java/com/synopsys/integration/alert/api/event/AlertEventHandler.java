package com.synopsys.integration.alert.api.event;

public interface AlertEventHandler<T extends AlertEvent> {
    void handle(T event);

}
