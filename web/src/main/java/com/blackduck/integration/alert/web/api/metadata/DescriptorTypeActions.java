/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.web.api.metadata.model.DescriptorTypesResponseModel;

@Component
public class DescriptorTypeActions {
    public ActionResponse<DescriptorTypesResponseModel> getAll() {
        return new ActionResponse<>(HttpStatus.OK, DescriptorTypesResponseModel.DEFAULT);
    }

}
