/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.email.action.EmailGlobalCrudActions;
import com.synopsys.integration.alert.channel.email.action.EmailGlobalTestAction;
import com.synopsys.integration.alert.channel.email.action.EmailGlobalValidationAction;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

// FIXME: Need to mask sensitive data. Need to allow any values in Additional properties.

@RestController
@RequestMapping(AlertRestConstants.EMAIL_CONFIGURATION_PATH)
public class EmailGlobalConfigController implements StaticConfigResourceController<EmailGlobalConfigModel>, ValidateController<EmailGlobalConfigModel>, ReadPageController<AlertPagedModel<EmailGlobalConfigModel>> {
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
    public EmailGlobalConfigModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public AlertPagedModel<EmailGlobalConfigModel> getPage(Integer pageNumber, Integer pageSize, String searchTerm) {
        return ResponseFactory.createContentResponseFromAction(configActions.getPaged(pageNumber, pageSize));
    }

    @Override
    public EmailGlobalConfigModel create(EmailGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(UUID id, EmailGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(EmailGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestParam String sendTo, @RequestBody EmailGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(testAction.testWithPermissionCheck(sendTo, resource));
    }

}
