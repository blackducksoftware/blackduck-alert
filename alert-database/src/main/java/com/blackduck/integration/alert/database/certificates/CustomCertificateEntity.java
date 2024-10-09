package com.blackduck.integration.alert.database.certificates;

import java.time.OffsetDateTime;

import com.blackduck.integration.alert.database.BaseEntity;
import com.blackduck.integration.alert.database.DatabaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "custom_certificates")
public class CustomCertificateEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "alias")
    private String alias;
    @Column(name = "certificate_content")
    private String certificateContent;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    public CustomCertificateEntity() {
    }

    public CustomCertificateEntity(String alias, String certificateContent, OffsetDateTime lastUpdated) {
        this.alias = alias;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public String getCertificateContent() {
        return certificateContent;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }
}
