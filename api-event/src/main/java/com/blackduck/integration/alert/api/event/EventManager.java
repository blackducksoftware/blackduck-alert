package com.blackduck.integration.alert.api.event;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class EventManager {
    private final Gson gson;
    private final RabbitTemplate rabbitTemplate;
    private final TaskExecutor taskExecutor;

    @Autowired
    public EventManager(Gson gson, RabbitTemplate rabbitTemplate, TaskExecutor taskExecutor) {
        this.gson = gson;
        this.rabbitTemplate = rabbitTemplate;
        this.taskExecutor = taskExecutor;
    }

    public void sendEvents(List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    public void sendEvent(AlertEvent event) {
        taskExecutor.execute(() -> {
            String destination = event.getDestination();
            String jsonMessage = toJsonOrNull(event);
            rabbitTemplate.convertAndSend(destination, jsonMessage);
        });
    }

    private String toJsonOrNull(Object content) {
        if (content != null) {
            return gson.toJson(content);
        }
        return null;
    }

}
