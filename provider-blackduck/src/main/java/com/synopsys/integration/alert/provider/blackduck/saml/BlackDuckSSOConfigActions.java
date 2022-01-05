/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.saml;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;

@Component
public class BlackDuckSSOConfigActions {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public BlackDuckSSOConfigActions(
        BlackDuckProviderKey blackDuckProviderKey,
        BlackDuckPropertiesFactory blackDuckPropertiesFactory,
        AuthorizationManager authorizationManager
    ) {
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.authorizationManager = authorizationManager;
    }

    public ActionResponse<BlackDuckSSOConfigResponseModel> retrieveBlackDuckSSOConfig(Long blackDuckConfigId) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, blackDuckProviderKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        BlackDuckSSOConfigRetriever ssoConfigRetriever;
        Optional<BlackDuckProperties> optionalBlackDuckProperties = blackDuckPropertiesFactory.createProperties(blackDuckConfigId);
        if (optionalBlackDuckProperties.isPresent()) {
            try {
                ssoConfigRetriever = BlackDuckSSOConfigRetriever.fromProperties(optionalBlackDuckProperties.get());
            } catch (AlertException e) {
                String errorMessage = String.format("Failed to initialize a Black Duck connection for configuration with id [%s]: %s", blackDuckConfigId, e.getMessage());
                logger.debug(errorMessage, e);
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, errorMessage);
            }

            try {
                BlackDuckSSOConfigView config = ssoConfigRetriever.retrieve();
                BlackDuckSSOConfigResponseModel configResponseModel = fromView(config);
                return new ActionResponse<>(HttpStatus.OK, configResponseModel);
            } catch (AlertException e) {
                String errorMessage = String.format("Failed to retrieve Black Duck SSO configuration: %s", e.getMessage());
                logger.debug(errorMessage, e);
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, errorMessage);
            }
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format("Failed to find Black Duck global configuration with id [%s]", blackDuckConfigId));
    }

    private BlackDuckSSOConfigResponseModel fromView(BlackDuckSSOConfigView view) {
        return new BlackDuckSSOConfigResponseModel(
            view.getSsoEnabled(),
            view.getSpEntityId(),
            view.getIdpMetadataUrl(),
            view.getIdpMetadataFileUploaded(),
            view.getGroupSynchronizationEnabled(),
            view.getLocalLogoutEnabled(),
            view.getSpExternalUrl(),
            view.getUserCreationEnabled(),
            view.getSamlMetadataUrl()
        );
    }

}
