package com.synopsys.integration.alert.database.certificates;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "client_certificate_keys")
public class ClientCertificateKeyEntity extends BaseEntity {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "password")
    private String password;
    @Column(name = "key_content")
    private String keyContent;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    public ClientCertificateKeyEntity() {
    }

    public ClientCertificateKeyEntity(UUID id, String password, String keyContent, OffsetDateTime lastUpdated) {
        this.id = id;
        this.password = password;
        this.keyContent = keyContent;
        this.lastUpdated = lastUpdated;

        this.name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getKeyContent() {
        return keyContent;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLastUpdated(final OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
