package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class NotificationDetails {
    private final List<ProjectMessage> projectMessages;
    private final List<SimpleMessage> simpleMessages;

    public NotificationDetails(List<ProjectMessage> projectMessages, List<SimpleMessage> simpleMessages) {
        this.projectMessages = projectMessages;
        this.simpleMessages = simpleMessages;
    }

    public List<ProjectMessage> getProjectMessages() {
        return projectMessages;
    }

    public List<SimpleMessage> getSimpleMessages() {
        return simpleMessages;
    }

}
