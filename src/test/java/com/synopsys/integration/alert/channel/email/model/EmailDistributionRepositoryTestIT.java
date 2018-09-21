/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel.email.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class EmailDistributionRepositoryTestIT {
    @Autowired
    private EmailGroupDistributionRepository emailGroupDistributionRepository;

    @Before
    public void cleanUpBeforeTest() {
        emailGroupDistributionRepository.deleteAll();
    }

    @Test
    public void saveEntityTestIT() {
        final String emailTemplateLogoImage = "IT Test Logo";
        final String emailSubjectLine = "IT Test Subject Line";
        final boolean projectOwnerOnly = false;
        final EmailGroupDistributionConfigEntity entity = new EmailGroupDistributionConfigEntity(emailTemplateLogoImage, emailSubjectLine, projectOwnerOnly);
        final EmailGroupDistributionConfigEntity savedEntity = emailGroupDistributionRepository.save(entity);
        assertEquals(1, emailGroupDistributionRepository.count());
        assertNotNull(savedEntity.getId());
        assertEquals(emailTemplateLogoImage, savedEntity.getEmailTemplateLogoImage());
        assertEquals(emailSubjectLine, savedEntity.getEmailSubjectLine());
    }

}
