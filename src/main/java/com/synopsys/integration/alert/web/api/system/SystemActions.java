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
package com.synopsys.integration.alert.web.api.system;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusUtility;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.rest.RestConstants;

@Component
public class SystemActions {
    private final Logger logger = LoggerFactory.getLogger(SystemActions.class);

    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;

    @Autowired
    public SystemActions(SystemStatusUtility systemStatusUtility, SystemMessageUtility systemMessageUtility) {
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
    }

    public ActionResponse<MultiSystemMessageModel> getSystemMessages(@Nullable String startDate, @Nullable String endDate) {
        try {
            if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
                return getSystemMessages();
            } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
                return getSystemMessagesAfter(startDate);
            } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                return getSystemMessagesBefore(endDate);
            } else {
                return getSystemMessagesBetween(startDate, endDate);
            }
        } catch (ParseException ex) {
            logger.error("error occurred getting system messages", ex);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public ActionResponse<MultiSystemMessageModel> getSystemMessagesSinceStartup() {
        List<SystemMessageModel> messages = systemMessageUtility.getSystemMessagesAfter(systemStatusUtility.getStartupTime());
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessagesAfter(String startDate) throws ParseException {
        OffsetDateTime date = DateUtils.parseDate(startDate, RestConstants.JSON_DATE_FORMAT);
        List<SystemMessageModel> messages = systemMessageUtility.getSystemMessagesAfter(date);
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessagesBefore(String endDate) throws ParseException {
        OffsetDateTime date = DateUtils.parseDate(endDate, RestConstants.JSON_DATE_FORMAT);
        List<SystemMessageModel> messages = systemMessageUtility.getSystemMessagesBefore(date);
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessagesBetween(String startDate, String endDate) throws ParseException {
        DateRange dateRange = DateRange.of(startDate, endDate);
        List<SystemMessageModel> messages = systemMessageUtility.findBetween(dateRange);
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessages() {
        List<SystemMessageModel> messages = systemMessageUtility.getSystemMessages();
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }
}
