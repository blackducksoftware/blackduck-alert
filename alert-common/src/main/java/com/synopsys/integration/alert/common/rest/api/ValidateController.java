/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;

public interface ValidateController<T> {
    @PostMapping("/validate")
    ValidationResponseModel validate(@RequestBody T requestBody);
}
