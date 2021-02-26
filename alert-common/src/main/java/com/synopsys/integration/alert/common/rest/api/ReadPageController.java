/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ReadPageController<P extends AlertPagedModel<?>> {
    String DEFAULT_PAGE_NUMBER = "0";
    String DEFAULT_PAGE_SIZE = "10";
    
    @GetMapping
    P getPage(
        @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
        @RequestParam(required = false) String searchTerm
    );

}
