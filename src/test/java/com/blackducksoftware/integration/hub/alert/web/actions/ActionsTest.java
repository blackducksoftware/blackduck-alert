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
package com.blackducksoftware.integration.hub.alert.web.actions;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class ActionsTest<R extends CommonDistributionConfigRestModel, GR extends ConfigRestModel, E extends DatabaseEntity, GE extends DatabaseEntity> {
    private final MockUtils<R, GR, E, GE> mockUtils;
    private final ObjectTransformer objectTransformer;

    public ActionsTest(final MockUtils<R, GR, E, GE> mockUtils) {
        this.mockUtils = mockUtils;
        objectTransformer = new ObjectTransformer();
    }

}
