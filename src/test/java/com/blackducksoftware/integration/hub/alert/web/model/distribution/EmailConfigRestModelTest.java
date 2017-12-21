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
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public class EmailConfigRestModelTest extends RestModelTest<EmailGroupDistributionRestModel> {
    private final EmailMockUtils mockUtils = new EmailMockUtils();

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
        return -1130789619;
    }

    @Override
    public void assertRestModelFieldsFull(final EmailGroupDistributionRestModel restModel) {
        assertEquals(mockUtils.getEmailGroup(), restModel.getGroupName());
    }

    @Override
    public int restModelHashCode() {
        return 50228440;
    }

    @Override
    public MockUtils<EmailGroupDistributionRestModel, ?, ?, ?> getMockUtil() {
        return mockUtils;
    }

    @Override
    public Class<EmailGroupDistributionRestModel> getRestModelClass() {
        return EmailGroupDistributionRestModel.class;
    }

}
