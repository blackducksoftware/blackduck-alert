package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.common.digest.DateRange;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.workflow.NotificationManager;
import com.synopsys.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.synopsys.integration.alert.workflow.processor.policy.PolicyNotificationTypeProcessor;
import com.synopsys.integration.alert.workflow.processor.vulnerability.VulnerabilityNotificationTypeProcessor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.NotificationView;
import com.synopsys.integration.blackduck.notification.CommonNotificationView;
import com.synopsys.integration.blackduck.notification.CommonNotificationViewResults;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.CommonNotificationService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.connection.RestConnection;

public class BlackDuckAccumulatorTest {

    private File testAccumulatorParent;

    private TestAlertProperties testAlertProperties;
    private TestBlackDuckProperties testBlackDuckProperties;
    private NotificationManager notificationManager;
    private TaskScheduler taskScheduler;

    @Before
    public void init() throws Exception {
        testAccumulatorParent = new File("testAccumulatorDirectory");
        testAccumulatorParent.mkdirs();
        System.out.println(testAccumulatorParent.getCanonicalPath());

        testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertConfigHome(testAccumulatorParent.getCanonicalPath());
        testBlackDuckProperties = new TestBlackDuckProperties(testAlertProperties);

        notificationManager = Mockito.mock(NotificationManager.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);
    }

