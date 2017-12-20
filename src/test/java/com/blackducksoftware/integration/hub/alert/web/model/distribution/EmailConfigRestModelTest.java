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
package com.blackducksoftware.integration.hub.alert.web.model.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.EmailMockUtils;

public class EmailConfigRestModelTest extends RestModelTest<EmailGroupDistributionRestModel> {
    private static final EmailMockUtils mockUtils = new EmailMockUtils();

    public EmailConfigRestModelTest() {
        super(mockUtils, EmailGroupDistributionRestModel.class);
    }

    @Override
    public void assertRestModelFieldsNull(final EmailGroupDistributionRestModel restModel) {
        assertNull(restModel.getGroupName());
    }

    @Override
    public long restModelSerialId() {
        return EmailGroupDistributionRestModel.getSerialversionuid();
    }

    @Override
    public int emptyRestModelHashCode() {
        return 1219584725;
    }

    @Override
    public void assertRestModelFieldsFull(final EmailGroupDistributionRestModel restModel) {
        assertEquals(mockUtils.getEmailGroup(), restModel.getGroupName());
    }

    @Override
    public int restModelHashCode() {
        return 1873006408;
    }

}
