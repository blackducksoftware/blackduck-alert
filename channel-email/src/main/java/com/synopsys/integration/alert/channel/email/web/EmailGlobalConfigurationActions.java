/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@Component
public class EmailGlobalConfigurationActions {
    public ActionResponse<EmailGlobalConfigResponse> getOne(Long id) {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ActionResponse<EmailGlobalConfigResponse> create(EmailGlobalConfigResponse resource) {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ActionResponse<EmailGlobalConfigResponse> update(Long id, EmailGlobalConfigResponse resource) {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ActionResponse<ValidationResponseModel> validate(EmailGlobalConfigResponse requestBody) {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ActionResponse<EmailGlobalConfigResponse> delete(Long id) {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ActionResponse<ValidationResponseModel> test(EmailGlobalConfigResponse resource) {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
