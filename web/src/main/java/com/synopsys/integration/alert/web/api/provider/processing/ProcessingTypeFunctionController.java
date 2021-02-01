/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.api.provider.processing;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(ProcessingTypeFunctionController.PROCESSING_TYPE_FUNCTION_URL)
public class ProcessingTypeFunctionController extends AbstractFunctionController<LabelValueSelectOptions> {
    public static final String PROCESSING_TYPE_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ProviderDistributionUIConfig.KEY_PROCESSING_TYPE;

    public ProcessingTypeFunctionController(ProcessingSelectCustomFunctionAction functionAction) {
        super(functionAction);
    }
}
