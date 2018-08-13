package com.synopsys.integration.alert.audit.controller;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.web.controller.BaseController;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class AuditEntryControllerTestIT {

    private final String auditUrl = BaseController.BASE_PATH + "/audit";
    @Autowired
    AuditEntryRepository auditEntryRepository;

    @Autowired
    CommonDistributionRepository commonDistributionRepository;

    @Autowired
    AuditNotificationRepository auditNotificationRepository;

    @Autowired
    NotificationContentRepository notificationRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private MockAuditEntryEntity mockAuditEntity;
    private MockCommonDistributionEntity mockCommonDistributionEntity;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        auditEntryRepository.deleteAllInBatch();
        commonDistributionRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();

        mockAuditEntity = new MockAuditEntryEntity();
        mockCommonDistributionEntity = new MockCommonDistributionEntity();
        cleanup();
    }

    public void cleanup() {
        auditEntryRepository.deleteAllInBatch();
        commonDistributionRepository.deleteAllInBatch();
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
        CommonDistributionConfigEntity commonEntity = mockCommonDistributionEntity.createEntity();
        final MockNotificationContent mockNotifications = new MockNotificationContent();
        NotificationContent notificationEntity = mockNotifications.createEntity();
        notificationEntity = notificationRepository.save(notificationEntity);
        commonEntity = commonDistributionRepository.save(commonEntity);
        mockAuditEntity.setCommonConfigId(commonEntity.getId());
        AuditEntryEntity auditEntity = mockAuditEntity.createEntity();
        auditEntity = auditEntryRepository.save(auditEntity);
        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));
        final String resendUrl = auditUrl + "/" + String.valueOf(auditEntity.getId()) + "/" + "/resend";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
