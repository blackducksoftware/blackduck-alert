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
package com.synopsys.integration.alert.provider.blackduck.validators;

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
