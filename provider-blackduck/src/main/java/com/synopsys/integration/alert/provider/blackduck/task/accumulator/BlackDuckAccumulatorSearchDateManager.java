/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;

public class BlackDuckAccumulatorSearchDateManager {
    public static final String TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE = "last.search.end.date";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;
    private final Long providerConfigId;
    private final String taskName;

    public BlackDuckAccumulatorSearchDateManager(ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor, Long providerConfigId, String taskName) {
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
        this.providerConfigId = providerConfigId;
        this.taskName = taskName;
    }

    public void saveNextSearchStart(OffsetDateTime nextSearchStartTime) {
        String nextSearchStartTimeString = DateUtils.formatDateAsJsonString(nextSearchStartTime);
        logger.info("Accumulator Next Range Start Time: {} ", nextSearchStartTimeString);
        providerTaskPropertiesAccessor.setTaskProperty(providerConfigId, taskName, TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE, nextSearchStartTimeString);
    }

    public DateRange retrieveNextSearchDateRange() {
        OffsetDateTime endDate = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime startDate = endDate;
        try {
            Optional<String> nextSearchStartTime = retrieveNextSearchStart();
            if (nextSearchStartTime.isPresent()) {
                String lastRunValue = nextSearchStartTime.get();
                startDate = DateUtils.parseDateFromJsonString(lastRunValue);
            } else {
                startDate = endDate.minusMinutes(1);
            }
        } catch (ParseException e) {
            logger.error("Error creating accumulator search date range", e);
        }
        return DateRange.of(startDate, endDate);
    }

    private Optional<String> retrieveNextSearchStart() {
        return providerTaskPropertiesAccessor.getTaskProperty(taskName, TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE);
    }

}
