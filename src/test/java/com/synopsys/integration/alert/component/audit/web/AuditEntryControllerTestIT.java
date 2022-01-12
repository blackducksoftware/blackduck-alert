package com.synopsys.integration.alert.component.audit.web;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.component.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@Transactional
@AlertIntegrationTest
public class AuditEntryControllerTestIT {
    private final String auditUrl = AlertRestConstants.BASE_PATH + "/audit";
    @Autowired
    NotificationContentRepository notificationRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;
    @Autowired
    private AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Autowired
    private JobAccessor jobAccessor;

    @Autowired
    // FIXME why is this class autowired twice?
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private ConfigurationModel providerConfigModel = null;

    MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
    MockNotificationContent mockNotificationContent = new MockNotificationContent();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        cleanup();

        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("My Black Duck Config");

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigModel = configurationModelConfigurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
        mockNotificationContent.setProviderConfigId(providerConfigModel.getConfigurationId());
    }

    @AfterEach
    public void cleanup() {
        auditEntryRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();
        auditNotificationRepository.flush();
        notificationRepository.flush();

        auditEntryRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();

        if (null != providerConfigModel && null != providerConfigModel.getConfigurationId()) {
            configurationModelConfigurationAccessor.deleteConfiguration(providerConfigModel.getConfigurationId());
        }
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetConfig() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(auditUrl)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetConfigWithId() throws Exception {
        AuditEntryEntity entity = mockAuditEntryEntity.createEntity();
        entity = auditEntryRepository.save(entity);

        NotificationEntity notificationContent = mockNotificationContent.createEntity();
        notificationContent = notificationRepository.save(notificationContent);

        auditNotificationRepository.save(new AuditNotificationRelation(entity.getId(), notificationContent.getId()));

        String getUrl = auditUrl + "/" + notificationContent.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetAuditInfoForJob() throws Exception {
        AuditEntryEntity entity = mockAuditEntryEntity.createEntity();
        entity = auditEntryRepository.save(entity);
        String getUrl = auditUrl + "/job/" + entity.getCommonConfigId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testResendNotification() throws Exception {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel job = jobAccessor.createJob(jobRequestModel);

        NotificationEntity notificationEntity = mockNotificationContent.createEntity();
        notificationEntity = notificationRepository.save(notificationEntity);
        mockAuditEntryEntity.setCommonConfigId(job.getJobId());
        AuditEntryEntity auditEntity = mockAuditEntryEntity.createEntity();
        auditEntity = auditEntryRepository.save(auditEntity);
        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));

        String resendUrl = auditUrl + "/resend/" + notificationEntity.getId() + "/";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testResendJobConfig() throws Exception {
        List<ConfigurationFieldModel> slackFields = new ArrayList<>(MockConfigurationModelFactory.createSlackDistributionFields());
        ConfigurationFieldModel providerConfigName = providerConfigModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse(null);
        slackFields.add(providerConfigName);

        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel jobModel = jobAccessor.createJob(jobRequestModel);

        NotificationEntity notificationEntity = mockNotificationContent.createEntity();
        notificationEntity = notificationRepository.save(notificationEntity);
        mockAuditEntryEntity.setCommonConfigId(jobModel.getJobId());
        AuditEntryEntity auditEntity = mockAuditEntryEntity.createEntity();
        auditEntity = auditEntryRepository.save(auditEntity);
        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));

        String resendUrl = auditUrl + "/resend/" + notificationEntity.getId() + "/job/" + auditEntity.getCommonConfigId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private DistributionJobRequestModel createJobRequestModel() {
        SlackJobDetailsModel details = new SlackJobDetailsModel(null, "test_webhook", "#test-channel", null);
        return new DistributionJobRequestModel(
            true,
            "Test Slack Job",
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            mockNotificationContent.getProviderConfigId(),
            false,
            null,
            null,
            List.of("notificationType"),
            List.of(),
            List.of(),
            List.of(),
            details
        );
    }

}
