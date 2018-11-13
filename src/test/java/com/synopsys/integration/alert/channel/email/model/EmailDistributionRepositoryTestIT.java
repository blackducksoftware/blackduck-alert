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
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;

public class EmailDistributionRepositoryTestIT extends AlertIntegrationTest {
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
