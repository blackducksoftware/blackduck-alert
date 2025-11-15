/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.mapping.JobNotificationMapper2;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.test.common.MockAlertProperties;

@ExtendWith(SpringExtension.class)
class NotificationMappingProcessorTest {
    @Mock
    NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    @Mock
    JobNotificationMapper2 jobNotificationMapper;
    @Mock
    NotificationAccessor notificationAccessor;

    private static Stream<Arguments> testBatchLimits() {
        return Stream.of(
            Arguments.of(NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MAXIMUM, null),
            Arguments.of(NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MAXIMUM, 11000),
            Arguments.of(NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MAXIMUM, Integer.MAX_VALUE),
            Arguments.of(5000, 5000),
            Arguments.of(NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MINIMUM, 100),
            Arguments.of(NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MINIMUM, Integer.MIN_VALUE)
        );
    }

    @MethodSource("testBatchLimits")
    @ParameterizedTest
    void testNotificationBatchLimitFromEnv(int expectedBatchLimit, Integer testBatchLimit) {
        MockAlertProperties alertProperties = new MockAlertProperties();
        alertProperties.setNotificationMappingBatchLimit(testBatchLimit);
        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(notificationDetailExtractionDelegator, jobNotificationMapper, notificationAccessor, alertProperties);
        assertEquals(expectedBatchLimit, notificationMappingProcessor.getNotificationMappingBatchLimit());
    }

    @Test
    void testHasExceededBatchLimit() {
        MockAlertProperties alertProperties = new MockAlertProperties();
        alertProperties.setNotificationMappingBatchLimit(NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MAXIMUM);
        UUID correlationId = UUID.randomUUID();
        Mockito.when(jobNotificationMapper.hasBatchReachedSizeLimit(correlationId, NotificationMappingProcessor.DEFAULT_BATCH_LIMIT_MAXIMUM)).thenReturn(true);
        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(notificationDetailExtractionDelegator, jobNotificationMapper, notificationAccessor, alertProperties);
        assertTrue(notificationMappingProcessor.hasExceededBatchLimit(correlationId));
    }
}
