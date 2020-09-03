/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.api.functions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ActionResult;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelSelectCustomEndpoint;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@RestController
@RequestMapping(ChannelNameFunctionController.CHANNEL_NAME_FUNCTION_URL)
public class ChannelNameFunctionController extends AbstractFunctionController {
    public static final String CHANNEL_NAME_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ChannelDistributionUIConfig.KEY_CHANNEL_NAME;

    private final AuthorizationManager authorizationManager;
    private final ChannelSelectCustomEndpoint functionAction;

    @Autowired
    public ChannelNameFunctionController(AuthorizationManager authorizationManager, ChannelSelectCustomEndpoint functionAction) {
        this.authorizationManager = authorizationManager;
        this.functionAction = functionAction;
    }

    @PostMapping
    public List<LabelValueSelectOption> postConfig(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @RequestBody FieldModel restModel) {
        if (!authorizationManager.hasExecutePermission(restModel.getContext(), restModel.getDescriptorName())) {
            throw ResponseFactory.createForbiddenException();
        }
        List<LabelValueSelectOption> responseContent = List.of();
        HttpServletContentWrapper servletContentWrapper = new HttpServletContentWrapper(httpRequest, httpResponse);
        ActionResult<List<LabelValueSelectOption>> result = functionAction.createResponse(restModel, servletContentWrapper);
        if (result.isSuccessful()) {
            if (result.hasContent()) {
                responseContent = result.getContent().get();
            }
        } else {
            throw ResponseFactory.createStatusException(result);
        }
        return responseContent;
    }
}


