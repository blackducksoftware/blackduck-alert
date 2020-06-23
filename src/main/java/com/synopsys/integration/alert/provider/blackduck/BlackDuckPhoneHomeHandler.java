/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.UniquePhoneHomeProduct;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.util.NameVersion;

@Component
public class BlackDuckPhoneHomeHandler implements ProviderPhoneHomeHandler {
    private static final String UNKNOWN_ID = "<unknown>";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckPhoneHomeHandler.class);

    private BlackDuckProvider provider;
    private DescriptorAccessor descriptorAccessor;

    @Autowired
    public BlackDuckPhoneHomeHandler(BlackDuckProvider provider, DescriptorAccessor descriptorAccessor) {
        this.provider = provider;
        this.descriptorAccessor = descriptorAccessor;
    }

    @Override
    public ProviderKey getProviderKey() {
        return provider.getKey();
    }

    @Override
    public PhoneHomeRequestBodyBuilder populatePhoneHomeData(ConfigurationModel configurationModel, NameVersion alertArtifactInfo) {
        String registrationId = null;
        String blackDuckUrl = UNKNOWN_ID;
        String blackDuckVersion = UNKNOWN_ID;
        try {
            descriptorAccessor.getRegisteredDescriptorById(configurationModel.getDescriptorId());
            StatefulProvider statefulProvider = provider.createStatefulProvider(configurationModel);
            BlackDuckProperties blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();

            Optional<BlackDuckHttpClient> blackDuckHttpClientOptional = blackDuckProperties.createBlackDuckHttpClient(logger);
            if (blackDuckHttpClientOptional.isPresent()) {
                BlackDuckHttpClient blackDuckHttpClient = blackDuckHttpClientOptional.get();
                BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
                BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
                BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
                CurrentVersionView currentVersionView = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
                blackDuckVersion = currentVersionView.getVersion();
                registrationId = blackDuckRegistrationService.getRegistrationId();
                blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(UNKNOWN_ID);
            }
        } catch (IntegrationException ignored) {
            // ignoring this exception
        }

        // We must check if the reg id is blank because of an edge case in which Black Duck can authenticate (while the webserver is coming up) without registration
        if (StringUtils.isBlank(registrationId)) {
            registrationId = UNKNOWN_ID;
        }
        PhoneHomeRequestBodyBuilder phoneHomeBuilder = new PhoneHomeRequestBodyBuilder(registrationId, blackDuckUrl, alertArtifactInfo, UniquePhoneHomeProduct.BLACK_DUCK, blackDuckVersion);
        return phoneHomeBuilder;
    }

}
