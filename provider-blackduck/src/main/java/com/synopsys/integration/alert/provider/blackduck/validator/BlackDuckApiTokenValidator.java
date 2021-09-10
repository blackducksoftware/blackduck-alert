/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckApiTokenValidator {
    private final BlackDuckProperties blackDuckProperties;
    private final Logger logger = LoggerFactory.getLogger(BlackDuckApiTokenValidator.class);

    public BlackDuckApiTokenValidator(BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
    }

    public boolean isApiTokenValid() {
        return blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger)
                   .map(this::isNotificationApiAllowed)
                   .orElse(false);
    }

    private boolean isNotificationApiAllowed(BlackDuckHttpClient httpClient) {
        boolean valid = true;
        try {
            //Verifies if the user has access to /api/notifications endpoint. Since only the the ability to access the endpoint is tested returning actual notifications is not needed.
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(httpClient, new Slf4jIntLogger(logger));
            NotificationService notificationService = blackDuckServicesFactory.createNotificationService();
            notificationService.getLatestNotificationDate();
        } catch (BlackDuckApiException ex) {
            if (RestConstants.FORBIDDEN_403 == ex.getOriginalIntegrationRestException().getHttpStatusCode()) {
                valid = false;
            }
            logger.error("Error reading notifications", ex);
        } catch (IntegrationException ex) {
            logger.error("Error reading notifications", ex);
            valid = false;
        }
        return valid;
    }

}
