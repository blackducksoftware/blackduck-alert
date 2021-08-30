package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.filter.PageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckAccumulatorTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    /**
     * This test should simulate a normal run of the accumulator with notifications present.
     */
    @Test
    public void runTest() throws Exception {
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
        Mockito.when(notificationAccessor.saveAllNotifications(Mockito.anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        EventManager eventManager = Mockito.mock(EventManager.class);
        Mockito.doNothing().when(eventManager).sendEvent(Mockito.any(NotificationReceivedEvent.class));

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, null, notificationAccessor, taskPropertiesAccessor, blackDuckProperties, validator, eventManager, notificationRetrieverFactory);
        accumulator.run();

        Mockito.verify(notificationAccessor, Mockito.times(1)).saveAllNotifications(Mockito.anyList());
    }

    @Test
    public void runValidateFalseTest() {
        BlackDuckProperties invalidProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(invalidProperties, false);
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = Mockito.mock(BlackDuckNotificationRetrieverFactory.class);

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, null, null, null, invalidProperties, validator, null, notificationRetrieverFactory);
        accumulator.run();

        Mockito.verify(notificationRetrieverFactory, Mockito.times(0)).createBlackDuckNotificationRetriever(invalidProperties);
    }

    @Test
    public void runCreateNotificationRetrieverEmptyTest() {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        BlackDuckProperties blackDuckProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(blackDuckProperties, true);
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = createBlackDuckNotificationRetrieverFactory(blackDuckProperties, null);

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, null, null, taskPropertiesAccessor, blackDuckProperties, validator, null, notificationRetrieverFactory);
        accumulator.run();

        Mockito.verify(taskPropertiesAccessor, Mockito.times(0)).getTaskProperty(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void runNotificationRetrieverThrowsException() throws IntegrationException {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        BlackDuckProperties blackDuckProperties = createBlackDuckProperties();
        BlackDuckSystemValidator validator = createBlackDuckValidator(blackDuckProperties, true);

        BlackDuckNotificationRetriever notificationRetriever = Mockito.mock(BlackDuckNotificationRetriever.class);
        Mockito.when(notificationRetriever.retrievePageOfFilteredNotifications(Mockito.any(), Mockito.anyList())).thenThrow(new IntegrationException("Test Exception"));
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = createBlackDuckNotificationRetrieverFactory(blackDuckProperties, notificationRetriever);

        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);

        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, null, notificationAccessor, taskPropertiesAccessor, blackDuckProperties, validator, null, notificationRetrieverFactory);
        accumulator.run();

        Mockito.verify(notificationAccessor, Mockito.times(0)).saveAllNotifications(Mockito.anyList());
    }

    private BlackDuckProperties createBlackDuckProperties() {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(0L);
        return blackDuckProperties;
    }

    private BlackDuckSystemValidator createBlackDuckValidator(BlackDuckProperties blackDuckProperties, boolean validationResult) {
        BlackDuckSystemValidator validator = Mockito.mock(BlackDuckSystemValidator.class);
        Mockito.when(validator.validate(Mockito.eq(blackDuckProperties))).thenReturn(validationResult);
        return validator;
    }

    private BlackDuckNotificationRetrieverFactory createBlackDuckNotificationRetrieverFactory(BlackDuckProperties blackDuckProperties, @Nullable BlackDuckNotificationRetriever notificationRetriever) {
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory = Mockito.mock(BlackDuckNotificationRetrieverFactory.class);
        Mockito.when(notificationRetrieverFactory.createBlackDuckNotificationRetriever(blackDuckProperties)).thenReturn(Optional.ofNullable(notificationRetriever));
        return notificationRetrieverFactory;
    }

    private StatefulAlertPage<NotificationUserView, IntegrationException> createMockNotificationPage(PageRetriever pageRetriever) throws IntegrationException {
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
