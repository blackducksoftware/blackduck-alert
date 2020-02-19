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
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
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

        TestAlertProperties testAlertProperties = new TestAlertProperties();
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
        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        Date date = new Date();
        assertEquals(RestConstants.formatDate(date), notificationAccumulator.formatDate(date));
    }

    @Test
    public void testCreateDateRange() {
        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        assertNotNull(dateRange);
        ZonedDateTime startTime = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneOffset.UTC);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneOffset.UTC);
        assertNotEquals(dateRange.getStart(), dateRange.getEnd());
        ZonedDateTime expectedStartTime = endTime.minusMinutes(1);
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testCreateDateRangeIOException() throws Exception {
        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        Date expectedStartDate = Date.from(startDateTime.toInstant());
        String startString = notificationAccumulator.formatDate(expectedStartDate);
        filePersistenceUtil.writeToFile(notificationAccumulator.getSearchRangeFileName(), startString);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("Can't read file test exception")).when(spiedAccumulator).readSearchStartTime(Mockito.any());
        DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeParseException() throws Exception {
        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        Date expectedStartDate = Date.from(startDateTime.toInstant());
        String startString = notificationAccumulator.formatDate(expectedStartDate);
        filePersistenceUtil.writeToFile(notificationAccumulator.getSearchRangeFileName(), startString);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new ParseException("Can't parse date test exception", 1)).when(spiedAccumulator).parseDateString(Mockito.any());
        DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeWithExistingFile() throws Exception {
        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        Date expectedStartDate = Date.from(startDateTime.toInstant());
        String startString = notificationAccumulator.formatDate(expectedStartDate);
        filePersistenceUtil.writeToFile(notificationAccumulator.getSearchRangeFileName(), startString);
        DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        assertNotNull(dateRange);
        Date actualStartDate = dateRange.getStart();
        Date actualEndDate = dateRange.getEnd();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    @Test
    public void testRun() {
        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulate() throws Exception {
        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        assertTrue(filePersistenceUtil.exists(spiedAccumulator.getSearchRangeFileName()));
        Mockito.verify(spiedAccumulator, Mockito.times(2)).formatDate(Mockito.any());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateException() throws Exception {
        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("can't write last search file")).when(spiedAccumulator).saveNextSearchStart(Mockito.anyString());
        spiedAccumulator.accumulate();
        assertTrue(filePersistenceUtil.exists(spiedAccumulator.getSearchRangeFileName()));
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateGetNextRunHasValue() throws Exception {
        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
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
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        List<NotificationView> notificationViewList = Arrays.asList(notificationView);

        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getFilteredNotifications(Mockito.any(), Mockito.any(), Mockito.anyList());

        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(mockedBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        DateRange dateRange = spiedAccumulator.createDateRange(spiedAccumulator.getSearchRangeFileName());
        spiedAccumulator.accumulate(dateRange);
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).read(Mockito.any());
        Mockito.verify(spiedAccumulator).process(Mockito.any());
        Mockito.verify(spiedAccumulator).write(Mockito.any());
    }

    @Test
    public void testAccumulateNextRunEmpty() {
        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(testBlackDuckProperties);
        BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).getMillisecondsToNextRun();
    }

    @Test
    public void testRead() throws Exception {
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        List<NotificationView> notificationViewList = Arrays.asList(notificationView);

        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(mockedBlackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://localhost:443/alert"));
        Mockito.when(mockedBlackDuckProperties.getApiToken()).thenReturn("Test Api Key");
        Mockito.when(mockedBlackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);

        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getFilteredNotifications(Mockito.any(), Mockito.any(), Mockito.anyList());

        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());

        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertFalse(notificationViews.isEmpty());
    }

    @Test
    public void testReadNoNotifications() throws Exception {
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        List<NotificationView> notificationViewList = List.of();

        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());

        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertTrue(notificationViews.isEmpty());
    }

    @Test
    public void testReadMissingRestConnection() throws Exception {
        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.empty()).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertTrue(notificationViews.isEmpty());
    }

    @Test
    public void testReadException() throws Exception {
        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doThrow(RuntimeException.class).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());

        BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFileName());
        List<NotificationView> notificationViews = notificationAccumulator.read(dateRange);
        assertTrue(notificationViews.isEmpty());

    }

    @Test
    public void testProcess() throws Exception {
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
    public void testProcessEmptyList() {
        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties);
        List<AlertNotificationModel> contentList = notificationAccumulator.process(List.of());
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void testWrite() {
        BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        notificationAccumulator.setProviderPropertiesForRun(testBlackDuckProperties);
        Date creationDate = new Date();
        AlertNotificationModel content = new AlertNotificationModel(1L, 1L, "BlackDuck", "BlackDuck_1", "NotificationType", "{content: \"content is here\"}", creationDate, creationDate);
        List<AlertNotificationModel> notificationContentList = Collections.singletonList(content);
        notificationAccumulator.write(notificationContentList);

        Mockito.verify(notificationManager, Mockito.times(notificationContentList.size())).saveAllNotifications(Mockito.any());
    }

    private BlackDuckAccumulator createNonProcessingAccumulator(BlackDuckProperties blackDuckProperties) {
        return createAccumulator(blackDuckProperties);
    }

    private BlackDuckAccumulator createAccumulator(BlackDuckProperties blackDuckProperties) {
        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(BLACK_DUCK_PROVIDER_KEY, taskScheduler, notificationManager, filePersistenceUtil);
        accumulator.setProviderPropertiesForRun(blackDuckProperties);
        return accumulator;
    }

}
