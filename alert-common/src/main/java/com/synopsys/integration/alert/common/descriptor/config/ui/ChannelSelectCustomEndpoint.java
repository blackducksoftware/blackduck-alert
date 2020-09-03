/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResult;
import com.synopsys.integration.alert.common.action.endpoint.CustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class ChannelSelectCustomEndpoint extends CustomEndpoint<List<LabelValueSelectOption>> {
    private DescriptorMap descriptorMap;
    private AuthorizationManager authorizationManager;

    @Autowired
    public ChannelSelectCustomEndpoint(DescriptorMap descriptorMap, AuthorizationManager authorizationManager) {
        super(authorizationManager);
        this.descriptorMap = descriptorMap;
        this.authorizationManager = authorizationManager;
    }

    @Override
    public ActionResult<List<LabelValueSelectOption>> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        List<LabelValueSelectOption> content = descriptorMap.getDescriptorByType(DescriptorType.CHANNEL).stream()
                                                   .filter(this::hasPermission)
                                                   .map(descriptor -> descriptor.getUIConfig(ConfigContextEnum.DISTRIBUTION))
                                                   .flatMap(Optional::stream)
                                                   .map(uiConfig -> (ChannelDistributionUIConfig) uiConfig)
                                                   .map(channelDistributionUIConfig -> new LabelValueSelectOption(channelDistributionUIConfig.getLabel(), channelDistributionUIConfig.getChannelKey().getUniversalKey()))
                                                   .sorted()
                                                   .collect(Collectors.toList());
        return new ActionResult<>(HttpStatus.OK, content);
    }

    private boolean hasPermission(Descriptor descriptor) {
        return authorizationManager.hasPermissions(ConfigContextEnum.DISTRIBUTION.name(), descriptor.getDescriptorKey().getUniversalKey());
    }
}
