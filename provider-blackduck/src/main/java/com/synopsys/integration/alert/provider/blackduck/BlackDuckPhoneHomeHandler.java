/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.alert.api.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerData;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.UniquePhoneHomeProduct;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.util.NameVersion;

@Component
public class BlackDuckPhoneHomeHandler implements ProviderPhoneHomeHandler {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckPhoneHomeHandler.class);

    private final BlackDuckProvider provider;
    private final DescriptorAccessor descriptorAccessor;

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
        String blackDuckUrl = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        String blackDuckVersion = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        try {
            descriptorAccessor.getRegisteredDescriptorById(configurationModel.getDescriptorId());
            StatefulProvider statefulProvider = provider.createStatefulProvider(configurationModel);
            BlackDuckProperties blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();

            BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(logger);
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
            BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
            BlackDuckServerData blackDuckServerData = blackDuckRegistrationService.getBlackDuckServerData();
            blackDuckVersion = blackDuckServerData.getVersion();
            registrationId = blackDuckServerData.getRegistrationKey().orElse(null);
            blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE);
        } catch (IntegrationException ignored) {
            // ignoring this exception
        }

        // We must check if the reg id is blank because of an edge case in which Black Duck can authenticate (while the webserver is coming up) without registration
        if (StringUtils.isBlank(registrationId)) {
            registrationId = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        }
        PhoneHomeRequestBodyBuilder phoneHomeBuilder = new PhoneHomeRequestBodyBuilder(registrationId, blackDuckUrl, alertArtifactInfo, UniquePhoneHomeProduct.BLACK_DUCK, blackDuckVersion);
        return phoneHomeBuilder;
    }

}
