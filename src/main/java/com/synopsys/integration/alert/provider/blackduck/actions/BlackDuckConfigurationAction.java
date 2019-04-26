/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckConfigurationAction extends ConfigurationAction {

    @Autowired
    protected BlackDuckConfigurationAction(final BlackDuckGlobalApiAction blackDuckGlobalApiAction, final BlackDuckGlobalTestAction blackDuckGlobalTestAction, final BlackDuckDistributionTestAction blackDuckDistributionTestAction) {
        super(BlackDuckProvider.COMPONENT_NAME);
        addGlobalApiAction(blackDuckGlobalApiAction);
        addGlobalTestAction(blackDuckGlobalTestAction);
        addDistributionTestAction(blackDuckDistributionTestAction);
    }
}
