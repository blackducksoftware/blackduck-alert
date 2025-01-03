/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.ConfigResourceController;
import com.blackduck.integration.alert.common.rest.api.TestController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.MultiFieldModel;

/**
 * @deprecated Further development on endpoints should be done with explicit implementations. This controller is planned for removal
 * once all Alert functions are migrated to their own endpoints.
 * Deprecated in 8.x, planned for removed in 10.0.0.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(AlertRestConstants.CONFIGURATION_PATH)
public class ConfigController implements ConfigResourceController, TestController<FieldModel>, ValidateController<FieldModel> {
    private final ConfigActions configActions;

    @Autowired
    public ConfigController(ConfigActions configActions) {
        this.configActions = configActions;
    }

    @GetMapping
    public MultiFieldModel getAll(@RequestParam ConfigContextEnum context, @RequestParam(required = false) String descriptorName) {
        return ResponseFactory.createContentResponseFromAction(configActions.readAllByContextAndDescriptorWithoutChecks(context.name(), descriptorName));
    }

    @Override
    public FieldModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public FieldModel create(FieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(Long id, FieldModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(FieldModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(configActions.validate(requestBody));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel test(FieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.test(resource));
    }
}
