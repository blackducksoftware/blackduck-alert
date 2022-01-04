/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.user;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.MultiResponseModel;

public class MultiUserConfigResponseModel extends MultiResponseModel<UserConfig> {
    public MultiUserConfigResponseModel(final List<UserConfig> models) {
        super(models);
    }

    public List<UserConfig> getUsers() {
        return getModels();
    }
}
