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

import com.blackducksoftware.integration.hub.alert.mock.model.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.RestModelTest;

public class EmailConfigRestModelTest extends RestModelTest<EmailGroupDistributionRestModel> {

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
        assertEquals(getMockUtil().getGroupName(), restModel.getGroupName());
    }

    @Override
    public int restModelHashCode() {
        return -1858260894;
    }

    @Override
    public Class<EmailGroupDistributionRestModel> getRestModelClass() {
        return EmailGroupDistributionRestModel.class;
    }

    @Override
    public MockEmailRestModel getMockUtil() {
        return new MockEmailRestModel();
    }

}
