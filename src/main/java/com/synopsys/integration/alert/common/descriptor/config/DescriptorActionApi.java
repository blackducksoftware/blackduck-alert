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
package com.synopsys.integration.alert.common.descriptor.config;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class DescriptorActionApi {
    private final TypeConverter typeConverter;
    private final RepositoryAccessor repositoryAccessor;
    private final StartupComponent startupComponent;

    public DescriptorActionApi(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor) {
        this(typeConverter, repositoryAccessor, null);
    }

    public DescriptorActionApi(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor, final StartupComponent startupComponent) {
        this.typeConverter = typeConverter;
        this.repositoryAccessor = repositoryAccessor;
        this.startupComponent = startupComponent;
    }

    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    public RepositoryAccessor getRepositoryAccessor() {
        return repositoryAccessor;
    }

    public StartupComponent getStartupComponent() {
        return startupComponent;
    }

    public boolean hasStartupProperties() {
        return getStartupComponent() != null;
    }

    public abstract void validateConfig(final Config restModel, final Map<String, String> fieldErrors);

    public TestConfigModel createTestConfigModel(final Map<String, ConfigurationFieldModel> config, final String destination) throws AlertFieldException {
        return new TestConfigModel(config, destination);
    }

    public abstract void testConfig(final TestConfigModel testConfig) throws IntegrationException;

    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return getRepositoryAccessor().readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities() {
        return getRepositoryAccessor().readEntities();
    }

    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        return getRepositoryAccessor().saveEntity(entity);
    }

    public void deleteEntity(final long id) {
        getRepositoryAccessor().deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final Config config) {
        return getTypeConverter().populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        return getTypeConverter().populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final String json) {
        return getTypeConverter().getConfigFromJson(json);
    }

    public DistributionEvent createChannelEvent(final CommonDistributionConfiguration commmonDistributionConfig, final AggregateMessageContent messageContent) {
        final FieldAccessor fieldAccessor = new FieldAccessor(commmonDistributionConfig.getConfigurationFieldModelMap());
        return new DistributionEvent(commmonDistributionConfig.getChannelName(), RestConstants.formatDate(new Date()), commmonDistributionConfig.getProviderName(), commmonDistributionConfig.getFormatType().name(), messageContent,
            fieldAccessor);
    }

    public DistributionEvent createChannelTestEvent(final CommonDistributionConfiguration commmonDistributionConfig) {
        final AggregateMessageContent messageContent = createTestNotificationContent();
        final FieldAccessor fieldAccessor = new FieldAccessor(commmonDistributionConfig.getConfigurationFieldModelMap());
        return new DistributionEvent(commmonDistributionConfig.getChannelName(), RestConstants.formatDate(new Date()), commmonDistributionConfig.getProviderName(), commmonDistributionConfig.getFormatType().name(), messageContent,
            fieldAccessor);
    }

    public AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, Collections.emptyList());

    }

}
