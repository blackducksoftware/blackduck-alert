package com.blackducksoftware.integration.hub.alert.datasource.purge;

import static org.junit.Assert.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
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

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class PurgeJobIT {

    @Autowired
    private PurgeConfig purgeConfig;
    @Autowired
    private NotificationRepositoryWrapper notificationRepository;

    @Before
    public void cleanup() {
        notificationRepository.deleteAll();
    }

    @Test
    public void testGetJobName() {
        assertEquals(PurgeConfig.PURGE_JOB_NAME, purgeConfig.getJobName());
    }

    @Test
    public void testGetStepName() {
        assertEquals(PurgeConfig.PURGE_STEP_NAME, purgeConfig.getStepName());
    }

    @Test
    public void testCreateReader() {
        assertNotNull(purgeConfig.reader());
    }

    @Test
    public void testCreateWriter() {
        assertNotNull(purgeConfig.writer());
    }

    @Test
    public void testCreateProcessor() {
        assertNotNull(purgeConfig.processor());
    }

    @Test
    public void testCreateStep() {
        assertNotNull(purgeConfig.createStep(purgeConfig.reader(), purgeConfig.processor(), purgeConfig.writer()));
    }

    @Test
    public void testReaderNoData() throws Exception {
        final PurgeReader reader = purgeConfig.reader();
        final List<NotificationModel> entityList = reader.read();
        assertNull(entityList);
    }

    @Test
    public void testReaderWithNullRepository() throws Exception {
        final PurgeReader reader = new PurgeReader(null, 1);
        final List<NotificationModel> entityList = reader.read();
        assertNull(entityList);
    }

    @Test
    public void testReaderWithData() throws Exception {
        final List<NotificationEntity> entityList = new ArrayList<>();
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
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(3);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.plusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));
        notificationRepository.save(entityList);

        PurgeReader reader = purgeConfig.reader();
        List<NotificationModel> resultList = reader.read();

        assertEquals(2, resultList.size());

        notificationRepository.deleteAll();
        entityList.clear();
        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));
        notificationRepository.save(entityList);

        reader = purgeConfig.reader();
        resultList = reader.read();

        assertNull(resultList);
    }

    @Test
    public void testDayOffsetReaderWithData() throws Exception {

        final List<NotificationEntity> entityList = new ArrayList<>();
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
        zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        zonedDateTime = zonedDateTime.minusDays(1);
        Date createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(3);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.plusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        entityList.add(new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person));
        notificationRepository.save(entityList);

        final PurgeReader reader = purgeConfig.createReaderWithDayOffset(2);
        final List<NotificationModel> resultList = reader.read();

        assertEquals(1, resultList.size());
    }

    @Test
    public void testProcessorNoData() throws Exception {
        final List<NotificationModel> entityList = null;
        final PurgeProcessor processor = purgeConfig.processor();
        final List<NotificationModel> resultList = processor.process(entityList);
        assertEquals(entityList, resultList);
    }

    @Test
    public void testProcessorWithData() throws Exception {
        final List<NotificationModel> entityList = new ArrayList<>();
        entityList.add(new NotificationModel(null, null));
        entityList.add(new NotificationModel(null, null));
        entityList.add(new NotificationModel(null, null));
        entityList.add(new NotificationModel(null, null));
        entityList.add(new NotificationModel(null, null));
        final PurgeProcessor processor = purgeConfig.processor();
        final List<NotificationModel> resultList = processor.process(entityList);
        assertEquals(entityList, resultList);
    }

    @Test
    public void testWriterNoData() throws Exception {
        final List<List<NotificationModel>> itemList = Collections.emptyList();
        final PurgeWriter writer = purgeConfig.writer();
        writer.write(itemList);
        assertEquals(0, notificationRepository.count());
    }

    @Test
    public void testWriterNullData() throws Exception {
        final List<List<NotificationModel>> itemList = null;
        final PurgeWriter writer = purgeConfig.writer();
        writer.write(itemList);
        assertEquals(0, notificationRepository.count());
    }

    @Test
    public void testWriterWithData() throws Exception {
        final List<NotificationModel> entityList = new ArrayList<>();
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
        final Collection<VulnerabilityEntity> vulnerabilityList = null;

        ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(1);
        Date createdAt = Date.from(zonedDateTime.toInstant());
        NotificationEntity notification = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person);
        notificationRepository.save(notification);
        entityList.add(new NotificationModel(notification, vulnerabilityList));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.minusDays(3);
        createdAt = Date.from(zonedDateTime.toInstant());
        notification = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person);
        notificationRepository.save(notification);
        entityList.add(new NotificationModel(notification, vulnerabilityList));

        zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        zonedDateTime = zonedDateTime.plusDays(1);
        createdAt = Date.from(zonedDateTime.toInstant());
        notification = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person);
        notificationRepository.save(notification);
        entityList.add(new NotificationModel(notification, vulnerabilityList));

        assertEquals(3, notificationRepository.count());
        final List<List<NotificationModel>> itemList = new ArrayList<>();
        itemList.add(entityList);
        final PurgeWriter writer = purgeConfig.writer();
        writer.write(itemList);
        assertEquals(0, notificationRepository.count());
    }

}
