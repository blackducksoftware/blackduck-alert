/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@RestController
@RequestMapping(PolicyFilterFunctionController.POLICY_FILTER_FUNCTION_URL)
public class PolicyFilterFunctionController extends AbstractFunctionController<NotificationFilterModelOptions> {
    public static final String POLICY_FILTER_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER;

    @Autowired
    public PolicyFilterFunctionController(PolicyNotificationFilterCustomFunctionAction functionAction) {
        super(functionAction);
    }
}
