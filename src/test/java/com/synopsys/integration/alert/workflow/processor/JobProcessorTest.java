package com.synopsys.integration.alert.workflow.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.distribution.CommonDistributionConfigReader;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.FilterApplier;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category({ IntegrationTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class JobProcessorTest {

    @Autowired
    private List<ProviderDescriptor> providerDescriptors;
    @Autowired
    private CommonDistributionConfigReader commonDistributionConfigReader;
    @Autowired
    private FilterApplier filterApplier;
    @Autowired
    private NotificationFilter notificationFilter;

    @Test
    public void testNoJobProcessing() throws Exception {
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent policyNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, policyContent, NotificationType.RULE_VIOLATION);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final NotificationContent vulnerabilityNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<NotificationContent> notificationContentList = Arrays.asList(policyNotification, vulnerabilityNotification);
        final JobProcessor jobProcessor = new JobProcessor(providerDescriptors, commonDistributionConfigReader, filterApplier, notificationFilter);
        final Map<CommonDistributionConfig, List<TopicContent>> topicContentMap = jobProcessor.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.isEmpty());
    }

    @Test
    public void testJobProcessing() throws Exception {
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent policyNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, policyContent, NotificationType.RULE_VIOLATION);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final NotificationContent vulnerabilityNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<NotificationContent> notificationContentList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> projectList = Arrays.asList("example", "alert-test-project", "alert-test-project-2");
        final List<String> notificationTypesLIst = Arrays.asList(NotificationType.RULE_VIOLATION.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfig jobConfig = createCommonDistributionJob("channel_email", BlackDuckProvider.COMPONENT_NAME, frequencyType.name(), projectList, notificationTypesLIst, FormatType.DEFAULT);

        final CommonDistributionConfigReader spiedReader = Mockito.spy(commonDistributionConfigReader);
        Mockito.when(spiedReader.getPopulatedConfigs()).thenReturn(Arrays.asList(jobConfig));

        final JobProcessor jobProcessor = new JobProcessor(providerDescriptors, spiedReader, filterApplier, notificationFilter);
        final Map<CommonDistributionConfig, List<TopicContent>> topicContentMap = jobProcessor.processNotifications(frequencyType, notificationContentList);

        assertFalse(topicContentMap.isEmpty());
    }

    @Test
    public void testJobProcessingFrequencyMismatch() throws Exception {
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent policyNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, policyContent, NotificationType.RULE_VIOLATION);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final NotificationContent vulnerabilityNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<NotificationContent> notificationContentList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> projectList = Arrays.asList("example", "alert-test-project", "alert-test-project-2");
        final List<String> notificationTypesLIst = Arrays.asList(NotificationType.RULE_VIOLATION.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfig jobConfig = createCommonDistributionJob("channel_email", BlackDuckProvider.COMPONENT_NAME, FrequencyType.DAILY.name(), projectList, notificationTypesLIst, FormatType.DEFAULT);

        final CommonDistributionConfigReader spiedReader = Mockito.spy(commonDistributionConfigReader);
        Mockito.when(spiedReader.getPopulatedConfigs()).thenReturn(Arrays.asList(jobConfig));

        final JobProcessor jobProcessor = new JobProcessor(providerDescriptors, spiedReader, filterApplier, notificationFilter);
        final Map<CommonDistributionConfig, List<TopicContent>> topicContentMap = jobProcessor.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.isEmpty());
    }

    @Test
    public void testJobProcessingProviderMismatch() throws Exception {
        final String unknownProvider = "unknown_provider";
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent policyNotification = createNotification(unknownProvider, policyContent, NotificationType.RULE_VIOLATION);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final NotificationContent vulnerabilityNotification = createNotification(unknownProvider, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<NotificationContent> notificationContentList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> projectList = Arrays.asList("bad-project");
        final List<String> notificationTypesLIst = Arrays.asList(NotificationType.RULE_VIOLATION.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfig jobConfig = createCommonDistributionJob("channel_email", unknownProvider, frequencyType.name(), projectList, notificationTypesLIst, FormatType.DEFAULT);

        final CommonDistributionConfigReader spiedReader = Mockito.spy(commonDistributionConfigReader);
        Mockito.when(spiedReader.getPopulatedConfigs()).thenReturn(Arrays.asList(jobConfig));

        final JobProcessor jobProcessor = new JobProcessor(providerDescriptors, spiedReader, filterApplier, notificationFilter);
        final Map<CommonDistributionConfig, List<TopicContent>> topicContentMap = jobProcessor.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.containsKey(jobConfig));
        assertTrue(topicContentMap.get(jobConfig).isEmpty());
    }

    @Test
    public void testJobProcessingNotificationTypeMismatch() throws Exception {
        final String unknownProvider = "unknown_provider";
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent policyNotification = createNotification(unknownProvider, policyContent, NotificationType.RULE_VIOLATION);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final NotificationContent vulnerabilityNotification = createNotification(unknownProvider, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<NotificationContent> notificationContentList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> projectList = Arrays.asList("bad-project");
        final List<String> notificationTypesLIst = Arrays.asList(NotificationType.RULE_VIOLATION.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfig jobConfig = createCommonDistributionJob("channel_email", BlackDuckProvider.COMPONENT_NAME, frequencyType.name(), projectList, notificationTypesLIst, FormatType.DEFAULT);

        final CommonDistributionConfigReader spiedReader = Mockito.spy(commonDistributionConfigReader);
        Mockito.when(spiedReader.getPopulatedConfigs()).thenReturn(Arrays.asList(jobConfig));

        final JobProcessor jobProcessor = new JobProcessor(providerDescriptors, spiedReader, filterApplier, notificationFilter);
        final Map<CommonDistributionConfig, List<TopicContent>> topicContentMap = jobProcessor.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.containsKey(jobConfig));
        assertTrue(topicContentMap.get(jobConfig).isEmpty());
    }

    private CommonDistributionConfig createCommonDistributionJob(final String distributionType, final String providerName, final String frequency,
        final List<String> configuredProjects, final List<String> notificationTypes, final FormatType formatType) {
        return new CommonDistributionConfig("1L", "1L", distributionType, "Test Distribution Job", providerName, frequency, "true", configuredProjects, notificationTypes, formatType.name());
    }

    private String getNotificationContentFromFile(final String notificationJsonFileName) throws Exception {
        final ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        final File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private NotificationContent createNotification(final String providerName, final String notificationContent, final NotificationType type) {
        return new NotificationContent(Date.from(Instant.now()), providerName, type.name(), notificationContent);
    }
}
