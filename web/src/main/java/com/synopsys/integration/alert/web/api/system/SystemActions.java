/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.system;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class SystemActions {
    private final Logger logger = LoggerFactory.getLogger(SystemActions.class);

    private final SystemStatusAccessor systemStatusAccessor;
    private final SystemMessageAccessor systemMessageAccessor;

    @Autowired
    public SystemActions(SystemStatusAccessor systemStatusAccessor, SystemMessageAccessor systemMessageAccessor) {
        this.systemStatusAccessor = systemStatusAccessor;
        this.systemMessageAccessor = systemMessageAccessor;
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
        List<SystemMessageModel> messages = systemMessageAccessor.getSystemMessagesAfter(systemStatusAccessor.getStartupTime());
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessagesAfter(String startDate) throws ParseException {
        OffsetDateTime date = DateUtils.parseDateFromJsonString(startDate);
        List<SystemMessageModel> messages = systemMessageAccessor.getSystemMessagesAfter(date);
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessagesBefore(String endDate) throws ParseException {
        OffsetDateTime date = DateUtils.parseDateFromJsonString(endDate);
        List<SystemMessageModel> messages = systemMessageAccessor.getSystemMessagesBefore(date);
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessagesBetween(String startDate, String endDate) throws ParseException {
        DateRange dateRange = DateRange.of(startDate, endDate);
        List<SystemMessageModel> messages = systemMessageAccessor.findBetween(dateRange);
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

    private ActionResponse<MultiSystemMessageModel> getSystemMessages() {
        List<SystemMessageModel> messages = systemMessageAccessor.getSystemMessages();
        return new ActionResponse<>(HttpStatus.OK, new MultiSystemMessageModel(messages));
    }

}
