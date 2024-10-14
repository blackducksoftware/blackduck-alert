/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.certificates;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "client_certificates")
public class ClientCertificateEntity extends BaseEntity {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "alias")
    private String alias;
    @Column(name = "private_key_id")
    private UUID privateKeyId;
    @Column(name = "certificate_content")
    private String certificateContent;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    public ClientCertificateEntity() {
    }

    public ClientCertificateEntity(UUID id, UUID privateKeyId, String certificateContent, OffsetDateTime lastUpdated) {
        this.id = id;
        this.privateKeyId = privateKeyId;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;

        this.alias = AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS;
    }

    public UUID getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public UUID getPrivateKeyId() {
        return privateKeyId;
    }

    public String getCertificateContent() {
        return certificateContent;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
