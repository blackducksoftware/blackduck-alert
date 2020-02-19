package com.synopsys.integration.alert.common.message.model;

import java.util.Date;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;

public class CommonMessageData {
    private final Long notificationId;
    private final Long providerConfigId;
    private final String providerName;
    private final String providerURL;
    private final Date providerCreationDate;
    private final ConfigurationJobModel job;

    public CommonMessageData(Long notificationId, Long providerConfigId, String providerName, String providerURL, Date providerCreationDate, ConfigurationJobModel job) {
        this.notificationId = notificationId;
        this.providerConfigId = providerConfigId;
        this.providerName = providerName;
        this.providerURL = providerURL;
        this.providerCreationDate = providerCreationDate;
        this.job = job;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderURL() {
        return providerURL;
    }

    public Date getProviderCreationDate() {
        return providerCreationDate;
    }

    public ConfigurationJobModel getJob() {
        return job;
    }
}
