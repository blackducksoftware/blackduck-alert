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
package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;

import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class CommonDistributionRestModelTest<R extends CommonDistributionConfigRestModel> extends RestModelTest<R> {

    @Override
    public void testId(final R restModel) {
        assertEquals(String.valueOf(getMockUtil().getId()), restModel.getDistributionConfigId());
    }
}
