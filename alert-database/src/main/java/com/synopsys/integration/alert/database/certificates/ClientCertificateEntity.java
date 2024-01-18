package com.synopsys.integration.alert.database.certificates;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

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

    @Id
    @Column(name = "private_key_id")
    private UUID privateKeyId;

    @Column(name = "certificate_content")
    private String certificateContent;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    public ClientCertificateEntity() {
    }

    public ClientCertificateEntity(UUID id, String alias, UUID privateKeyId, String certificateContent, OffsetDateTime lastUpdated) {
        this.id = id;
        this.privateKeyId = privateKeyId;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
        this.alias = alias;
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
}
