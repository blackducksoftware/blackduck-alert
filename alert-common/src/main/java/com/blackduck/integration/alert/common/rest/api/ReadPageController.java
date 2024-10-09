/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public interface ReadPageController<P extends AlertPagedModel<?>> {
    @GetMapping
    P getPage(
        @RequestParam(defaultValue = AlertPagedModel.DEFAULT_PAGE_NUMBER_STRING) Integer pageNumber,
        @RequestParam(defaultValue = AlertPagedModel.DEFAULT_PAGE_SIZE_STRING) Integer pageSize,
        @RequestParam(required = false) String searchTerm,
        @RequestParam(required = false) String sortName,
        @RequestParam(required = false) String sortOrder
    );

}
