package com.synopsys.integration.alert.database.deprecated.channel.hipchat;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.common.annotation.SensitiveField;
import com.synopsys.integration.alert.database.deprecated.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.database.security.StringEncryptionConverter;

@Entity
@Table(schema = "alert", name = "global_hipchat_config")
public class HipChatGlobalConfigEntity extends GlobalChannelConfigEntity {

    // @EncryptedStringField
    @Column(name = "api_key")
    @SensitiveField
    @Convert(converter = StringEncryptionConverter.class)
    private String apiKey;

    @Column(name = "host_server")
    private String hostServer;

    public HipChatGlobalConfigEntity() {
    }

    public HipChatGlobalConfigEntity(final String apiKey, final String hostServer) {
        this.apiKey = apiKey;
        this.hostServer = hostServer;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getHostServer() {
        return hostServer;
    }

}