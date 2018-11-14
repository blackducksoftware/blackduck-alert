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
package com.synopsys.integration.alert.web.actions;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.model.DateRange;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.install.RequiredSystemConfiguration;
import com.synopsys.integration.alert.install.SystemInitializer;
import com.synopsys.integration.alert.web.model.SystemMessageModel;
import com.synopsys.integration.rest.RestConstants;

@Component
public class SystemActions {
    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;
    private final SystemInitializer systemInitializer;

    @Autowired
    public SystemActions(final SystemStatusUtility systemStatusUtility, final SystemMessageUtility systemMessageUtility, final SystemInitializer systemInitializer) {
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
        this.systemInitializer = systemInitializer;
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
        final DateRange dateRange = new DateRange(RestConstants.parseDateString(startDate), RestConstants.parseDateString(endDate));
        return systemMessageUtility.findBetween(dateRange).stream().map(this::convert).collect(Collectors.toList());
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessageUtility.getSystemMessages().stream().map(this::convert).collect(Collectors.toList());
    }

    private SystemMessageModel convert(final SystemMessage systemMessage) {
        final String createdAt = RestConstants.formatDate(systemMessage.getCreated());
        return new SystemMessageModel(systemMessage.getType(), createdAt, systemMessage.getContent());
    }

    public RequiredSystemConfiguration saveRequiredInformation(final RequiredSystemConfiguration requiredSystemConfiguration) {
        systemInitializer.updateRequiredConfiguration(requiredSystemConfiguration);
        return requiredSystemConfiguration;
    }
}
