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
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorTypesResponseModel;

@Component
public class DescriptorTypeActions {
    public ActionResponse<DescriptorTypesResponseModel> getAll() {
        return new ActionResponse<>(HttpStatus.OK, DescriptorTypesResponseModel.DEFAULT);
    }

}
