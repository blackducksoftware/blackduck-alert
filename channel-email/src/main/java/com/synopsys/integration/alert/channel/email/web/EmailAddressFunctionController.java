/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(EmailAddressFunctionController.EMAIL_ADDRESS_FUNCTION_URL)
public class EmailAddressFunctionController extends AbstractFunctionController<EmailAddressOptions> {
    public static final String EMAIL_ADDRESS_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES;

    @Autowired
    public EmailAddressFunctionController(EmailCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
