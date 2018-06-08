package com.blackducksoftware.integration.hub.alert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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

import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.VulnerabilityRepository;
import com.blackducksoftware.integration.hub.alert.enumeration.VulnerabilityOperationEnum;
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
public class NotificationManagerTestIT {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;

    @Autowired
    private NotificationManager notificationManager;

    private NotificationEntity createNotificationEntity(final Date createdAt) {
        final String eventKey = "event_key_for_notification";
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.VULNERABILITY;
        final String projectName = "projectName";
        final String projectVersion = "projectVersion";
        final String componentName = "componentName";
        final String componentVersion = "componentVersion";
        final String policyRuleName = "policyRuleName";
        final String person = "person";
        final String projectUrl = "projectURL";
        final String projectVersionUrl = "projectVersionUrl";
        final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person);
        return entity;
    }

    private NotificationEntity createNotificationEntity() {
        final Date createdAt = createDate(LocalDateTime.now());
        return createNotificationEntity(createdAt);
    }

    private Date createDate(final LocalDateTime localTime) {
        return Date.from(localTime.toInstant(ZoneOffset.UTC));
    }

    public void assertNotificationModel(final NotificationEntity entity, final NotificationModel model) {
        assertEquals(entity.getComponentName(), model.getComponentName());
        assertEquals(entity.getComponentVersion(), model.getComponentVersion());
        assertEquals(entity.getCreatedAt(), model.getCreatedAt());
        assertEquals(entity.getEventKey(), model.getEventKey());
        assertEquals(entity, model.getNotificationEntity());
        assertEquals(entity.getNotificationType(), model.getNotificationType());
        assertEquals(entity.getPolicyRuleName(), model.getPolicyRuleName());
        assertEquals(entity.getPolicyRuleUser(), model.getPolicyRuleUser());
        assertEquals(entity.getProjectName(), model.getProjectName());
        assertEquals(entity.getProjectUrl(), model.getProjectUrl());
        assertEquals(entity.getProjectVersion(), model.getProjectVersion());
        assertEquals(entity.getProjectVersionUrl(), model.getProjectVersionUrl());
    }

    @Before
    public void cleanUpDB() {
        notificationRepository.deleteAll();
        vulnerabilityRepository.deleteAll();
    }

    @Test
    public void testSave() {
        final NotificationEntity notificationEntity = createNotificationEntity();
        final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, null);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vulnerabilityEntity);
        NotificationModel model = new NotificationModel(notificationEntity, vulnerabilityList);
        NotificationModel savedModel = notificationManager.saveNotification(model);

        assertNotNull(savedModel.getNotificationEntity().getId());
        assertNotificationModel(notificationEntity, savedModel);
        assertEquals(vulnerabilityList.size(), model.getVulnerabilityList().size());

        model = new NotificationModel(notificationEntity, null);
        savedModel = notificationManager.saveNotification(model);

        assertNotNull(savedModel.getNotificationEntity().getId());
        assertNotificationModel(notificationEntity, savedModel);
        assertTrue(model.getVulnerabilityList().isEmpty());
    }

    @Test
    public void testFindByIds() {
        final NotificationEntity notificationEntity = createNotificationEntity();
        final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, null);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vulnerabilityEntity);
        final NotificationModel model = new NotificationModel(notificationEntity, vulnerabilityList);
        final NotificationModel savedModel = notificationManager.saveNotification(model);
        final List<Long> notificationIds = Arrays.asList(savedModel.getNotificationEntity().getId());
        final List<NotificationModel> notificationModelList = notificationManager.findByIds(notificationIds);

        assertEquals(1, notificationModelList.size());
    }

    @Test
    public void testFindByIdsInvalidIds() {
        final NotificationEntity notificationEntity = createNotificationEntity();
        final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity("id2", VulnerabilityOperationEnum.ADD, null);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vulnerabilityEntity);
        final NotificationModel model = new NotificationModel(notificationEntity, vulnerabilityList);
        notificationManager.saveNotification(model);

        final List<Long> notificationIds = Arrays.asList(34L, 22L, 10L);
        final List<NotificationModel> notificationModelList = notificationManager.findByIds(notificationIds);
        assertTrue(notificationModelList.isEmpty());
    }

    @Test
    public void findByCreatedAtBetween() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        Date createdAt = createDate(time.minusHours(3));
        NotificationEntity entity = createNotificationEntity(createdAt);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));
        createdAt = createDate(time.plusMinutes(1));
        final NotificationEntity entityToFind1 = createNotificationEntity(createdAt);
        createdAt = createDate(time.plusMinutes(5));
        final NotificationEntity entityToFind2 = createNotificationEntity(createdAt);
        createdAt = createDate(time.plusHours(3));
        entity = createNotificationEntity(createdAt);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));
        notificationManager.saveNotification(new NotificationModel(entityToFind1, Collections.emptyList()));
        notificationManager.saveNotification(new NotificationModel(entityToFind2, Collections.emptyList()));

        final List<NotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertEquals(2, foundList.size());
        assertNotificationModel(entityToFind1, foundList.get(0));
        assertNotificationModel(entityToFind2, foundList.get(1));
    }

    @Test
    public void findByCreatedAtBetweenInvalidDate() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        final Date createdAtEarlier = createDate(time.minusHours(5));
        NotificationEntity entity = createNotificationEntity(createdAtEarlier);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));

        final Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationEntity(createdAtLater);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));

        final List<NotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBefore() {
        final LocalDateTime time = LocalDateTime.now();
        Date searchDate = createDate(time.plusHours(1));
        final Date createdAt = createDate(time.minusHours(5));
        NotificationEntity entity = createNotificationEntity(createdAt);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));
        final Date createdAtLaterThanSearch = createDate(time.plusHours(3));
        entity = createNotificationEntity(createdAtLaterThanSearch);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));

        List<NotificationModel> foundList = notificationManager.findByCreatedAtBefore(searchDate);

        assertEquals(1, foundList.size());

        searchDate = createDate(time.minusHours(6));
        foundList = notificationManager.findByCreatedAtBefore(searchDate);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBeforeDayOffset() {
        final LocalDateTime time = LocalDateTime.now();
        final Date createdAt = createDate(time.minusDays(5));
        NotificationEntity entity = createNotificationEntity(createdAt);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));
        final Date createdAtLaterThanSearch = createDate(time.plusDays(3));
        entity = createNotificationEntity(createdAtLaterThanSearch);
        notificationManager.saveNotification(new NotificationModel(entity, Collections.emptyList()));

        List<NotificationModel> foundList = notificationManager.findByCreatedAtBeforeDayOffset(2);

        assertEquals(1, foundList.size());

        foundList = notificationManager.findByCreatedAtBeforeDayOffset(6);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void testDeleteNotificationList() {
        final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, null);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vulnerabilityEntity);
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        final Date createdAt = createDate(time.minusHours(3));
        NotificationEntity entity = createNotificationEntity(createdAt);
        notificationManager.saveNotification(new NotificationModel(entity, vulnerabilityList));
        Date createdAtInRange = createDate(time.plusMinutes(1));
        final NotificationEntity entityToFind1 = createNotificationEntity(createdAtInRange);
        createdAtInRange = createDate(time.plusMinutes(5));
        final NotificationEntity entityToFind2 = createNotificationEntity(createdAtInRange);
        final Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationEntity(createdAtLater);
        notificationManager.saveNotification(new NotificationModel(entity, vulnerabilityList));
        notificationManager.saveNotification(new NotificationModel(entityToFind1, vulnerabilityList));
        notificationManager.saveNotification(new NotificationModel(entityToFind2, vulnerabilityList));

        final List<NotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);
        assertEquals(4, notificationRepository.count());
        assertEquals(4, vulnerabilityRepository.count());

        notificationManager.deleteNotificationList(foundList);

        assertEquals(2, notificationRepository.count());
        assertEquals(2, vulnerabilityRepository.count());
    }

    @Test
    public void testDeleteNotification() {
        final NotificationEntity notificationEntity = createNotificationEntity();
        final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, null);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vulnerabilityEntity);
        final NotificationModel model = new NotificationModel(notificationEntity, vulnerabilityList);
        final NotificationModel savedModel = notificationManager.saveNotification(model);

        assertEquals(1, notificationRepository.count());
        assertEquals(1, vulnerabilityRepository.count());

        notificationManager.deleteNotification(savedModel);

        assertEquals(0, notificationRepository.count());
        assertEquals(0, vulnerabilityRepository.count());
    }
}
