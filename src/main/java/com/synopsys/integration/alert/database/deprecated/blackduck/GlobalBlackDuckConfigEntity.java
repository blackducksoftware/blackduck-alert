package com.synopsys.integration.alert.database.deprecated.blackduck;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.common.annotation.SensitiveField;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.security.StringEncryptionConverter;

@Entity
@Table(schema = "alert", name = "global_blackduck_config")
public class GlobalBlackDuckConfigEntity extends DatabaseEntity {
    @Column(name = "blackduck_timeout")
    private Integer blackDuckTimeout;

    // @EncryptedStringField
    @Column(name = "blackduck_api_key")
    @SensitiveField
    @Convert(converter = StringEncryptionConverter.class)
    private String blackDuckApiKey;
    @Column(name = "blackduck_url")
    private String blackDuckUrl;

    public GlobalBlackDuckConfigEntity() {
        // JPA requires default constructor definitions
    }

    public GlobalBlackDuckConfigEntity(final Integer blackDuckTimeout, final String blackDuckApiKey, final String blackDuckUrl) {
        this.blackDuckTimeout = blackDuckTimeout;
        this.blackDuckApiKey = blackDuckApiKey;
        this.blackDuckUrl = blackDuckUrl;
    }

    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public String getBlackDuckApiKey() {
        return blackDuckApiKey;
    }

    public String getBlackDuckUrl() {
        return blackDuckUrl;
    }
}