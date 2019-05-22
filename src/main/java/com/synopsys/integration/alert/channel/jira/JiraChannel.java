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
package com.synopsys.integration.alert.channel.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.exception.IntegrationException;

@Component(value = JiraChannel.COMPONENT_NAME)
public class JiraChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_jira";
    private static final Logger logger = LoggerFactory.getLogger(JiraChannel.class);

    public JiraChannel(final Gson gson, final AlertProperties alertProperties, final AuditUtility auditUtility) {
        super(gson, alertProperties, auditUtility);
    }

    @Override
    public void sendMessage(final DistributionEvent event) throws IntegrationException {
        logger.info("Received event to send to Jira {}", event);
    }

    @Override
    public String getDestinationName() {
        return COMPONENT_NAME;
    }
}
