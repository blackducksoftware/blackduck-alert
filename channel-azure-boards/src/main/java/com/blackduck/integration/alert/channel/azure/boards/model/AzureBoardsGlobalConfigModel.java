/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.common.model.Obfuscated;
import com.blackduck.integration.alert.common.rest.model.ConfigWithMetadata;

public class AzureBoardsGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<AzureBoardsGlobalConfigModel> {
    private String organizationName;
    private String appId;
    private Boolean isAppIdSet;
    private String clientSecret;
    private Boolean isClientSecretSet;

    public AzureBoardsGlobalConfigModel() {
        // For serialization
    }

    public AzureBoardsGlobalConfigModel(String id, String name, String organizationName, String appId, String clientSecret) {
        super(id, name);
        this.organizationName = organizationName;
        this.appId = appId;
        this.clientSecret = clientSecret;
    }

    public AzureBoardsGlobalConfigModel(
        String id,
        String name,
        String createdAt,
        String lastUpdated,
        String organizationName,
        String appId,
        Boolean isAppIdSet,
        String clientSecret,
        Boolean isClientSecretSet
    ) {
        this(id, name, organizationName, appId, clientSecret);
        this.isAppIdSet = isAppIdSet;
        this.isClientSecretSet = isClientSecretSet;
        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    @Override
    public AzureBoardsGlobalConfigModel obfuscate() {
        return new AzureBoardsGlobalConfigModel(
            getId(),
            getName(),
            getCreatedAt(),
            getLastUpdated(),
            organizationName,
            null,
            StringUtils.isNotBlank(appId),
            null,
            StringUtils.isNotBlank(clientSecret)
        );
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public Optional<Boolean> getIsAppIdSet() {
        return Optional.ofNullable(isAppIdSet);
    }

    public void setIsAppIdSet(Boolean appIdSet) {
        isAppIdSet = appIdSet;
    }

    public Optional<String> getAppId() {
        return Optional.ofNullable(appId);
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Optional<Boolean> getIsClientSecretSet() {
        return Optional.ofNullable(isClientSecretSet);
    }

    public void setIsClientSecretSet(Boolean clientSecretSet) {
        isClientSecretSet = clientSecretSet;
    }

    public Optional<String> getClientSecret() {
        return Optional.ofNullable(clientSecret);
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
