/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.scheduling.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ConfigurationAction;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;

@Component
public class SchedulingConfigurationAction extends ConfigurationAction {
    @Autowired
    protected SchedulingConfigurationAction(SchedulingDescriptorKey schedulingDescriptorKey, SchedulingGlobalApiAction schedulingGlobalApiAction) {
        super(schedulingDescriptorKey);
        addGlobalApiAction(schedulingGlobalApiAction);
    }

}
