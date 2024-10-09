/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.web.api.metadata.model.ConfigContextsResponseModel;

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
