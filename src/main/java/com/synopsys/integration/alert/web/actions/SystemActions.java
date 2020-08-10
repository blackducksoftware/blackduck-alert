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
package com.synopsys.integration.alert.web.actions;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusUtility;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.web.config.FieldModelProcessor;
import com.synopsys.integration.rest.RestConstants;

@Component
public class SystemActions {
    private final Logger logger = LoggerFactory.getLogger(SystemActions.class);

    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;
    private final FieldModelProcessor fieldModelProcessor;
    private final SettingsUtility settingsUtility;

    @Autowired
    public SystemActions(SystemStatusUtility systemStatusUtility, SystemMessageUtility systemMessageUtility, FieldModelProcessor fieldModelProcessor, SettingsUtility settingsUtility) {
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
        this.fieldModelProcessor = fieldModelProcessor;
        this.settingsUtility = settingsUtility;
    }

    public boolean isSystemInitialized() {
        return systemStatusUtility.isSystemInitialized();
    }

    public List<SystemMessageModel> getSystemMessagesSinceStartup() {
        return systemMessageUtility.getSystemMessagesAfter(systemStatusUtility.getStartupTime());
    }

    public List<SystemMessageModel> getSystemMessagesAfter(String startDate) throws ParseException {
        OffsetDateTime date = DateUtils.parseDate(startDate, RestConstants.JSON_DATE_FORMAT);
        return systemMessageUtility.getSystemMessagesAfter(date);
    }

    public List<SystemMessageModel> getSystemMessagesBefore(String endDate) throws ParseException {
        OffsetDateTime date = DateUtils.parseDate(endDate, RestConstants.JSON_DATE_FORMAT);
        return systemMessageUtility.getSystemMessagesBefore(date);
    }

    public List<SystemMessageModel> getSystemMessagesBetween(String startDate, String endDate) throws ParseException {
        DateRange dateRange = DateRange.of(startDate, endDate);
        return systemMessageUtility.findBetween(dateRange);
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessageUtility.getSystemMessages();
    }

    public FieldModel getCurrentSystemSetup() {
        try {
            return settingsUtility.getFieldModel().orElse(null);
        } catch (AlertException ex) {
            logger.error("Error getting initial settings", ex);
        }

        return null;
    }

    public FieldModel saveRequiredInformation(FieldModel settingsToSave, List<AlertFieldStatus> fieldErrors) throws AlertException {
        FieldModel systemSettings = settingsToSave;
        fieldErrors.addAll(fieldModelProcessor.validateFieldModel(systemSettings));
        if (fieldErrors.isEmpty()) {
            if (settingsUtility.doesConfigurationExist()) {
                systemSettings = settingsUtility.updateSettings(Long.valueOf(settingsToSave.getId()), settingsToSave);
            } else {
                systemSettings = settingsUtility.saveSettings(settingsToSave);
            }
            systemStatusUtility.setSystemInitialized(true);
        }

        return systemSettings;
    }

}
