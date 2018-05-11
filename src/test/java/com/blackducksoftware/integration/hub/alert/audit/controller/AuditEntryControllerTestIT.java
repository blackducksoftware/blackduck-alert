package com.blackducksoftware.integration.hub.alert.audit.controller;

import javax.transaction.Transactional;

import org.junit.After;
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

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepository;
import com.blackducksoftware.integration.hub.alert.audit.repository.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;
import com.blackducksoftware.integration.hub.alert.web.controller.BaseController;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class AuditEntryControllerTestIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    AuditEntryRepository auditEntryRepository;

    @Autowired
    CommonDistributionRepository commonDistributionRepository;

    @Autowired
    AuditNotificationRepository auditNotificationRepository;

    @Autowired
    NotificationRepository notificationRepository;

    private MockMvc mockMvc;
    private MockAuditEntryEntity mockAuditEntity;
    private MockCommonDistributionEntity mockCommonDistributionEntity;

    private final String auditUrl = BaseController.BASE_PATH + "/audit";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        auditEntryRepository.deleteAllInBatch();
        commonDistributionRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();

        mockAuditEntity = new MockAuditEntryEntity();
        mockCommonDistributionEntity = new MockCommonDistributionEntity();
    }

    @After
    public void cleanup() {
        auditEntryRepository.deleteAllInBatch();
        commonDistributionRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(auditUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfigWithId() throws Exception {
        AuditEntryEntity entity = mockAuditEntity.createEntity();
        entity = auditEntryRepository.save(entity);
        final String getUrl = auditUrl + "/" + String.valueOf(entity.getId());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(getUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPostConfig() throws Exception {
        CommonDistributionConfigEntity commonEntity = mockCommonDistributionEntity.createEntity();
        final MockNotificationEntity mockNotifications = new MockNotificationEntity();
        NotificationEntity notificationEntity = mockNotifications.createEntity();
        notificationEntity = notificationRepository.save(notificationEntity);
        commonEntity = commonDistributionRepository.save(commonEntity);
        mockAuditEntity.setCommonConfigId(commonEntity.getId());
        AuditEntryEntity auditEntity = mockAuditEntity.createEntity();
        auditEntity = auditEntryRepository.save(auditEntity);
        auditNotificationRepository.save(new AuditNotificationRelation(auditEntity.getId(), notificationEntity.getId()));
        final String resendUrl = auditUrl + "/" + String.valueOf(auditEntity.getId()) + "/" + "/resend";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(resendUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
