package com.blackducksoftware.integration.hub.notification.datasource.entity.event;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "notification_events")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String eventKey;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    private String notificationType;
    private String projectName;
    private String projectVersion;
    private String componentName;
    private String componentVersion;
    private String policyRuleName;

    public NotificationEntity() {
    }

    public NotificationEntity(final String eventKey, final Date createdAt, final String notificationType, final String projectName, final String projectVersion, final String componentName, final String componentVersion,
            final String policyRuleName) {
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationType = notificationType;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.policyRuleName = policyRuleName;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getPolicyRuleName() {
        return policyRuleName;
    }
}
