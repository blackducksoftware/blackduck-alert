package com.blackducksoftware.integration.alert.provider.hub.accumulator;

import static org.junit.Assert.*;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.accumulator.SearchIntervalAccumulator;
import com.blackducksoftware.integration.alert.common.enumeration.AlertEnvironment;
import com.blackducksoftware.integration.alert.common.enumeration.InternalEventTypes;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.policy.PolicyNotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.vulnerability.VulnerabilityNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

public class NotificationAccumulatorTest {

    private Gson gson;
    private ContentConverter contentConverter;

    private File testAccumulatorParent;

    @Before
    public void init() throws Exception {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        testAccumulatorParent = new File("testAccumulatorDirectory");
        testAccumulatorParent.mkdirs();
        System.out.println(testAccumulatorParent.getCanonicalPath());
    }

    @After
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(testAccumulatorParent);
    }

    private NotificationAccumulator createNonProcessingAccumulator(final GlobalProperties globalProperties) {
        return createAccumulator(globalProperties, Collections.emptyList());
    }

    private NotificationAccumulator createAccumulator(final GlobalProperties globalProperties, final List<NotificationTypeProcessor> processorList) {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        return new NotificationAccumulator(taskScheduler, globalProperties, contentConverter, notificationManager, channelTemplateManager, processorList);
    }

    @Test
    public void testFormatDate() {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final NotificationAccumulator notificationAccumulator = createNonProcessingAccumulator(globalProperties);
        final Date date = new Date();
        assertEquals(RestConnection.formatDate(date), notificationAccumulator.formatDate(date));
    }

    @Test
    public void testCreateDateRange() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.when(globalProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME)).thenReturn(testAccumulatorParent.getCanonicalPath());
        final NotificationAccumulator notificationAccumulator = createNonProcessingAccumulator(globalProperties);
        final Pair<Date, Date> dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        final ZonedDateTime startTime = ZonedDateTime.ofInstant(dateRange.getLeft().toInstant(), ZoneOffset.UTC);
        final ZonedDateTime endTime = ZonedDateTime.ofInstant(dateRange.getRight().toInstant(), ZoneOffset.UTC);
        assertNotEquals(dateRange.getLeft(), dateRange.getRight());
        final ZonedDateTime expectedStartTime = endTime.minusMinutes(1);
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testCreateDateRangeWithExistingFile() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.when(globalProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME)).thenReturn(testAccumulatorParent.getCanonicalPath());
        final NotificationAccumulator notificationAccumulator = createNonProcessingAccumulator(globalProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, SearchIntervalAccumulator.ENCODING);
        final Pair<Date, Date> dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        final Date actualStartDate = dateRange.getLeft();
        final Date actualEndDate = dateRange.getRight();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    @Test
    public void testAccumulate() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);

        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final NotificationAccumulator notificationAccumulator = new NotificationAccumulator(taskScheduler, globalProperties, contentConverter, notificationManager, channelTemplateManager, processorList);
        final ComponentVersionView versionView = new ComponentVersionView();

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        resultList.addAll(vulnerabilityResults.getResults());
        resultList.addAll(createPolicyViolationNotification());

        final NotificationDetailResults notificationResults = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());

        Mockito.when(globalProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME)).thenReturn(testAccumulatorParent.getCanonicalPath());
        Mockito.doReturn(Optional.of(restConnection)).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationDetailResultsPopulated(Mockito.any(), Mockito.any(), Mockito.any());

        final Pair<Date, Date> dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        notificationAccumulator.accumulate(dateRange);
        assertTrue(notificationAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    @Test
    public void testRun() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);

        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);

        final ComponentVersionView versionView = new ComponentVersionView();

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        resultList.addAll(vulnerabilityResults.getResults());
        resultList.addAll(createPolicyViolationNotification());

        final NotificationDetailResults notificationResults = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());

        Mockito.when(globalProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME)).thenReturn(testAccumulatorParent.getCanonicalPath());
        Mockito.doReturn(Optional.of(restConnection)).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationDetailResultsPopulated(Mockito.any(), Mockito.any(), Mockito.any());

        final NotificationAccumulator notificationAccumulator = new NotificationAccumulator(taskScheduler, globalProperties, contentConverter, notificationManager, channelTemplateManager, processorList);
        final NotificationAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        //Mockito.verify(spiedAccumulator).formatDate(Mockito.any());
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).read(Mockito.any());
        Mockito.verify(spiedAccumulator).process(Mockito.any());
        Mockito.verify(spiedAccumulator).write(Mockito.any());
    }

    @Test
    public void testRead() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);

        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);

        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);

        final AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.projectName = "VulnerableProject";
        affectedProjectVersion.projectVersion = "VulnerableProjectUrl";
        affectedProjectVersion.projectVersionName = "1.2.3";

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.componentName = componentName;
        content.componentVersion = componentVersionUrl;
        content.versionName = "1.0.0";
        content.affectedProjectVersions = Arrays.asList(affectedProjectVersion);

        final NotificationDetailResult detail = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));

        Mockito.doReturn(Optional.of(restConnection)).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationDetailResultsPopulated(Mockito.any(), Mockito.any(), Mockito.any());

        final NotificationAccumulator notificationAccumulator = createNonProcessingAccumulator(globalProperties);
        final Pair<Date, Date> dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<NotificationDetailResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertTrue(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadNoNotifications() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createEmptyNotificationResults();

        Mockito.doReturn(Optional.of(restConnection)).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationDetailResultsPopulated(Mockito.any(), Mockito.any(), Mockito.any());
        final NotificationAccumulator notificationAccumulator = createNonProcessingAccumulator(globalProperties);
        final Pair<Date, Date> dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<NotificationDetailResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadMissingRestConnection() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.doReturn(Optional.empty()).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        final NotificationAccumulator notificationAccumulator = createNonProcessingAccumulator(globalProperties);
        final Pair<Date, Date> dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<NotificationDetailResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());
    }

    @Test
    public void testProcess() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final NotificationAccumulator notificationAccumulator = createAccumulator(globalProperties, processorList);
        final ComponentVersionView versionView = new ComponentVersionView();

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        resultList.addAll(vulnerabilityResults.getResults());
        resultList.addAll(createPolicyViolationNotification());

        final NotificationDetailResults notificationData = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());

        final AlertEvent storeEvent = notificationAccumulator.process(notificationData);

        assertNotNull(storeEvent);
        final Optional<NotificationModels> optionalModel = Optional.ofNullable(contentConverter.getJsonContent(storeEvent.getContent(), NotificationModels.class));
        final List<NotificationModel> notifications = optionalModel.get().getNotificationModelList();

        assertFalse(notifications.isEmpty());
    }

    @Test
    public void testProcessorListNull() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final NotificationAccumulator notificationAccumulator = createAccumulator(globalProperties, processorList);
        final ComponentVersionView versionView = new ComponentVersionView();
        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        final NotificationDetailResults notificationData = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());

        final AlertEvent storeEventNull = notificationAccumulator.process(notificationData);
        assertNotNull(storeEventNull);
        final Optional<NotificationModels> optionalModel = Optional.ofNullable(contentConverter.getJsonContent(storeEventNull.getContent(), NotificationModels.class));
        assertTrue(optionalModel.get().getNotificationModelList().isEmpty());
    }

    @Test
    public void testWrite() {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationAccumulator notificationAccumulator = new NotificationAccumulator(taskScheduler, globalProperties, contentConverter, notificationManager, channelTemplateManager, Collections.emptyList());

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final AlertEvent storeEvent = new AlertEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), contentConverter.getJsonString(models));
        notificationAccumulator.write(storeEvent);

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    private List<NotificationDetailResult> createPolicyViolationNotification() {
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);
        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.0.0";
        content.projectVersion = "project version url";
        content.componentVersionsInViolation = 1;

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolationName";
        policyInfo.policy = "policyUrl";
        content.policyInfos = Arrays.asList(policyInfo);

        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.componentName = "notification test component";
        componentVersionStatus.componentVersionName = "1.2.3";
        componentVersionStatus.component = "component url";
        componentVersionStatus.componentVersion = "component version url";
        componentVersionStatus.componentIssueLink = "issuesLink";
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "IN_VIOLATION";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        final List<NotificationDetailResult> detailList = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        return detailList;
    }
}
