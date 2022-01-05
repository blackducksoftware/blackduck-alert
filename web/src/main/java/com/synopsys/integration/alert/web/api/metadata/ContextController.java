/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.api.metadata.model.ConfigContextsResponseModel;

@RestController
@RequestMapping(ContextController.CONTEXTS_PATH)
public class ContextController {
    public static final String CONTEXTS_PATH = MetadataControllerConstants.METADATA_BASE_PATH + "/contexts";
    private ContextActions actions;

    @Autowired
    public ContextController(ContextActions actions) {
        this.actions = actions;
    }

    @GetMapping
    public ConfigContextsResponseModel getContexts() {
        return ResponseFactory.createContentResponseFromAction(actions.getAll());
    }
}
