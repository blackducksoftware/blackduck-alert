package com.blackducksoftware.integration.alert.database.purge;

import static org.junit.Assert.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.database.DatabaseDataSource;
import com.blackducksoftware.integration.alert.database.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.database.entity.repository.NotificationContentRepository;
import com.blackducksoftware.integration.alert.workflow.scheduled.PurgeTask;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class PurgeJobIT {

    @Autowired
    private PurgeTask purgeTask;
    @Autowired
    private NotificationContentRepository notificationRepository;

    @Before
    public void cleanup() {
        notificationRepository.deleteAll();
    }

    private NotificationContent createNotificationContent(final Date createdAt) {
        return new NotificationContent(createdAt, "provider", "notificationType", "{content: \"content is here...\"}");
    }

    @Test
    public void testGetJobName() {
        assertEquals(PurgeTask.PURGE_JOB_NAME, purgeTask.getJobName());
    }

    @Test
    public void testGetStepName() {
        assertEquals(PurgeTask.PURGE_STEP_NAME, purgeTask.getStepName());
    }

    @Test
    public void testCreateReader() {
        assertNotNull(purgeTask.reader());
    }

    @Test
    public void testCreateWriter() {
        assertNotNull(purgeTask.writer());
    }

    @Test
    public void testCreateProcessor() {
        assertNotNull(purgeTask.processor());
    }

    @Test
    public void testCreateStep() {
        assertNotNull(purgeTask.createStep(purgeTask.reader(), purgeTask.processor(), purgeTask.writer()));
    }

    @Test
    public void testReaderNoData() throws Exception {
        final PurgeReader reader = purgeTask.reader();
        final List<NotificationContent> entityList = reader.read();
        assertNull(entityList);
    }

    @Test
    public void testReaderWithNullRepository() {
        final PurgeReader reader = new PurgeReader(null, 1);
        final List<NotificationContent> entityList = reader.read();
        assertNull(entityList);
    }

    @Test
    public void testReaderWithData() {
        final List<NotificationContent> entityList = new ArrayList<>();
        final String eventKey = "eventKey";
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.VULNERABILITY;
        final String projectName = "ProjectName";
        final String projectUrl = "ProjectUrl";
        final String projectVersion = "ProjectVersion";
        final String projectVersionUrl = "ProjectVersionUrl";
        final String componentName = "ComponentName";
        final String componentVersion = "ComponentVersion";
        final String policyRuleName = "PolicyRuleName";
        final String person = "PolicyPerson";

        ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(2);
        Date createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(3);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.plusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));
        notificationRepository.saveAll(entityList);

        PurgeReader reader = purgeTask.reader();
        List<NotificationContent> resultList = reader.read();

        assertEquals(2, resultList.size());

        notificationRepository.deleteAll();
        entityList.clear();
        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));
        notificationRepository.saveAll(entityList);

        reader = purgeTask.reader();
        resultList = reader.read();

        assertNull(resultList);
    }

    @Test
    public void testDayOffsetReaderWithData() throws Exception {

        final List<NotificationContent> entityList = new ArrayList<>();

        ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        zonedDateTime = zonedDateTime.minusDays(1);
        Date createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(3);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.plusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(createNotificationContent(createdAt));
        notificationRepository.saveAll(entityList);

        final PurgeReader reader = purgeTask.createReaderWithDayOffset(2);
        final List<NotificationContent> resultList = reader.read();

        assertEquals(1, resultList.size());
    }

    @Test
    public void testProcessorNoData() throws Exception {
        final List<NotificationContent> entityList = null;
        final PurgeProcessor processor = purgeTask.processor();
        final List<NotificationContent> resultList = processor.process(entityList);
        assertEquals(entityList, resultList);
    }

    @Test
    public void testProcessorWithData() throws Exception {
        final List<NotificationContent> entityList = new ArrayList<>();
        final PurgeProcessor processor = purgeTask.processor();
        final List<NotificationContent> resultList = processor.process(entityList);
        assertEquals(entityList, resultList);
    }

    @Test
    public void testWriterNoData() throws Exception {
        final List<List<NotificationContent>> itemList = Collections.emptyList();
        final PurgeWriter writer = purgeTask.writer();
        writer.write(itemList);
        assertEquals(0, notificationRepository.count());
    }

    @Test
    public void testWriterNullData() throws Exception {
        final List<List<NotificationContent>> itemList = null;
        final PurgeWriter writer = purgeTask.writer();
        writer.write(itemList);
        assertEquals(0, notificationRepository.count());
    }

    @Test
    public void testWriterWithData() throws Exception {
        final List<NotificationContent> entityList = new ArrayList<>();

        ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(1);
        Date createdAt = Date.from(zonedDateTime.toInstant());
        NotificationContent notification = createNotificationContent(createdAt);
        notificationRepository.save(notification);
        entityList.add(notification);

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(3);
        createdAt = Date.from(zonedDateTime.toInstant());
        notification = createNotificationContent(createdAt);
        notificationRepository.save(notification);
        entityList.add(notification);

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.plusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        notification = createNotificationContent(createdAt);
        notificationRepository.save(notification);
        entityList.add(notification);

        assertEquals(3, notificationRepository.count());
        final List<List<NotificationContent>> itemList = new ArrayList<>();
        itemList.add(entityList);
        final PurgeWriter writer = purgeTask.writer();
        writer.write(itemList);
        assertEquals(0, notificationRepository.count());
    }

}
