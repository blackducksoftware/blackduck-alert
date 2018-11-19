package com.synopsys.integration.alert.workflow.filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.channel.event.NotificationToChannelEventConverter;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackEntity;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.DistributionNotificationTypeRelation;
import com.synopsys.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.processor.MessageContentAggregator;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationToChannelEventConverterTest extends AlertIntegrationTest {
    @Autowired
    private EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;
    @Autowired
    private HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
    @Autowired
    private SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private NotificationFilter notificationFilter;
    @Autowired
    private NotificationToChannelEventConverter notificationToEventConverter;
    @Autowired
    private DistributionNotificationTypeRepository distributionNotificationTypeRepository;
    @Autowired
    private NotificationTypeRepository notificationTypeRepository;
    @Autowired
    private MessageContentAggregator messageContentAggregator;

    @Before
    public void initializeConfig() {
        cleanUp();

        final SlackDistributionConfigEntity slackDistributionConfigEntity = new MockSlackEntity().createEntity();
        final DatabaseEntity slackEntity = slackDistributionRepositoryAccessor.saveEntity(slackDistributionConfigEntity);

        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = new MockHipChatEntity().createEntity();
        final DatabaseEntity hipChatEntity = hipChatDistributionRepositoryAccessor.saveEntity(hipChatDistributionConfigEntity);

        final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = new MockEmailEntity().createEntity();
        final DatabaseEntity emailEntity = emailDistributionRepositoryAccessor.saveEntity(emailGroupDistributionConfigEntity);

        CommonDistributionConfigEntity slackCommonDistributionConfig = new CommonDistributionConfigEntity(slackEntity.getId(), SlackChannel.COMPONENT_NAME, "Slack Config", "provider_blackduck", FrequencyType.REAL_TIME, false,
            "", FormatType.DEFAULT);
        CommonDistributionConfigEntity hipChatCommonDistributionConfig = new CommonDistributionConfigEntity(hipChatEntity.getId(), HipChatChannel.COMPONENT_NAME, "HipChat Config", "provider_blackduck", FrequencyType.REAL_TIME, false,
            "", FormatType.DEFAULT);
        CommonDistributionConfigEntity emailCommonDistributionConfig = new CommonDistributionConfigEntity(emailEntity.getId(), EmailGroupChannel.COMPONENT_NAME, "Email Config", "provider_blackduck", FrequencyType.REAL_TIME, false,
            "", FormatType.DEFAULT);

        slackCommonDistributionConfig = commonDistributionRepository.save(slackCommonDistributionConfig);
        hipChatCommonDistributionConfig = commonDistributionRepository.save(hipChatCommonDistributionConfig);
        emailCommonDistributionConfig = commonDistributionRepository.save(emailCommonDistributionConfig);

        for (final NotificationType notificationCategoryEnum : NotificationType.values()) {
            saveDistributionNotificationTypeRelation(slackCommonDistributionConfig.getId(), notificationCategoryEnum.name());
            saveDistributionNotificationTypeRelation(hipChatCommonDistributionConfig.getId(), notificationCategoryEnum.name());
            saveDistributionNotificationTypeRelation(emailCommonDistributionConfig.getId(), notificationCategoryEnum.name());
        }
    }

    @After
    public void cleanUp() {
        commonDistributionRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
        notificationTypeRepository.deleteAll();
        slackDistributionRepositoryAccessor.deleteAll();
        hipChatDistributionRepositoryAccessor.deleteAll();
        emailDistributionRepositoryAccessor.deleteAll();
    }

    private void saveDistributionNotificationTypeRelation(final Long commonDistributionConfigId, final String notificationType) {
        final DistributionNotificationTypeRelation notificationRelation = new DistributionNotificationTypeRelation(commonDistributionConfigId, notificationType);
        distributionNotificationTypeRepository.save(notificationRelation);
    }

    @Test
    public void createInvalidDigestTypeTest() {
        final NotificationContent notificationModel = createNotificationModel("Project_1", "1.0.0", NotificationType.RULE_VIOLATION);
        final List<NotificationContent> notificationModels = Arrays.asList(notificationModel);

        final Map<? extends CommonDistributionConfig, List<AggregateMessageContent>> jobNotifications = messageContentAggregator.processNotifications(FrequencyType.DAILY, notificationModels);
        final List<DistributionEvent> distributionEvents = notificationToEventConverter.convertToEvents(jobNotifications);
        assertTrue(distributionEvents.isEmpty());
    }

    @Test
    public void createChannelEventTest() {
        final List<CommonDistributionConfigEntity> configEntityList = commonDistributionRepository.findAll();

        final NotificationContent notification_1 = createNotificationModel("Project_1", "1.0.0", NotificationType.RULE_VIOLATION);
        final NotificationContent notification_2 = createNotificationModel("Project_2", "1.0.0", NotificationType.RULE_VIOLATION);
        final NotificationContent notification_3 = createNotificationModel("Project_1", "2.0.0", NotificationType.RULE_VIOLATION);
        final List<NotificationContent> notificationModels = Arrays.asList(notification_1, notification_2, notification_3);

        final Map<? extends CommonDistributionConfig, List<AggregateMessageContent>> jobNotifications = messageContentAggregator.processNotifications(FrequencyType.REAL_TIME, notificationModels);
        final List<DistributionEvent> distributionEvents = notificationToEventConverter.convertToEvents(jobNotifications);
        //assertEquals(configEntityList.size() * filteredNotifications.size(), distributionEvents.size());

        distributionEvents.forEach(event -> {
            assertNotNull(event.getContent());
        });
    }

    private NotificationContent createNotificationModel(final String projectName, final String projectVersion, final NotificationType notificationType) {
        final Date createdAt = Date.from(ZonedDateTime.now().toInstant());

        final NotificationContent model = new NotificationContent(createdAt, "provider", createdAt, notificationType.name(), projectName + projectVersion);
        return model;
    }
}
