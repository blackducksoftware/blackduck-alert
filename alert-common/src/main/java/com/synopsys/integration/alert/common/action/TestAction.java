/**
 * alert-common
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
package com.synopsys.integration.alert.common.action;

import java.util.TreeSet;

import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class TestAction {

    public TestConfigModel createTestConfigModel(final String configId, final FieldAccessor fieldAccessor, final String destination) throws IntegrationException {
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, destination);
        testConfigModel.setConfigId(configId);
        return testConfigModel;
    }

    public abstract void testConfig(final TestConfigModel testConfig) throws IntegrationException;

    public AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Test message sent by Alert", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, new TreeSet<>());
    }
}
