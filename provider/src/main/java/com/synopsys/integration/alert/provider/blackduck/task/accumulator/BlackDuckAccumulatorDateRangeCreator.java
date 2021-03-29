/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.provider.blackduck.task.BlackDuckAccumulator;
import com.synopsys.integration.rest.RestConstants;

@Component
public class BlackDuckAccumulatorDateRangeCreator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;

    @Autowired
    public BlackDuckAccumulatorDateRangeCreator(ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor) {
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
    }

    public DateRange createDateRange(String taskName) {
        OffsetDateTime endDate = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime startDate = endDate;
        try {
            Optional<String> nextSearchStartTime = getNextSearchStart(taskName);
            if (nextSearchStartTime.isPresent()) {
                String lastRunValue = nextSearchStartTime.get();
                startDate = parseDateString(lastRunValue);
            } else {
                startDate = endDate.minusMinutes(1);
            }
        } catch (ParseException e) {
            logger.error("Error creating date range", e);
        }
        return DateRange.of(startDate, endDate);
    }

    private Optional<String> getNextSearchStart(String taskName) {
        return providerTaskPropertiesAccessor.getTaskProperty(taskName, BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE);
    }

    private OffsetDateTime parseDateString(String date) throws ParseException {
        return DateUtils.parseDate(date, RestConstants.JSON_DATE_FORMAT);
    }

}
