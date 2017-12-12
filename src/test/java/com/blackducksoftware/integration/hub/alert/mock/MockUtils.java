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
package com.blackducksoftware.integration.hub.alert.mock;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public interface MockUtils<R extends CommonDistributionConfigRestModel, GR extends ConfigRestModel, E extends DatabaseEntity, GE extends DatabaseEntity> {

    public GR createGlobalRestModel();

    public R createRestModel();

    public GE createGlobalEntity();

    public E createEntity();

    public String getGlobalRestModelJson();

    public String getEmptyGlobalRestModelJson();

    public String getRestModelJson();

    public String getEmptyRestModelJson();

    public String getGlobalEntityJson();

    public String getEmptyGlobalEntityJson();

    public String getEntityJson();

    public String getEmptyEntityJson();

}
