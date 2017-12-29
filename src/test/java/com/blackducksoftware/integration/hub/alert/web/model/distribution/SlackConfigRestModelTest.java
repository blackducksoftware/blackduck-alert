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

import com.blackducksoftware.integration.hub.alert.mock.model.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.RestModelTest;

public class SlackConfigRestModelTest extends RestModelTest<SlackDistributionRestModel> {

    @Override
    public void assertRestModelFieldsNull(final SlackDistributionRestModel restModel) {
        assertNull(restModel.getChannelName());
        assertNull(restModel.getChannelUsername());
        assertNull(restModel.getWebhook());
    }

    @Override
    public long restModelSerialId() {
        return SlackDistributionRestModel.getSerialversionuid();
    }

    @Override
    public int emptyRestModelHashCode() {
        return -1862761851;
    }

    @Override
    public void assertRestModelFieldsFull(final SlackDistributionRestModel restModel) {
        assertEquals(getMockUtil().getWebhook(), restModel.getWebhook());
        assertEquals(getMockUtil().getChannelName(), restModel.getChannelName());
        assertEquals(getMockUtil().getChannelUsername(), restModel.getChannelUsername());
    }

    @Override
    public int restModelHashCode() {
        return 1683888131;
    }

    @Override
    public Class<SlackDistributionRestModel> getRestModelClass() {
        return SlackDistributionRestModel.class;
    }

    @Override
    public MockSlackRestModel getMockUtil() {
        return new MockSlackRestModel();
    }

}