    @After
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(testAccumulatorParent);
    }

    private BlackDuckAccumulator createNonProcessingAccumulator(final BlackDuckProperties blackDuckProperties) {
        return createAccumulator(blackDuckProperties, Collections.emptyList());
    }

    private BlackDuckAccumulator createAccumulator(final BlackDuckProperties blackDuckProperties, final List<NotificationTypeProcessor> processorList) {
        return new BlackDuckAccumulator(taskScheduler, testAlertProperties, blackDuckProperties, notificationManager);
    }

    @Test
    public void testFormatDate() {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        final Date date = new Date();
        assertEquals(RestConstants.formatDate(date), notificationAccumulator.formatDate(date));
    }

    @Test
    public void testCreateDateRange() {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        final ZonedDateTime startTime = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneOffset.UTC);
        final ZonedDateTime endTime = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneOffset.UTC);
        assertNotEquals(dateRange.getStart(), dateRange.getEnd());
        final ZonedDateTime expectedStartTime = endTime.minusMinutes(1);
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testCreateDateRangeIOException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, BlackDuckAccumulator.ENCODING);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("Can't read file test exception")).when(spiedAccumulator).readSearchStartTime(Mockito.any());
        final DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeParseException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, BlackDuckAccumulator.ENCODING);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new ParseException("Can't parse date test exception", 1)).when(spiedAccumulator).parseDateString(Mockito.any());
        final DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeWithExistingFile() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, BlackDuckAccumulator.ENCODING);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        final Date actualStartDate = dateRange.getStart();
        final Date actualEndDate = dateRange.getEnd();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    @Test
    public void testRun() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, notificationManager);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulate() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, notificationManager);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        assertTrue(spiedAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(spiedAccumulator, Mockito.times(2)).formatDate(Mockito.any());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, notificationManager);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("can't write last search file")).when(spiedAccumulator).saveNextSearchStart(Mockito.anyString());
        spiedAccumulator.accumulate();
        assertTrue(spiedAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateGetNextRunHasValue() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, notificationManager);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.when(spiedAccumulator.getMillisecondsToNextRun()).thenReturn(Optional.of(Long.MAX_VALUE));
        spiedAccumulator.accumulate();
        assertTrue(spiedAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateWithDateRange() throws Exception {
        // this is the most comprehensive test as it mocks all services in use and completes the full apply
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        final HubServicesFactory blackDuckServicesFactory = Mockito.mock(HubServicesFactory.class);
        final HubService blackDuckService = Mockito.mock(HubService.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final CommonNotificationService commonNotificationService = Mockito.mock(CommonNotificationService.class);
        final NotificationView notificationView = new NotificationView();

        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        final List<NotificationView> notificationViewList = Arrays.asList(notificationView);
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> commonViewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults viewResults = new CommonNotificationViewResults(commonViewList, Optional.of(notificationView.createdAt), Optional.of(RestConstants.formatDate(notificationView.createdAt)));

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.when(blackDuckServicesFactory.createHubService()).thenReturn(blackDuckService);
        Mockito.doReturn(commonNotificationService).when(blackDuckServicesFactory).createCommonNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());
        Mockito.doReturn(commonViewList).when(commonNotificationService).getCommonNotifications(notificationViewList);
        Mockito.doReturn(viewResults).when(commonNotificationService).getCommonNotificationViewResults(commonViewList);

        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, mockedBlackDuckProperties, notificationManager);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        final DateRange dateRange = spiedAccumulator.createDateRange(spiedAccumulator.getSearchRangeFilePath());
        spiedAccumulator.accumulate(dateRange);
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).read(Mockito.any());
        Mockito.verify(spiedAccumulator).process(Mockito.any());
        Mockito.verify(spiedAccumulator).write(Mockito.any());
    }

    @Test
    public void testAccumulateNextRunEmpty() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, notificationManager);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).getMillisecondsToNextRun();
    }

    @Test
    public void testRead() throws Exception {
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        final HubServicesFactory blackDuckServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final HubService blackDuckService = Mockito.mock(HubService.class);
        final CommonNotificationService commonNotificationService = Mockito.mock(CommonNotificationService.class);
        final NotificationView notificationView = new NotificationView();

        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        final List<NotificationView> notificationViewList = Arrays.asList(notificationView);
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> commonViewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults viewResults = new CommonNotificationViewResults(commonViewList, Optional.of(notificationView.createdAt), Optional.of(RestConstants.formatDate(notificationView.createdAt)));

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.when(blackDuckServicesFactory.createHubService()).thenReturn(blackDuckService);
        Mockito.doReturn(commonNotificationService).when(blackDuckServicesFactory).createCommonNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());
        Mockito.doReturn(commonViewList).when(commonNotificationService).getCommonNotifications(notificationViewList);
        Mockito.doReturn(viewResults).when(commonNotificationService).getCommonNotificationViewResults(commonViewList);

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertTrue(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadNoNotifications() throws Exception {
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        final HubServicesFactory blackDuckServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final HubService blackDuckService = Mockito.mock(HubService.class);
        final CommonNotificationService commonNotificationService = Mockito.mock(CommonNotificationService.class);
        final NotificationView notificationView = new NotificationView();

        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        final List<NotificationView> notificationViewList = Arrays.asList(notificationView);
        final List<CommonNotificationView> commonViewList = Collections.emptyList();
        final CommonNotificationViewResults viewResults = new CommonNotificationViewResults(commonViewList, Optional.of(notificationView.createdAt), Optional.of(RestConstants.formatDate(notificationView.createdAt)));

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.when(blackDuckServicesFactory.createHubService()).thenReturn(blackDuckService);
        Mockito.doReturn(commonNotificationService).when(blackDuckServicesFactory).createCommonNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());
        Mockito.doReturn(commonViewList).when(commonNotificationService).getCommonNotifications(notificationViewList);
        Mockito.doReturn(viewResults).when(commonNotificationService).getCommonNotificationViewResults(commonViewList);

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadMissingRestConnection() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.empty()).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadException() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doThrow(RuntimeException.class).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());

    }

    @Test
    public void testProcess() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties, processorList);
        final NotificationView notificationView = new NotificationView();
        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        notificationView.json = "{ content: \"content is here...\"}";
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> viewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults notificationResults = new CommonNotificationViewResults(viewList, Optional.of(new Date()), Optional.of(RestConstants.formatDate(new Date())));
        final List<NotificationContent> notificationContentList = notificationAccumulator.process(notificationResults);
        assertFalse(notificationContentList.isEmpty());
    }

    @Test
    public void testProcessEmptyList() {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties, processorList);
        final CommonNotificationViewResults viewList = new CommonNotificationViewResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        final List<NotificationContent> contentList = notificationAccumulator.process(viewList);
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void testWrite() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, notificationManager);

        final NotificationContent content = new MockNotificationContent(new Date(), "BlackDuck", "NotificationType", "{content: \"content is here\"}", null).createEntity();
        final List<NotificationContent> notificationContentList = Arrays.asList(content);
        notificationAccumulator.write(notificationContentList);

        Mockito.verify(notificationManager, Mockito.times(notificationContentList.size())).saveNotification(Mockito.any());
    }
}
