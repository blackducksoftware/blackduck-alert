package com.synopsys.integration.alert.workflow.message;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.ApplicationConfiguration;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

//TODO: Need to remove transactional from the AlertIntegrationTest annotation. Once IALERT-2228 is resolved we should make this an @AlertIntegrationTest again
@Tag(TestTags.DEFAULT_INTEGRATION)
@Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("alertdb")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class JmsNotificationReceiverTestIT {
    protected TestProperties properties;

    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;
    @Autowired
    private NotificationReceiver notificationReceiver;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private AuditAccessor auditAccessor;
    @Autowired
    private Gson gson;
    @Autowired
    private StaticJobAccessor staticJobAccessor;

    private List<AlertNotificationModel> savedModels;
    private DistributionJobModel distributionJobModel;
    private DistributionJobModel distributionJobModel2;

    //@BeforeEach
    // Insert into the database a notification //Look at NotificationReceiverTestIt
    // Create 2 Real-time slack jobs <-- look at SlackChannelTest
    @BeforeEach
    public void init() throws Exception {
        //Create a notification and add it into the notificationContentRepository database
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(1L, false));

        savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        //Create Slack Jobs
        properties = new TestProperties(); //TODO may not need this

        SlackJobDetailsModel slackJobDetailsModel = createSlackJobDetailsModel();
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobRequestModel("jobName1", slackJobDetailsModel);
        DistributionJobRequestModel distributionJobRequestModel2 = createDistributionJobRequestModel("jobName2", slackJobDetailsModel);

        distributionJobModel = staticJobAccessor.createJob(distributionJobRequestModel);
        distributionJobModel2 = staticJobAccessor.createJob(distributionJobRequestModel2);
    }

    //@AfterEach
    // Cleanup what we did in the BeforeEach
    // Delete the jobs
    // Clear out the database notifications
    @AfterEach
    public void cleanup() {
        defaultNotificationAccessor.deleteNotificationList(savedModels);
        //notificationContentRepository.flush();
        //notificationContentRepository.deleteAllInBatch();

        staticJobAccessor.deleteJob(distributionJobModel.getJobId());
        staticJobAccessor.deleteJob(distributionJobModel2.getJobId());
    }

    // Autowire EventManager
    // Create a NotificationReceivedEvent (like in the Accumulator) and send through the eventManager.sendEvent

    @Test
    public void testJms() {
        //Set breakpoints throughout this test, there is nothing to assert against here

        //TODO: For debugging, delete later
        List<AlertNotificationModel> alertPagedModel = defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed(10).getModels();

        eventManager.sendEvent(new NotificationReceivedEvent());
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(String uniqueJobName, SlackJobDetailsModel slackJobDetailsModel) {
        return new DistributionJobRequestModel(
            true,
            uniqueJobName,
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            1L,
            false,
            "*",
            List.of(NotificationType.VULNERABILITY.name()), //TODO policy or w/e we decide to choose should be the notification type. This is set in the createAlertNotificationModel
            List.of(),
            List.of(),
            List.of(),
            slackJobDetailsModel
        );
    }

    private SlackJobDetailsModel createSlackJobDetailsModel() {
        return new SlackJobDetailsModel(UUID.randomUUID(),
            properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK),
            properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME),
            properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME)
        );
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, boolean processed) {
        return new AlertNotificationModel(id, 1L, "provider_blackduck", "DELETED CONFIGURATION", NotificationType.VULNERABILITY.name(), "{content: \"content is here...\"}", DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), processed);
    }
}