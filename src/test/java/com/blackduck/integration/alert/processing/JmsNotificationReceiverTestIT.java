/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.blackduck.integration.alert.Application;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.configuration.ApplicationConfiguration;
import com.blackduck.integration.alert.database.DatabaseDataSource;
import com.blackduck.integration.alert.database.job.api.DefaultConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.database.job.api.DefaultNotificationAccessor;
import com.blackduck.integration.alert.database.job.api.StaticJobAccessor;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.util.DescriptorMocker;
import com.blackduck.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.enumeration.OperationType;
import com.blackduck.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;

//TODO: Need to remove transactional from the AlertIntegrationTest annotation. Once IALERT-2228 is resolved we should make this an @AlertIntegrationTest again
@Tag(TestTags.DEFAULT_INTEGRATION)
@Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("alertdb")
@SpringBootTest
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
class JmsNotificationReceiverTestIT {
    protected TestProperties properties;
    private List<AlertNotificationModel> savedModels;
    private DistributionJobModel distributionJobModel;
    private Long blackDuckGlobalConfigId;

    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private Gson gson;
    @Autowired
    private StaticJobAccessor staticJobAccessor;
    @Autowired
    private DefaultConfigurationModelConfigurationAccessor defaultConfigurationAccessor;
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;

    @BeforeEach
    public void init() {
        properties = new TestProperties();

        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("TRUE");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("blackduck-config");
        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://www.blackduck.com");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("someApiKey");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue(String.valueOf(BlackDuckProperties.DEFAULT_TIMEOUT));

        ConfigurationModel blackduckConfigurationModel = defaultConfigurationAccessor.createConfiguration(blackDuckProviderKey,
            ConfigContextEnum.GLOBAL,
            List.of(providerConfigEnabled,
                providerConfigName,
                blackduckUrl,
                blackduckApiKey,
                blackduckTimeout));
        blackDuckGlobalConfigId = blackduckConfigurationModel.getConfigurationId();

        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        for (Long i = 1L; i <= 1000; i++) {
            notificationContent.add(createAlertNotificationModel(i, false));
        }
        savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);

        SlackJobDetailsModel slackJobDetailsModel = createSlackJobDetailsModel();
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobRequestModel("jobName1", slackJobDetailsModel);
        distributionJobModel = staticJobAccessor.createJob(distributionJobRequestModel);
    }

    @AfterEach
    public void cleanup() {
        savedModels.forEach(defaultNotificationAccessor::deleteNotification);
        staticJobAccessor.deleteJob(distributionJobModel.getJobId());
    }

    @Test
    @Disabled
    void testJms() throws InterruptedException {
        // Set breakpoints throughout this test, there is nothing to assert against here. Suggestions for breakpoints:
        //      Registering listeners: EventListenerConfigurer
        //      Sending events: EventManager
        //      Receiving events: NotificationReceiver or DistributionChannel
        //      Processing notifications: NotificationReceiver
        NotificationReceivedEvent notificationReceivedEvent = new NotificationReceivedEvent(blackDuckGlobalConfigId);
        eventManager.sendEvent(notificationReceivedEvent);

        Thread.sleep(120000);
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(String uniqueJobName, SlackJobDetailsModel slackJobDetailsModel) {
        return new DistributionJobRequestModel(
            true,
            uniqueJobName,
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            UUID.randomUUID(),
            blackDuckGlobalConfigId,
            false,
            ".*",
            null,
            List.of(NotificationType.PROJECT.name()),
            List.of(),
            List.of(),
            List.of(),
            slackJobDetailsModel
        );
    }

    private SlackJobDetailsModel createSlackJobDetailsModel() {
        return new SlackJobDetailsModel(UUID.randomUUID(),
            properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK),
            properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME)
        );
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, boolean processed) {
        ProjectNotificationContent projectNotificationContent = new ProjectNotificationContent();
        projectNotificationContent.setProject("project");
        projectNotificationContent.setProjectName(String.format("projectName-%s", id));
        projectNotificationContent.setOperationType(OperationType.CREATE);
        ProjectNotificationView projectNotificationView = new ProjectNotificationView();
        projectNotificationView.setContent(projectNotificationContent);
        projectNotificationView.setType(NotificationType.PROJECT);
        String content = gson.toJson(projectNotificationView);

        return new AlertNotificationModel(
            id,
            blackDuckGlobalConfigId,
            "provider_blackduck",
            "DELETED CONFIGURATION",
            NotificationType.PROJECT.name(),
            content,
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(),
            processed,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }

}
