package com.synopsys.integration.alert.database.job.blackduck.notification;

import java.io.Serializable;
import java.util.UUID;

public class BlackDuckJobNotificationTypePK implements Serializable {
    private UUID jobId;
    private String notificationType;

    public BlackDuckJobNotificationTypePK() {
    }

    public BlackDuckJobNotificationTypePK(UUID jobId, String notificationType) {
        this.jobId = jobId;
        this.notificationType = notificationType;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

}
