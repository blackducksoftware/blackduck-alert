package com.synopsys.integration.alert.database.system;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "system_messages")
public class SystemMessages extends DatabaseEntity {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date created;
    @Column(name = "severity")
    private String severity;
    @Column(name = "message")
    private String message;

    public SystemMessages() {
    }

    public SystemMessages(final Date created, final String severity, final String message) {
        this.created = created;
        this.severity = severity;
        this.message = message;
    }

    public Date getCreated() {
        return created;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
