package com.synopsys.integration.alert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.workflow.NotificationManager;

public class NotificationManagerTestITAlert extends AlertIntegrationTest {

    @Autowired
    private NotificationContentRepository notificationContentRepository;

    @Autowired
    private NotificationManager notificationManager;

    private NotificationContent createNotificationContent(final Date createdAt) {
        final MockNotificationContent mockedNotificationContent = new MockNotificationContent(createdAt, "provider", createdAt, "notificationType", "{content: \"content is here...\"}", null);
        return mockedNotificationContent.createEntity();
    }

    private NotificationContent createNotificationContent() {
        final Date createdAt = createDate(LocalDateTime.now());
        return createNotificationContent(createdAt);
    }

    private Date createDate(final LocalDateTime localTime) {
        return Date.from(localTime.toInstant(ZoneOffset.UTC));
    }

    public void assertNotificationModel(final NotificationContent notification, final NotificationContent savedNotification) {
        assertEquals(notification.getCreatedAt(), savedNotification.getCreatedAt());
        assertEquals(notification.getProvider(), savedNotification.getProvider());
        assertEquals(notification.getNotificationType(), savedNotification.getNotificationType());
        assertEquals(notification.getContent(), savedNotification.getContent());
    }

    @Before
    public void cleanUpDB() {
        notificationContentRepository.deleteAll();
    }

    @Test
    public void testSave() {
        final NotificationContent notificationContent = createNotificationContent();
        final NotificationContent savedModel = notificationManager.saveNotification(notificationContent);
        assertNotNull(savedModel.getId());
        assertNotificationModel(notificationContent, savedModel);
    }

    @Test
    public void testFindByIds() {
        final NotificationContent notification = createNotificationContent();
        final NotificationContent savedModel = notificationManager.saveNotification(notification);
        final List<Long> notificationIds = Arrays.asList(savedModel.getId());
        final List<NotificationContent> notificationList = notificationManager.findByIds(notificationIds);

        assertEquals(1, notificationList.size());
    }

    @Test
    public void testFindByIdsInvalidIds() {
        final NotificationContent model = createNotificationContent();
        notificationManager.saveNotification(model);

        final List<Long> notificationIds = Arrays.asList(34L, 22L, 10L);
        final List<NotificationContent> notificationModelList = notificationManager.findByIds(notificationIds);
        assertTrue(notificationModelList.isEmpty());
    }

    @Test
    public void findByCreatedAtBetween() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        Date createdAt = createDate(time.minusHours(3));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        createdAt = createDate(time.plusMinutes(1));
        final NotificationContent entityToFind1 = createNotificationContent(createdAt);
        createdAt = createDate(time.plusMinutes(5));
        final NotificationContent entityToFind2 = createNotificationContent(createdAt);
        createdAt = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        notificationManager.saveNotification(entityToFind1);
        notificationManager.saveNotification(entityToFind2);

        final List<NotificationContent> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

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
        NotificationContent entity = createNotificationContent(createdAtEarlier);
        notificationManager.saveNotification(entity);

        final Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAtLater);
        notificationManager.saveNotification(entity);

        final List<NotificationContent> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBefore() {
        final LocalDateTime time = LocalDateTime.now();
        Date searchDate = createDate(time.plusHours(1));
        final Date createdAt = createDate(time.minusHours(5));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        final Date createdAtLaterThanSearch = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAtLaterThanSearch);
        notificationManager.saveNotification(entity);

        List<NotificationContent> foundList = notificationManager.findByCreatedAtBefore(searchDate);

        assertEquals(1, foundList.size());

        searchDate = createDate(time.minusHours(6));
        foundList = notificationManager.findByCreatedAtBefore(searchDate);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBeforeDayOffset() {
        final LocalDateTime time = LocalDateTime.now();
        final Date createdAt = createDate(time.minusDays(5));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        final Date createdAtLaterThanSearch = createDate(time.plusDays(3));
        entity = createNotificationContent(createdAtLaterThanSearch);
        notificationManager.saveNotification(entity);

        List<NotificationContent> foundList = notificationManager.findByCreatedAtBeforeDayOffset(2);

        assertEquals(1, foundList.size());

        foundList = notificationManager.findByCreatedAtBeforeDayOffset(6);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void testDeleteNotificationList() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        final Date createdAt = createDate(time.minusHours(3));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        Date createdAtInRange = createDate(time.plusMinutes(1));
        final NotificationContent entityToFind1 = createNotificationContent(createdAtInRange);
        createdAtInRange = createDate(time.plusMinutes(5));
        final NotificationContent entityToFind2 = createNotificationContent(createdAtInRange);
        final Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAtLater);
        notificationManager.saveNotification(entity);
        notificationManager.saveNotification(entityToFind1);
        notificationManager.saveNotification(entityToFind2);

        final List<NotificationContent> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);
        assertEquals(4, notificationContentRepository.count());

        notificationManager.deleteNotificationList(foundList);

        assertEquals(2, notificationContentRepository.count());
    }

    @Test
    public void testDeleteNotification() {
        final NotificationContent notificationEntity = createNotificationContent();
        final NotificationContent savedModel = notificationManager.saveNotification(notificationEntity);

        assertEquals(1, notificationContentRepository.count());

        notificationManager.deleteNotification(savedModel);

        assertEquals(0, notificationContentRepository.count());
    }
}
