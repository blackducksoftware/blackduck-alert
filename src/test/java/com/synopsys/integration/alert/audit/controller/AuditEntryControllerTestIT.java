package com.synopsys.integration.alert.audit.controller;

import java.util.Collection;

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
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldValueRepository;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.controller.BaseController;

public class AuditEntryControllerTestIT extends AlertIntegrationTest {

    private final String auditUrl = BaseController.BASE_PATH + "/audit";
    @Autowired
    NotificationContentRepository notificationRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;
    @Autowired
    private AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private BaseConfigurationAccessor baseConfigurationAccessor;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private MockAuditEntryEntity mockAuditEntity;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        auditEntryRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();

        mockAuditEntity = new MockAuditEntryEntity();
        cleanup();
    }

    public void cleanup() {
        auditEntryRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(auditUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfigWithId() throws Exception {
        AuditEntryEntity entity = mockAuditEntity.createEntity();
        entity = auditEntryRepository.save(entity);
        final String getUrl = auditUrl + "/" + String.valueOf(entity.getId());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPostConfig() throws Exception {
        final Collection<ConfigurationFieldModel> hipChatFields = MockConfigurationModelFactory.createHipChatConfigurationFields();
        final ConfigurationAccessor.ConfigurationModel configurationModel = baseConfigurationAccessor.createConfiguration(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.DISTRIBUTION, hipChatFields);

        final MockNotificationContent mockNotifications = new MockNotificationContent();
        NotificationContent notificationEntity = mockNotifications.createEntity();
        notificationEntity = notificationRepository.save(notificationEntity);
        mockAuditEntity.setCommonConfigId(configurationModel.getConfigurationId());
        AuditEntryEntity auditEntity = mockAuditEntity.createEntity();
        auditEntity = auditEntryRepository.save(auditEntity);
        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));

        final String resendUrl = auditUrl + "/" + String.valueOf(notificationEntity.getId()) + "/" + "/resend";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
