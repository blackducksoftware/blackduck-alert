/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(ChannelNameFunctionController.CHANNEL_NAME_FUNCTION_URL)
public class ChannelNameFunctionController extends AbstractFunctionController<LabelValueSelectOptions> {
    public static final String CHANNEL_NAME_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ChannelDistributionUIConfig.KEY_CHANNEL_NAME;

    @Autowired
    public ChannelNameFunctionController(ChannelSelectCustomFunctionAction functionAction) {
        super(functionAction);
    }
}


