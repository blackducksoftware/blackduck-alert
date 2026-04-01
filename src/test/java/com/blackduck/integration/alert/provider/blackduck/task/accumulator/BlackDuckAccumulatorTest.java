/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.processor.filter.PageRetriever;
import com.blackduck.integration.alert.api.processor.filter.StatefulAlertPage;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.NotificationUserView;
import com.blackduck.integration.exception.IntegrationException;

class BlackDuckAccumulatorTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    /**
     * This test should simulate a normal run of the accumulator with notifications present.
     */
    @Test
    void runValidAccumulatorTest() throws Exception {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        BlackDuckProperties blackDuckProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(blackDuckProperties, true);

        PageRetriever pageRetriever = Mockito.mock(PageRetriever.class);
        StatefulAlertPage<NotificationUserView, IntegrationException> notificationPage = createMockNotificationPage(pageRetriever);
        BlackDuckNotificationRetriever notificationRetriever = Mockito.mock(BlackDuckNotificationRetriever.class);
        Mockito.when(notificationRetriever.retrievePageOfFilteredNotifications(Mockito.any(), Mockito.anyList())).thenReturn(notificationPage);
        Mockito.when(pageRetriever.retrieveNextPage(Mockito.anyInt(), Mockito.anyInt())).thenReturn(AlertPagedDetails.emptyPage());
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = createBlackDuckNotificationRetrieverFactory(blackDuckProperties, notificationRetriever);

        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.when(notificationAccessor.saveAllNotificationsInBatch(Mockito.any(), Mockito.anyList())).thenAnswer(invocation -> invocation.getArgument(1));

        EventManager eventManager = Mockito.mock(EventManager.class);
        Mockito.doNothing().when(eventManager).sendEvent(Mockito.any(NotificationReceivedEvent.class));

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, null, notificationAccessor, taskPropertiesAccessor, blackDuckProperties, validator, eventManager, notificationRetrieverFactory);
        accumulator.run();

        Mockito.verify(notificationAccessor, Mockito.times(1)).saveAllNotificationsInBatch(Mockito.any(), Mockito.anyList());
        Mockito.verify(eventManager, Mockito.times(1)).sendEvent(Mockito.any(NotificationReceivedEvent.class));
    }

    @Test
    void runValidateFalseTest() {
        BlackDuckProperties invalidProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(invalidProperties, false);
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = Mockito.mock(BlackDuckNotificationRetrieverFactory.class);

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, null, null, null, invalidProperties, validator, null, notificationRetrieverFactory);
        accumulator.run();

        Mockito.verify(notificationRetrieverFactory, Mockito.times(0)).createBlackDuckNotificationRetriever(invalidProperties);
    }

    @Test
    void runCreateNotificationRetrieverEmptyTest() {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        BlackDuckProperties blackDuckProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(blackDuckProperties, true);
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = createBlackDuckNotificationRetrieverFactory(blackDuckProperties, null);

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(
            BLACK_DUCK_PROVIDER_KEY,
            null,
            null,
            taskPropertiesAccessor,
            blackDuckProperties,
            validator,
            null,
            notificationRetrieverFactory
        );
        accumulator.run();

        Mockito.verify(taskPropertiesAccessor, Mockito.times(0)).getTaskProperty(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void runNotificationRetrieverThrowsException() throws IntegrationException {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        BlackDuckProperties blackDuckProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(blackDuckProperties, true);

        BlackDuckNotificationRetriever notificationRetriever = Mockito.mock(BlackDuckNotificationRetriever.class);
        Mockito.when(notificationRetriever.retrievePageOfFilteredNotifications(Mockito.any(), Mockito.anyList())).thenThrow(new IntegrationException("Test Exception"));
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = createBlackDuckNotificationRetrieverFactory(blackDuckProperties, notificationRetriever);

        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(
            BLACK_DUCK_PROVIDER_KEY,
            null,
            notificationAccessor,
            taskPropertiesAccessor,
            blackDuckProperties,
            validator,
            null,
            notificationRetrieverFactory
        );
        accumulator.run();

        Mockito.verify(notificationAccessor, Mockito.times(0)).saveAllNotifications(Mockito.anyList());
    }

    @Test
    void testContentIdGeneration() {
        Long firstProviderId = 1L;
        Long secondProviderId = 2L;
        String url = "https://a-hub-server.blackduck.com/api/notifications/edea20df-02cf-467b-a98f-d0dc9637770c";
        String sameUrl = "https://a-hub-server.blackduck.com/api/notifications/edea20df-02cf-467b-a98f-d0dc9637770c";
        String differentUrl = "https://a-hub-server.blackduck.com/api/notifications/edfa20bf-03cf-467b-a98f-d0dc9637770c";

        BiFunction<Long, String, String> providerContentIdConverter = (providerId, notificationUrl) -> String.format("%s-%s", providerId, notificationUrl);
        String firstUrl = providerContentIdConverter.apply(firstProviderId, url);
        String firstSameUrl = providerContentIdConverter.apply(firstProviderId, sameUrl);
        String firstDifferentUrl = providerContentIdConverter.apply(firstProviderId, differentUrl);
        String secondUrl = providerContentIdConverter.apply(secondProviderId, url);
        String secondSameUrl = providerContentIdConverter.apply(secondProviderId, sameUrl);
        String secondDifferentUrl = providerContentIdConverter.apply(secondProviderId, differentUrl);

        DigestUtils digestUtils = new DigestUtils("SHA3-256");

        String firstHashUrl = digestUtils.digestAsHex(firstUrl);
        String firstHashOrSame = digestUtils.digestAsHex(firstSameUrl);
        String firstHashOfDifferent = digestUtils.digestAsHex(firstDifferentUrl);

        String secondHashUrl = digestUtils.digestAsHex(secondUrl);
        String secondHashOrSame = digestUtils.digestAsHex(secondSameUrl);
        String secondHashOfDifferent = digestUtils.digestAsHex(secondDifferentUrl);

        assertEquals(firstHashUrl, firstHashOrSame);
        assertNotEquals(firstHashUrl, firstHashOfDifferent);

        assertEquals(secondHashUrl, secondHashOrSame);
        assertNotEquals(secondHashUrl, secondHashOfDifferent);
        assertNotEquals(firstHashUrl, secondHashUrl);

    }

    private BlackDuckProperties createBlackDuckProperties() {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(0L);
        return blackDuckProperties;
    }

    private BlackDuckSystemValidator createBlackDuckValidator(BlackDuckProperties blackDuckProperties, boolean validationResult) {
        BlackDuckSystemValidator blackDuckSystemValidator = Mockito.mock(BlackDuckSystemValidator.class);
        Mockito.when(blackDuckSystemValidator.validate(blackDuckProperties)).thenReturn(validationResult);
        Mockito.doReturn(validationResult).when(blackDuckSystemValidator).canConnect(Mockito.eq(blackDuckProperties), Mockito.any(BlackDuckApiTokenValidator.class));
        return blackDuckSystemValidator;
    }

    private BlackDuckNotificationRetrieverFactory createBlackDuckNotificationRetrieverFactory(BlackDuckProperties blackDuckProperties, @Nullable BlackDuckNotificationRetriever notificationRetriever) {
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = Mockito.mock(BlackDuckNotificationRetrieverFactory.class);
        Mockito.when(notificationRetrieverFactory.createBlackDuckNotificationRetriever(blackDuckProperties)).thenReturn(Optional.ofNullable(notificationRetriever));
        return notificationRetrieverFactory;
    }

    private StatefulAlertPage<NotificationUserView, IntegrationException> createMockNotificationPage(PageRetriever pageRetriever) {
        NotificationUserView notificationView = createMockNotificationView();
        AlertPagedDetails<NotificationUserView> alertPagedDetails = new AlertPagedDetails<>(1, 0, 1, List.of(notificationView));
        return new StatefulAlertPage<>(alertPagedDetails, pageRetriever, BlackDuckNotificationRetriever.HAS_NEXT_PAGE);
    }

    private NotificationUserView createMockNotificationView() {
        NotificationUserView notificationView = Mockito.mock(NotificationUserView.class);
        Mockito.when(notificationView.getCreatedAt()).thenReturn(new Date());
        Mockito.when(notificationView.getType()).thenReturn(NotificationType.PROJECT);
        Mockito.when(notificationView.getJson()).thenReturn("{}");
        return notificationView;
    }

}
