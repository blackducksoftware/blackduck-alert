/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.web.api.metadata.model.ConfigContextsResponseModel;

@Component
public class ContextActions {
    public ActionResponse<ConfigContextsResponseModel> getAll() {
        return new ActionResponse<>(HttpStatus.OK, ConfigContextsResponseModel.DEFAULT);
    }

}
