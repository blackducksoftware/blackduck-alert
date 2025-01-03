/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

/**
 * @deprecated Deprecated in 8.x, planned for removed in 10.0.0.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(EmailAddressFunctionController.EMAIL_ADDRESS_FUNCTION_URL)
public class EmailAddressFunctionController extends AbstractFunctionController<EmailAddressOptions> {
    public static final String EMAIL_ADDRESS_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES;

    @Autowired
    public EmailAddressFunctionController(EmailCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
