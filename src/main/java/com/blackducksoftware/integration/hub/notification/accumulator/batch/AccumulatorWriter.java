package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.datasource.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.notification.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.notification.processor.PolicyViolationProcessor;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.report.api.PolicyRule;

public class AccumulatorWriter implements ItemWriter<DBStoreEvent> {
    private final static Logger logger = LoggerFactory.getLogger(AccumulatorWriter.class);
    private final NotificationRepository notificationRepository;

    public AccumulatorWriter(final NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void write(final List<? extends DBStoreEvent> itemList) throws Exception {
        itemList.forEach(item -> {
            final List<NotificationEvent> notificationList = item.getNotificationList();
            notificationList.forEach(notification -> {
                final String eventKey = notification.getEventKey();
                final NotificationContentItem content = (NotificationContentItem) notification.getDataSet().get(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT);
                final Date createdAt = content.getCreatedAt();
                final String notificationType = notification.getCategoryType().toString();
                final String projectName = content.getProjectVersion().getProjectName();
                final String projectVersion = content.getProjectVersion().getProjectVersionName();
                final String componentName = content.getComponentName();
                final String componentVersion = content.getComponentVersion().versionName;
                final String policyRuleName = getPolicyRule(notification);

                final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectVersion, componentName, componentVersion, policyRuleName);
                notificationRepository.save(entity);
                logger.info("Writing items: {} ", notification);
                final Iterable<NotificationEntity> entities = notificationRepository.findAll();
                logger.info("Entities: {}", entities);
            });
        });
    }

    private String getPolicyRule(final NotificationEvent notification) {
        final String key = PolicyViolationProcessor.POLICY_RULE;
        if (notification.getDataSet().containsKey(key)) {
            final PolicyRule rule = (PolicyRule) notification.getDataSet().get(key);
            return rule.getName();
        } else {
            return "";
        }
    }
}
