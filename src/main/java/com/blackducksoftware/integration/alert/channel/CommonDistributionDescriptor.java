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
package com.blackducksoftware.integration.alert.channel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.MessageListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class CommonDistributionDescriptor extends ChannelDescriptor {
    public static final String COMPONENT_NAME = "common";

    @Autowired
    public CommonDistributionDescriptor(final CommonDistributionContentConverter contentConverter, final CommonDistributionRepositoryAccessor repositoryAccessor) {
        super(COMPONENT_NAME, COMPONENT_NAME, null, null, contentConverter, repositoryAccessor);
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) {
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfigRestModel restModel, final ChannelEvent event) throws IntegrationException {
    }

    @Override
    public MessageListener getChannelListener() {
        return null;
    }

    @Override
    public Set<AlertStartupProperty> getGlobalEntityPropertyMapping() {
        return new HashSet<>();
    }

    @Override
    public ConfigRestModel getGlobalRestModelObject() {
        return null;
    }

    @Override
    public void validateGlobalConfig(final ConfigRestModel restModel, final Map<String, String> fieldErrors) {
    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) throws IntegrationException {
    }

}
