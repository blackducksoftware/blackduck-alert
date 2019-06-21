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
package com.synopsys.integration.alert.channel.jira.actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraGlobalApiAction extends ApiAction {
    private final Logger logger = LoggerFactory.getLogger(JiraGlobalApiAction.class);

    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public JiraGlobalApiAction(final ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public FieldModel afterUpdateAction(final Long id, final FieldModel fieldModel) throws AlertException {
        final String jiraCloudUrl = fieldModel.getFieldValue(JiraDescriptor.KEY_JIRA_URL).orElse("");
        try {
            removeSetupPluginFromDB(id, jiraCloudUrl);
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("There was a problem accessing the DB when updating Jira values: {}", e.getMessage());
        }

        return super.afterSaveAction(id, fieldModel);
    }

    public void removeSetupPluginFromDB(final Long id, final String jiraCloudUrl) throws AlertDatabaseConstraintException, AlertFieldException {
        final Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getConfigurationById(id);
        if (configurationModelOptional.isPresent()) {
            final ConfigurationModel configurationModel = configurationModelOptional.get();
            final String oldUrl = configurationModel.getField(JiraDescriptor.KEY_JIRA_URL)
                                      .flatMap(configurationFieldModel -> configurationFieldModel.getFieldValue()).orElse("");
            if (!oldUrl.equals(jiraCloudUrl)) {
                final List<ConfigurationFieldModel> fieldsCopy = configurationModel.getCopyOfFieldList()
                                                                     .stream()
                                                                     .filter(configurationFieldModel -> !configurationFieldModel.getFieldKey().equals(JiraDescriptor.KEY_JIRA_CONFIGURE_PLUGIN))
                                                                     .collect(Collectors.toList());
                configurationAccessor.updateConfiguration(configurationModel.getConfigurationId(), fieldsCopy);
                throw new AlertFieldException(Map.of(JiraDescriptor.KEY_JIRA_CONFIGURE_PLUGIN, "Please install plugin remotely after changing URL."));
            }
        }
    }

}
