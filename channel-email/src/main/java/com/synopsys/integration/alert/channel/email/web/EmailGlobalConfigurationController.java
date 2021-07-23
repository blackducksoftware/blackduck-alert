/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.TestController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@RestController
@RequestMapping(AlertRestConstants.EMAIL_CONFIGURATION_PATH)
public class EmailGlobalConfigurationController implements BaseResourceController<EmailGlobalConfigResponse>, TestController<EmailGlobalConfigResponse>, ValidateController<EmailGlobalConfigResponse> {
    private final EmailGlobalConfigurationActions configActions;

    @Autowired
    public EmailGlobalConfigurationController(EmailGlobalConfigurationActions configActions) {
        this.configActions = configActions;
    }

    @Override
    public EmailGlobalConfigResponse getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public EmailGlobalConfigResponse create(EmailGlobalConfigResponse resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(Long id, EmailGlobalConfigResponse resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(EmailGlobalConfigResponse requestBody) {
        return ResponseFactory.createContentResponseFromAction(configActions.validate(requestBody));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel test(EmailGlobalConfigResponse resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.test(resource));
    }

    private FieldModel toFieldModel(EmailGlobalConfigResponse response) {
        HashMap<String, FieldValueModel> responseAsMap = new HashMap<>();

        // TBI
        // There must be some way better than just:
        //
        // responseAsMap.put(
        //     EmailPropertyKeys.JAVAMAIL_WHATEVER_PROPERTY.getPropertyKey(),
        //     FieldValueModelConverter.convert(response.getWhateverProperty())
        // )

        return new FieldModel(ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), responseAsMap);
    }



}
