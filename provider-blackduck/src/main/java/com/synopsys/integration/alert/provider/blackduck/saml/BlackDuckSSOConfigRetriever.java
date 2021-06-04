/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

public class BlackDuckSSOConfigRetriever {
    private static final String SSO_CONFIGURATION_MIME_TYPE = "application/vnd.blackducksoftware.admin-4+json";
    private static final BlackDuckPath<BlackDuckSSOConfig> SSO_CONFIGURATION_PATH = BlackDuckPath.single("/api/sso/configuration", BlackDuckSSOConfig.class);

    private final Logger logger = LoggerFactory.getLogger(BlackDuckSSOConfigRetriever.class);
    private final ApiDiscovery apiDiscovery;
    private final BlackDuckApiClient blackDuckApiClient;

    public static BlackDuckSSOConfigRetriever fromProperties(BlackDuckProperties blackDuckProperties) throws AlertException {
        Logger logger = LoggerFactory.getLogger(BlackDuckSSOConfigRetriever.class);
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(intLogger);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
        return new BlackDuckSSOConfigRetriever(blackDuckServicesFactory.getApiDiscovery(), blackDuckServicesFactory.getBlackDuckApiClient());
    }

    public BlackDuckSSOConfigRetriever(ApiDiscovery apiDiscovery, BlackDuckApiClient blackDuckApiClient) {
        this.apiDiscovery = apiDiscovery;
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public BlackDuckSSOConfig retrieve() throws AlertException {
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder().acceptMimeType(SSO_CONFIGURATION_MIME_TYPE);
        UrlSingleResponse<BlackDuckSSOConfig> ssoConfigurationSingleResponse = apiDiscovery.metaSingleResponse(SSO_CONFIGURATION_PATH);
        BlackDuckRequest<BlackDuckSSOConfig, UrlSingleResponse<BlackDuckSSOConfig>> ssoConfigurationRequest = new BlackDuckRequest<>(requestBuilder, ssoConfigurationSingleResponse);
        try {
            return blackDuckApiClient.getResponse(ssoConfigurationRequest);
        } catch (IntegrationException e) {
            String errorMessage = "Unable to retrieve SSO configuration from BlackDuck";
            logger.debug(errorMessage, e);
            throw new AlertException(errorMessage, e);
        }
    }

}
