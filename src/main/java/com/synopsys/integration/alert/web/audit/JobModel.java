package com.synopsys.integration.alert.web.audit;

import com.synopsys.integration.alert.web.model.Config;

public class JobModel extends Config {

    private String name;
    private String eventType;
    private String timeCreated;
    private String timeLastSent;
    private String status;
    private String errorMessage;
    private String errorStackTrace;

    public JobModel() {
    }

    public JobModel(final String id, final String name, final String eventType, final String timeCreated, final String timeLastSent, final String status, final String errorMessage, final String errorStackTrace) {
        super(id);
        this.name = name;
        this.eventType = eventType;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

}
