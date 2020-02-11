package com.synopsys.integration.alert.audit.controller;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

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

import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.controller.BaseController;

@Transactional
public class AuditEntryControllerTestIT extends AlertIntegrationTest {

    private final String auditUrl = BaseController.BASE_PATH + "/audit";
    @Autowired
    NotificationContentRepository notificationRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;
    @Autowired
    private AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private ConfigurationAccessor baseConfigurationAccessor;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        cleanup();
    }

    @AfterEach
    public void cleanup() {
        auditEntryRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetConfig() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(auditUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetConfigWithId() throws Exception {
        MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        AuditEntryEntity entity = mockAuditEntryEntity.createEntity();
        entity = auditEntryRepository.save(entity);

        MockNotificationContent mockNotificationContent = new MockNotificationContent();
        NotificationEntity notificationContent = mockNotificationContent.createEntity();
        notificationContent = notificationRepository.save(notificationContent);

        auditNotificationRepository.save(new AuditNotificationRelation(entity.getId(), notificationContent.getId()));

        String getUrl = auditUrl + "/" + notificationContent.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetAuditInfoForJob() throws Exception {
        AuditEntryEntity entity = new MockAuditEntryEntity().createEntity();
        entity = auditEntryRepository.save(entity);
        String getUrl = auditUrl + "/job/" + entity.getCommonConfigId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testPostConfig() throws Exception {
        Collection<ConfigurationFieldModel> slackFields = MockConfigurationModelFactory.createSlackDistributionFields();
        SlackChannelKey slackChannelKey = new SlackChannelKey();
        ConfigurationModel configurationModel = baseConfigurationAccessor.createConfiguration(slackChannelKey, ConfigContextEnum.DISTRIBUTION, slackFields);
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(UUID.randomUUID(), Set.of(configurationModel));

        MockNotificationContent mockNotifications = new MockNotificationContent();
        NotificationEntity notificationEntity = mockNotifications.createEntity();
        notificationEntity = notificationRepository.save(notificationEntity);
        MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        mockAuditEntryEntity.setCommonConfigId(configurationJobModel.getJobId());
        AuditEntryEntity auditEntity = mockAuditEntryEntity.createEntity();
        auditEntity = auditEntryRepository.save(auditEntity);
        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));

        String resendUrl = auditUrl + "/resend/" + notificationEntity.getId() + "/";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
