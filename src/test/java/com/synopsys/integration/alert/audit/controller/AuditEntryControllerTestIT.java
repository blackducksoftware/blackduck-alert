package com.synopsys.integration.alert.audit.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.web.controller.BaseController;

public class AuditEntryControllerTestIT extends AlertIntegrationTest {

    private final String auditUrl = BaseController.BASE_PATH + "/audit";
    @Autowired
    AuditEntryRepository auditEntryRepository;

    //    @Autowired
    //    CommonDistributionRepository commonDistributionRepository;
    //
    //    @Autowired
    //    HipChatDistributionRepository hipChatDistributionRepository;
    //
    //    @Autowired
    //    AuditNotificationRepository auditNotificationRepository;
    //
    //    @Autowired
    //    NotificationContentRepository notificationRepository;
    //    @Autowired
    //    private WebApplicationContext webApplicationContext;
    //    private MockMvc mockMvc;
    //    private MockAuditEntryEntity mockAuditEntity;
    //    private MockCommonDistributionEntity mockCommonDistributionEntity;
    //    private MockHipChatEntity mockHipChatEntity;
    //
    //    @Before
    //    public void setup() {
    //        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    //
    //        auditEntryRepository.deleteAllInBatch();
    //        commonDistributionRepository.deleteAllInBatch();
    //        hipChatDistributionRepository.deleteAllInBatch();
    //        auditNotificationRepository.deleteAllInBatch();
    //        notificationRepository.deleteAllInBatch();
    //
    //        mockAuditEntity = new MockAuditEntryEntity();
    //        mockCommonDistributionEntity = new MockCommonDistributionEntity();
    //        mockHipChatEntity = new MockHipChatEntity();
    //        cleanup();
    //    }
    //
    //    public void cleanup() {
    //        auditEntryRepository.deleteAllInBatch();
    //        commonDistributionRepository.deleteAllInBatch();
    //        hipChatDistributionRepository.deleteAllInBatch();
    //        auditNotificationRepository.deleteAllInBatch();
    //        notificationRepository.deleteAllInBatch();
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "ADMIN")
    //    public void testGetConfig() throws Exception {
    //        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(auditUrl)
    //                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
    //                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
    //        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "ADMIN")
    //    public void testGetConfigWithId() throws Exception {
    //        AuditEntryEntity entity = mockAuditEntity.createEntity();
    //        entity = auditEntryRepository.save(entity);
    //        final String getUrl = auditUrl + "/" + String.valueOf(entity.getId());
    //        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl)
    //                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
    //                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
    //        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "ADMIN")
    //    public void testPostConfig() throws Exception {
    //        HipChatDistributionConfigEntity hipChatDistributionConfigEntity = mockHipChatEntity.createEntity();
    //        hipChatDistributionConfigEntity = hipChatDistributionRepository.save(hipChatDistributionConfigEntity);
    //
    //        CommonDistributionConfigEntity commonEntity = mockCommonDistributionEntity.createEntity();
    //        commonEntity.setDistributionConfigId(hipChatDistributionConfigEntity.getId());
    //
    //        final MockNotificationContent mockNotifications = new MockNotificationContent();
    //        NotificationContent notificationEntity = mockNotifications.createEntity();
    //        notificationEntity = notificationRepository.save(notificationEntity);
    //        commonEntity = commonDistributionRepository.save(commonEntity);
    //        mockAuditEntity.setCommonConfigId(commonEntity.getId());
    //        AuditEntryEntity auditEntity = mockAuditEntity.createEntity();
    //        auditEntity = auditEntryRepository.save(auditEntity);
    //        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));
    //
    //        final String resendUrl = auditUrl + "/" + String.valueOf(notificationEntity.getId()) + "/" + "/resend";
    //        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl)
    //                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
    //                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
    //        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    //    }
}
