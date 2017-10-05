package com.blackducksoftware.integration.hub.notification.datasource.entity.event;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "notification_events", schema = "notification")
public class NotificationEntity {

    @Id
    @GeneratedValue
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

    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Column(name = "event_key")
    public String getEventKey() {
        return eventKey;
    }

    @Column(name = "created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Column(name = "notification_type")
    public String getNotificationType() {
        return notificationType;
    }

    @Column(name = "project_name")
    public String getProjectName() {
        return projectName;
    }

    @Column(name = "project_version")
    public String getProjectVersion() {
        return projectVersion;
    }

    @Column(name = "component_name")
    public String getComponentName() {
        return componentName;
    }

    @Column(name = "component_version")
    public String getComponentVersion() {
        return componentVersion;
    }

    @Column(name = "policy_rule_name")
    public String getPolicyRuleName() {
        return policyRuleName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
        result = prime * result + ((componentVersion == null) ? 0 : componentVersion.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((eventKey == null) ? 0 : eventKey.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
        result = prime * result + ((policyRuleName == null) ? 0 : policyRuleName.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((projectVersion == null) ? 0 : projectVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NotificationEntity other = (NotificationEntity) obj;
        if (componentName == null) {
            if (other.componentName != null) {
                return false;
            }
        } else if (!componentName.equals(other.componentName)) {
            return false;
        }
        if (componentVersion == null) {
            if (other.componentVersion != null) {
                return false;
            }
        } else if (!componentVersion.equals(other.componentVersion)) {
            return false;
        }
        if (createdAt == null) {
            if (other.createdAt != null) {
                return false;
            }
        } else if (!createdAt.equals(other.createdAt)) {
            return false;
        }
        if (eventKey == null) {
            if (other.eventKey != null) {
                return false;
            }
        } else if (!eventKey.equals(other.eventKey)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (notificationType == null) {
            if (other.notificationType != null) {
                return false;
            }
        } else if (!notificationType.equals(other.notificationType)) {
            return false;
        }
        if (policyRuleName == null) {
            if (other.policyRuleName != null) {
                return false;
            }
        } else if (!policyRuleName.equals(other.policyRuleName)) {
            return false;
        }
        if (projectName == null) {
            if (other.projectName != null) {
                return false;
            }
        } else if (!projectName.equals(other.projectName)) {
            return false;
        }
        if (projectVersion == null) {
            if (other.projectVersion != null) {
                return false;
            }
        } else if (!projectVersion.equals(other.projectVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotificationEntity [id=" + id + ", eventKey=" + eventKey + ", createdAt=" + createdAt + ", notificationType=" + notificationType + ", projectName=" + projectName + ", projectVersion=" + projectVersion + ", componentName="
                + componentName + ", componentVersion=" + componentVersion + ", policyRuleName=" + policyRuleName + "]";
    }
}
