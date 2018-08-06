/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.alert.workflow.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.TestAlertProperties;
import com.blackducksoftware.integration.alert.TestBlackDuckProperties;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.google.gson.Gson;

public class NotificationItemProcessorTest {

    @Test
    public void testProcess() throws HubIntegrationException {
        final NotificationModel notificationModel = new NotificationModel(null, null);
        final NotificationTypeProcessor notificationTypeProcessor = Mockito.mock(NotificationTypeProcessor.class);
        Mockito.when(notificationTypeProcessor.isApplicable(Mockito.any())).thenReturn(true);
        Mockito.when(notificationTypeProcessor.process(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(notificationModel));

        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(Arrays.asList(notificationTypeProcessor), contentConverter);

        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new TestAlertProperties());

        final HubBucket hubBucket = new HubBucket();
        final NotificationDetailResult notificationResult = Mockito.mock(NotificationDetailResult.class);

        final NotificationDetailResults notificationData = new NotificationDetailResults(Arrays.asList(notificationResult), Optional.empty(), Optional.empty(), hubBucket);
        final AlertEvent alertEvent = notificationItemProcessor.process(globalProperties, notificationData);

        assertEquals("DB_STORE_EVENT", alertEvent.getDestination());

        final String content = alertEvent.getContent();
        final NotificationModels models = contentConverter.getJsonContent(content, NotificationModels.class);
        assertTrue(models.getNotificationModelList().size() == 1);
    }
}
