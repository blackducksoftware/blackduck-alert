/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.system;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

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
