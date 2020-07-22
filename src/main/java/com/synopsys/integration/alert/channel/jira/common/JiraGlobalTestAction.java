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
package com.synopsys.integration.alert.channel.jira.common;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class JiraGlobalTestAction extends TestAction {
    protected abstract boolean isAppMissing(FieldAccessor fieldAccessor) throws IntegrationException;

    protected abstract boolean isUserMissing(FieldAccessor fieldAccessor) throws IntegrationException;

    protected abstract String getChannelDisplayName();

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        try {
            if (isUserMissing(registeredFieldValues)) {
                throw new AlertException("User did not match any known users.");
            }

            if (isAppMissing(registeredFieldValues)) {
                throw new AlertException(String.format("Please configure the %s plugin for your server.", getChannelDisplayName()));
            }
        } catch (IntegrationException e) {
            throw new AlertException("An error occurred during testing: " + e.getMessage());
        }
        return new MessageResult(String.format("Successfully connected to %s instance.", getChannelDisplayName()));
    }
}
