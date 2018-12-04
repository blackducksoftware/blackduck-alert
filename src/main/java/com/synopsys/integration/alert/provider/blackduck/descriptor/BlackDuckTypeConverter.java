/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckConfig;

@Component
public class BlackDuckTypeConverter extends TypeConverter {
    private final BlackDuckProperties blackDuckProperties;
    private final AlertProperties alertProperties;

    @Autowired
    public BlackDuckTypeConverter(final ContentConverter contentConverter, final BlackDuckProperties blackDuckProperties, final AlertProperties alertProperties) {
        super(contentConverter);
        this.blackDuckProperties = blackDuckProperties;
        this.alertProperties = alertProperties;
    }

    @Override
    public Config getConfigFromJson(final String json) {
        return getContentConverter().getJsonContent(json, BlackDuckConfig.class);
    }

    @Override
    public DatabaseEntity populateEntityFromConfig(final Config restModel) {
        final BlackDuckConfig blackDuckConfig = (BlackDuckConfig) restModel;
        final Integer blackDuckTimeout = getContentConverter().getIntegerValue(blackDuckConfig.getBlackDuckTimeout());
        final GlobalBlackDuckConfigEntity blackDuckEntity = new GlobalBlackDuckConfigEntity(blackDuckTimeout, blackDuckConfig.getBlackDuckApiKey(), blackDuckConfig.getBlackDuckUrl());
        addIdToEntityPK(blackDuckConfig.getId(), blackDuckEntity);
        return blackDuckEntity;
    }

    @Override
    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        final GlobalBlackDuckConfigEntity blackDuckEntity = (GlobalBlackDuckConfigEntity) entity;
        final BlackDuckConfig blackDuckConfig = new BlackDuckConfig();
        final String id = getContentConverter().getStringValue(blackDuckEntity.getId());
        final String blackDuckTimeout = getContentConverter().getStringValue(blackDuckEntity.getBlackDuckTimeout());
        blackDuckConfig.setId(id);
        blackDuckConfig.setBlackDuckTimeout(blackDuckTimeout);
        blackDuckConfig.setBlackDuckApiKeyIsSet(StringUtils.isNotBlank(blackDuckEntity.getBlackDuckApiKey()));
        blackDuckConfig.setBlackDuckApiKey(blackDuckEntity.getBlackDuckApiKey());
        return updateModelFromProperties(blackDuckConfig);
    }

    private BlackDuckConfig updateModelFromProperties(final BlackDuckConfig config) {
        config.setBlackDuckUrl(blackDuckProperties.getBlackDuckUrl().orElse(null));
        final Boolean trustCertificate = alertProperties.getAlertTrustCertificate().orElse(null);
        if (null != trustCertificate) {
            config.setBlackDuckAlwaysTrustCertificate(String.valueOf(trustCertificate));
        }
        config.setBlackDuckProxyHost(alertProperties.getAlertProxyHost().orElse(null));
        config.setBlackDuckProxyPort(alertProperties.getAlertProxyPort().orElse(null));
        config.setBlackDuckProxyUsername(alertProperties.getAlertProxyUsername().orElse(null));
        // Do not send passwords going to the UI
        final boolean proxyPasswordIsSet = StringUtils.isNotBlank(alertProperties.getAlertProxyPassword().orElse(null));
        config.setBlackDuckProxyPasswordIsSet(proxyPasswordIsSet);
        return config;
    }

}
