/**
 * channel
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
package com.synopsys.integration.alert.channel.email.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailDistributionTestAction extends ChannelDistributionTestAction {
    private final EmailActionHelper emailActionHelper;

    @Autowired
    public EmailDistributionTestAction(EmailChannel emailChannel, EmailActionHelper emailActionHelper) {
        super(emailChannel);
        this.emailActionHelper = emailActionHelper;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        String destination = fieldModel.getFieldValue(TestAction.KEY_DESTINATION_NAME).orElse("");
        FieldUtility updatedFieldUtility = emailActionHelper.createUpdatedFieldAccessor(registeredFieldValues, destination);
        return super.testConfig(jobId, fieldModel, updatedFieldUtility);
    }

}
