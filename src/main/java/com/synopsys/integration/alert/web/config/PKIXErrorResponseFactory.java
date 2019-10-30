/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.ResponseFactory;

@Component
public class PKIXErrorResponseFactory {
    private final Logger logger = LoggerFactory.getLogger(PKIXErrorResponseFactory.class);
    private final String BLACKDUCK_GITHUB_DEPLOYMENT_URL = "https://github.com/blackducksoftware/blackduck-alert/blob/master/deployment";
    private final String ALERT_DEPLOYMENT_DOCKER_SWARM = "docker-swarm";
    private final String ALERT_DEPLOYMENT_DOCKER_COMPOSE = "docker-compose";
    private final String ALERT_DEPLOYMENT_KUBERNETES = "kubernetes";

    private ResponseFactory responseFactory;

    @Autowired
    public PKIXErrorResponseFactory(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    public Optional<ResponseEntity<String>> createSSLExceptionResponse(String id, Exception e) {
        if (e.getMessage().toUpperCase().contains("PKIX")) {
            logger.debug("Found an error regarding PKIX, creating a unique response...");
            String pkixErrorMessage = createMessage();
            return Optional.of(responseFactory.createBadRequestResponse(id, pkixErrorMessage));
        }

        return Optional.empty();
    }

    private String createMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("<div>");
        messageBuilder.append("We detected an issue with your certificates, please properly import your certificate before deploying Alert.");
        messageBuilder.append("<br>");
        messageBuilder.append("To resolve this issue, redeploy Alert and use the appropriate link below to properly install your certificates:");
        messageBuilder.append("<br>");
        messageBuilder.append("- Docker Swarm ");
        messageBuilder.append(createLinkToGithub(ALERT_DEPLOYMENT_DOCKER_SWARM));
        messageBuilder.append("<br>");
        messageBuilder.append("- Docker Compose ");
        messageBuilder.append(createLinkToGithub(ALERT_DEPLOYMENT_DOCKER_COMPOSE));
        messageBuilder.append("<br>");
        messageBuilder.append("- Kubernetes ");
        messageBuilder.append(createLinkToGithub(ALERT_DEPLOYMENT_KUBERNETES));
        messageBuilder.append("</div>");

        return messageBuilder.toString();
    }

    private String createLinkToGithub(String deploymentType) {
        String fullUrl = String.format("%s/%s/README.md#certificates", BLACKDUCK_GITHUB_DEPLOYMENT_URL, deploymentType);
        return String.format("<a href=\"%s\">%s Deployment</a>", fullUrl, deploymentType);
    }
}
