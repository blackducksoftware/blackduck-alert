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
package com.synopsys.integration.alert.common.action;

import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public abstract class ConfigurationAction {
    private final String descriptorName;
    private final Map<ConfigContextEnum, ApiAction> apiActionMap = new HashMap<>();
    private final Map<ConfigContextEnum, TestAction> testActionMap = new HashMap<>();

    protected ConfigurationAction(final String descriptorName) {
        this.descriptorName = descriptorName;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public void addGlobalApiAction(final ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.GLOBAL, apiAction);
    }

    public void addDistributionApiAction(final ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.DISTRIBUTION, apiAction);
    }

    public void addGlobalTestAction(final TestAction testAction) {
        testActionMap.put(ConfigContextEnum.GLOBAL, testAction);
    }

    public void addDistributionTestAction(final TestAction testAction) {
        testActionMap.put(ConfigContextEnum.DISTRIBUTION, testAction);
    }

    public ApiAction getApiAction(final ConfigContextEnum context) {
        return apiActionMap.get(context);
    }

    public TestAction getTestAction(final ConfigContextEnum context) {
        return testActionMap.get(context);
    }

}
