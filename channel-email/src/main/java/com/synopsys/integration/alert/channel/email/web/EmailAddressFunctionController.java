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

/**
 * @deprecated This class is unused and part of the old Email REST API. It is set for removal in 8.0.0.
 */
@RestController
@RequestMapping(EmailAddressFunctionController.EMAIL_ADDRESS_FUNCTION_URL)
@Deprecated(forRemoval = true)
public class EmailAddressFunctionController extends AbstractFunctionController<EmailAddressOptions> {
    public static final String EMAIL_ADDRESS_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES;

    @Autowired
    public EmailAddressFunctionController(EmailCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
