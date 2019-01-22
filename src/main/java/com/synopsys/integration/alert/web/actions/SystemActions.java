/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.actions;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.DateRange;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.web.config.ConfigActions;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.SystemMessageModel;
import com.synopsys.integration.rest.RestConstants;

@Component
public class SystemActions {
    private final Logger logger = LoggerFactory.getLogger(SystemActions.class);
    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;
    private final ConfigActions configActions;

    @Autowired
    public SystemActions(final SystemStatusUtility systemStatusUtility, final SystemMessageUtility systemMessageUtility, final ConfigActions configActions) {
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
        this.configActions = configActions;
    }

    public boolean isSystemInitialized() {
        return systemStatusUtility.isSystemInitialized();
    }

    public List<SystemMessageModel> getSystemMessagesSinceStartup() {
        return systemMessageUtility.getSystemMessagesAfter(systemStatusUtility.getStartupTime()).stream().map(this::convert).collect(Collectors.toList());
    }

    public List<SystemMessageModel> getSystemMessagesAfter(final String startDate) throws ParseException {
        final Date date = RestConstants.parseDateString(startDate);
        return systemMessageUtility.getSystemMessagesAfter(date).stream().map(this::convert).collect(Collectors.toList());
    }

    public List<SystemMessageModel> getSystemMessagesBefore(final String endDate) throws ParseException {
        final Date date = RestConstants.parseDateString(endDate);
        return systemMessageUtility.getSystemMessagesBefore(date).stream().map(this::convert).collect(Collectors.toList());
    }

    public List<SystemMessageModel> getSystemMessagesBetween(final String startDate, final String endDate) throws ParseException {
        final DateRange dateRange = DateRange.of(startDate, endDate);
        return systemMessageUtility.findBetween(dateRange).stream().map(this::convert).collect(Collectors.toList());
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessageUtility.getSystemMessages().stream().map(this::convert).collect(Collectors.toList());
    }

    private SystemMessageModel convert(final SystemMessage systemMessage) {
        final String createdAt = RestConstants.formatDate(systemMessage.getCreated());
        return new SystemMessageModel(systemMessage.getSeverity(), createdAt, systemMessage.getContent(), systemMessage.getType());
    }

    public FieldModel getCurrentSystemSetup() {
        final Map<String, FieldValueModel> valueMap = new HashMap<>();
        FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", valueMap);

        try {
            final List<FieldModel> fieldModels = configActions.getConfigs(ConfigContextEnum.GLOBAL, SettingsDescriptor.SETTINGS_COMPONENT);
            if (fieldModels.size() == 1) {
                model = fieldModels.get(0);
            }
        } catch (final AlertException ex) {
            logger.error("Error getting initial settings", ex);
        }

        return model;
    }

    public FieldModel saveRequiredInformation(final FieldModel settingsToSave, final Map<String, String> fieldErrors) {
        FieldModel systemSettings = settingsToSave;
        try {
            configActions.validateConfig(systemSettings, fieldErrors);
            if (fieldErrors.isEmpty()) {
                if (configActions.doesConfigExist(settingsToSave.getId())) {
                    systemSettings = configActions.updateConfig(Long.valueOf(settingsToSave.getId()), settingsToSave);
                } else {
                    systemSettings = configActions.saveConfig(settingsToSave);
                }
            }
        } catch (final AlertException | AlertFieldException ex) {
            logger.error("Error saving initial configuration", ex);
        }
        return systemSettings;
    }
}
