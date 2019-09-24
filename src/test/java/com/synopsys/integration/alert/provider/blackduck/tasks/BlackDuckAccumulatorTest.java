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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckAccumulatorTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    private File testAccumulatorParent;

    private TestBlackDuckProperties testBlackDuckProperties;
    private DefaultNotificationManager notificationManager;
    private TaskScheduler taskScheduler;
    private FilePersistenceUtil filePersistenceUtil;
    private Gson gson;

    @BeforeEach
    public void init() throws Exception {
        gson = new Gson();
        testAccumulatorParent = new File("testAccumulatorDirectory");
        testAccumulatorParent.mkdirs();
        System.out.println(testAccumulatorParent.getCanonicalPath());

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertConfigHome(testAccumulatorParent.getCanonicalPath());
        testBlackDuckProperties = new TestBlackDuckProperties(testAlertProperties);

        notificationManager = Mockito.mock(DefaultNotificationManager.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);
        filePersistenceUtil = new FilePersistenceUtil(testAlertProperties, new Gson());
    }

    @AfterEach
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(testAccumulatorParent);
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
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
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
        filePersistenceUtil.writeToFile(notificationAccumulator.getSearchRangeFileName(), startString);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("Can't read file test exception")).when(spiedAccumulator).readSearchStartTime(Mockito.any());
        final DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
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
        filePersistenceUtil.writeToFile(notificationAccumulator.getSearchRangeFileName(), startString);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new ParseException("Can't parse date test exception", 1)).when(spiedAccumulator).parseDateString(Mockito.any());
        final DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
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
        filePersistenceUtil.writeToFile(notificationAccumulator.getSearchRangeFileName(), startString);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        assertNotNull(dateRange);
        final Date actualStartDate = dateRange.getStart();
        final Date actualEndDate = dateRange.getEnd();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    @Test
    public void testRun() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulate() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        assertTrue(filePersistenceUtil.exists(spiedAccumulator.getSearchRangeFileName()));
        Mockito.verify(spiedAccumulator, Mockito.times(2)).formatDate(Mockito.any());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("can't write last search file")).when(spiedAccumulator).saveNextSearchStart(Mockito.anyString());
        spiedAccumulator.accumulate();
        assertTrue(filePersistenceUtil.exists(spiedAccumulator.getSearchRangeFileName()));
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateGetNextRunHasValue() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.when(spiedAccumulator.getMillisecondsToNextRun()).thenReturn(Optional.of(Long.MAX_VALUE));
        spiedAccumulator.accumulate();
        assertTrue(filePersistenceUtil.exists(spiedAccumulator.getSearchRangeFileName()));
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateWithDateRange() throws Exception {
        // this is the most comprehensive test as it mocks all services in use and completes the full extractApplicableNotifications
        final BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        final BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        final List<NotificationView> notificationViewList = Arrays.asList(notificationView);

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getFilteredNotifications(Mockito.any(), Mockito.any(), Mockito.anyList());

        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, mockedBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        final DateRange dateRange = spiedAccumulator.createDateRange(spiedAccumulator.getSearchRangeFileName());
        spiedAccumulator.accumulate(dateRange);
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).read(Mockito.any());
        Mockito.verify(spiedAccumulator).process(Mockito.any());
        Mockito.verify(spiedAccumulator).write(Mockito.any());
    }

    @Test
    public void testAccumulateNextRunEmpty() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).getMillisecondsToNextRun();
    }

    @Test
    public void testRead() throws Exception {
        final BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        final BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        final List<NotificationView> notificationViewList = Arrays.asList(notificationView);

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getFilteredNotifications(Mockito.any(), Mockito.any(), Mockito.anyList());

        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        final List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertFalse(notificationViews.isEmpty());
    }

    @Test
    public void testReadNoNotifications() throws Exception {
        final BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        final BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final List<NotificationView> notificationViewList = List.of();

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        final List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertTrue(notificationViews.isEmpty());
    }

    @Test
    public void testReadMissingRestConnection() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.empty()).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        final List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertTrue(notificationViews.isEmpty());
    }

    @Test
    public void testReadException() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doThrow(RuntimeException.class).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        final List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertTrue(notificationViews.isEmpty());

    }

    @Test
    public void testProcess() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties);
        final NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        notificationView.setJson("{ content: \"content is here...\"}");
        final List<AlertNotificationWrapper> notificationContentList = notificationAccumulator.process(List.of(notificationView));
        assertFalse(notificationContentList.isEmpty());
    }

    @Test
    public void testProcessEmptyList() {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties);
        final List<AlertNotificationWrapper> contentList = notificationAccumulator.process(List.of());
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void testWrite() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testBlackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
        final Date creationDate = new Date();
        final NotificationContent content = new MockNotificationContent(creationDate, "BlackDuck", creationDate, "NotificationType", "{content: \"content is here\"}", null).createEntity();
        final List<AlertNotificationWrapper> notificationContentList = Collections.singletonList(content);
        notificationAccumulator.write(notificationContentList);

        Mockito.verify(notificationManager, Mockito.times(notificationContentList.size())).saveAllNotifications(Mockito.any());
    }

    private BlackDuckAccumulator createNonProcessingAccumulator(final BlackDuckProperties blackDuckProperties) {
        return createAccumulator(blackDuckProperties);
    }

    private BlackDuckAccumulator createAccumulator(final BlackDuckProperties blackDuckProperties) {
        return new BlackDuckAccumulator(taskScheduler, blackDuckProperties, notificationManager, filePersistenceUtil, gson, BLACK_DUCK_PROVIDER_KEY);
    }

}
