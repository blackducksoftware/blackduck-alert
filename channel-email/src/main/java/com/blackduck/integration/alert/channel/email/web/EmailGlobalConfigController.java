/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.email.action.EmailGlobalCrudActions;
import com.blackduck.integration.alert.channel.email.action.EmailGlobalTestAction;
import com.blackduck.integration.alert.channel.email.action.EmailGlobalValidationAction;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;

@RestController
@RequestMapping(AlertRestConstants.EMAIL_CONFIGURATION_PATH)
public class EmailGlobalConfigController implements StaticUniqueConfigResourceController<EmailGlobalConfigModel>, ValidateController<EmailGlobalConfigModel> {
    private final EmailGlobalCrudActions configActions;
    private final EmailGlobalValidationAction validationAction;
    private final EmailGlobalTestAction testAction;

    @Autowired
    public EmailGlobalConfigController(EmailGlobalCrudActions configActions, EmailGlobalValidationAction validationAction, EmailGlobalTestAction testAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
        this.testAction = testAction;
    }

    @Override
    public EmailGlobalConfigModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @Override
    public EmailGlobalConfigModel create(EmailGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(EmailGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(resource));
    }

    @Override
    public void delete() {
        ResponseFactory.createContentResponseFromAction(configActions.delete());
    }

    @Override
    public ValidationResponseModel validate(EmailGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestParam String sendTo, @RequestBody EmailGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(testAction.testWithPermissionCheck(sendTo, resource));
    }

}
