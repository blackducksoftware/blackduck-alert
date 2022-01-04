/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.TestController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

//@RestController
//@RequestMapping(AlertRestConstants.EMAIL_CONFIGURATION_PATH)
public class EmailGlobalConfigController implements BaseResourceController<EmailGlobalConfigModel>, TestController<EmailGlobalConfigModel>, ValidateController<EmailGlobalConfigModel> {
    private final EmailGlobalConfigActions configActions;

    @Autowired
    public EmailGlobalConfigController(EmailGlobalConfigActions configActions) {
        this.configActions = configActions;
    }

    @Override
    public EmailGlobalConfigModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public EmailGlobalConfigModel create(EmailGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(Long id, EmailGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(EmailGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(configActions.validate(requestBody));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel test(EmailGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.test(resource));
    }

}
