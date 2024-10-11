/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.system;

import java.time.OffsetDateTime;

import com.blackduck.integration.alert.database.BaseEntity;
import com.blackduck.integration.alert.database.DatabaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "system_messages")
public class SystemMessageEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.system_messages_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.system_messages_id_seq_generator", sequenceName = "alert.system_messages_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "created_at")
    private OffsetDateTime created;
    @Column(name = "severity")
    private String severity;
    @Column(name = "content")
    private String content;
    @Column(name = "type")
    private String type;

    public SystemMessageEntity() {
    }

    public SystemMessageEntity(OffsetDateTime created, String severity, String content, String type) {
        this.created = created;
        this.severity = severity;
        this.content = content;
        this.type = type;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public String getSeverity() {
        return severity;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

}
