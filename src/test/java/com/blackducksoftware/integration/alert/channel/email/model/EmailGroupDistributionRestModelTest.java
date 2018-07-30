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
package com.blackducksoftware.integration.alert.channel.email.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.alert.web.channel.model.EmailDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionRestModelTest;

public class EmailGroupDistributionRestModelTest extends CommonDistributionRestModelTest<EmailDistributionConfig> {

    @Override
    public void assertRestModelFieldsNull(final EmailDistributionConfig restModel) {
        assertNull(restModel.getGroupName());
    }

    @Override
    public void assertRestModelFieldsFull(final EmailDistributionConfig restModel) {
        assertEquals(getMockUtil().getEmailTemplateLogoImage(), restModel.getEmailTemplateLogoImage());
        assertEquals(getMockUtil().getEmailSubjectLine(), restModel.getEmailSubjectLine());
        assertEquals(getMockUtil().getGroupName(), restModel.getGroupName());
    }

    @Override
    public Class<EmailDistributionConfig> getRestModelClass() {
        return EmailDistributionConfig.class;
    }

    @Override
    public MockEmailRestModel getMockUtil() {
        return new MockEmailRestModel();
    }

}
