/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;

public interface ValidateController<T> {
    @PostMapping("/validate")
    ValidationResponseModel validate(@RequestBody T requestBody);
}
