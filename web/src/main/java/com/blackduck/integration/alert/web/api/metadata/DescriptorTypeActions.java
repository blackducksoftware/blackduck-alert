/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.web.api.metadata.model.DescriptorTypesResponseModel;
import com.synopsys.integration.alert.common.action.ActionResponse;

@Component
public class DescriptorTypeActions {
    public ActionResponse<DescriptorTypesResponseModel> getAll() {
        return new ActionResponse<>(HttpStatus.OK, DescriptorTypesResponseModel.DEFAULT);
    }

}
