/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.SlackConfigRestModel;

@Component
public class SlackConfigActions extends ConfigActions<SlackConfigEntity, SlackConfigRestModel> {
    final SlackChannel slackChannel;

    @Autowired
    public SlackConfigActions(final SlackChannel slackChannel, final SlackRepository slackRepository, final ObjectTransformer objectTransformer) {
        super(SlackConfigEntity.class, SlackConfigRestModel.class, slackRepository, objectTransformer);
        this.slackChannel = slackChannel;
    }

    @Override
    public String validateConfig(final SlackConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isEmpty(restModel.getWebhook())) {
            fieldErrors.put("webhook", "Can't be blank");
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }

        return "Valid";
    }

    @Override
    public String channelTestConfig(final SlackConfigRestModel restModel) throws IntegrationException {
        return slackChannel.testMessage(objectTransformer.configRestModelToDatabaseEntity(restModel, SlackConfigEntity.class));
    }

    @Override
    public List<String> sensitiveFields() {
        return Collections.emptyList();
    }

}
