package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckValidator;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class BlackDuckAccumulatorTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    private File testAccumulatorParent;

    private TestBlackDuckProperties testBlackDuckProperties;
    private DefaultNotificationAccessor notificationManager;
    private TaskScheduler taskScheduler;
    private ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;
    private BlackDuckValidator blackDuckValidator;
    private EventManager eventManager;

    @BeforeEach
    public void init() throws Exception {
        testAccumulatorParent = new File("testAccumulatorDirectory");
        testAccumulatorParent.mkdirs();
        System.out.println(testAccumulatorParent.getCanonicalPath());

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        testAlertProperties.setAlertConfigHome(testAccumulatorParent.getCanonicalPath());
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        testBlackDuckProperties = new TestBlackDuckProperties(new Gson(), new ObjectMapper(), testAlertProperties, new TestProperties(), proxyManager);

        notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);

        providerTaskPropertiesAccessor = new ProviderTaskPropertiesAccessor() {
            final Map<String, String> properties = new HashMap<>();

            @Override
            public Optional<String> getTaskProperty(String taskName, String propertyKey) {
                return Optional.ofNullable(properties.get(taskName + propertyKey));
            }

            @Override
            public void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue) {
                properties.put(taskName + propertyKey, propertyValue);
            }
        };

        blackDuckValidator = Mockito.mock(BlackDuckValidator.class);
        Mockito.when(blackDuckValidator.validate(Mockito.any())).thenReturn(true);
        eventManager = Mockito.mock(EventManager.class);
    }

    @AfterEach
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(testAccumulatorParent);
    }

    @Test
    public void runTest() {
        BlackDuckAccumulator notificationAccumulator = createAccumulator(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        Mockito.verify(spiedAccumulator).retrieveAndStoreNotifications(Mockito.any());
    }

    @Test
    public void runVerifyFalseTest() {
        Mockito.when(blackDuckValidator.validate(Mockito.any())).thenReturn(false);
        BlackDuckAccumulator notificationAccumulator = createAccumulator(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        Mockito.verify(spiedAccumulator, Mockito.times(0)).retrieveAndStoreNotifications(Mockito.any());
    }

    @Test
    public void accumulateTest() {
        BlackDuckAccumulator notificationAccumulator = createAccumulator(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).retrieveAndStoreNotifications(Mockito.any());
    }

    @Test
    public void accumulateGetNextRunHasValueTest() {
        BlackDuckAccumulator notificationAccumulator = createAccumulator(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.when(spiedAccumulator.getMillisecondsToNextRun()).thenReturn(Optional.of(Long.MAX_VALUE));
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).retrieveAndStoreNotifications(Mockito.any());
    }

    @Test
    public void accumulateWithDateRangeTest() throws Exception {
        // this is the most comprehensive test as it mocks all services in use and completes the full extractApplicableNotifications
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        List<NotificationView> notificationViewList = List.of(notificationView);

        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getFilteredNotifications(Mockito.any(), Mockito.any(), Mockito.anyList());

        BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties);

        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).retrieveAndStoreNotifications(Mockito.any());
        Mockito.verify(spiedAccumulator).process(Mockito.any());
        Mockito.verify(spiedAccumulator).write(Mockito.any());
    }

    @Test
    public void processTest() {
        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties);
        NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        notificationView.setJson("{ content: \"content is here...\"}");
        List<AlertNotificationModel> notificationContentList = notificationAccumulator.process(List.of(notificationView));
        assertFalse(notificationContentList.isEmpty());
    }

    @Test
    public void processEmptyListTest() {
        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties);
        List<AlertNotificationModel> contentList = notificationAccumulator.process(List.of());
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void writeTest() {
        BlackDuckAccumulator notificationAccumulator = createAccumulator(testBlackDuckProperties);
        OffsetDateTime creationDate = DateUtils.createCurrentDateTimestamp();
        AlertNotificationModel content = new AlertNotificationModel(1L, 1L, "BlackDuck", "BlackDuck_1", "NotificationType", "{content: \"content is here\"}", creationDate, creationDate, false);
        List<AlertNotificationModel> notificationContentList = Collections.singletonList(content);
        notificationAccumulator.write(notificationContentList);

        Mockito.verify(notificationManager, Mockito.times(notificationContentList.size())).saveAllNotifications(Mockito.any());
    }

    private BlackDuckAccumulator createAccumulator(BlackDuckProperties blackDuckProperties) {
        return new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, providerTaskPropertiesAccessor, blackDuckProperties, blackDuckValidator, eventManager, blackDuckNotificationRetrieverFactory);
    }

}
