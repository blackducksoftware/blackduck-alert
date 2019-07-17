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
package com.synopsys.integration.alert.channel.email.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.action.CustomMessageAction;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.CustomMessageConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailCustomMessageAction extends CustomMessageAction {
    private final EmailActionHelper emailActionHelper;

    @Autowired
    public EmailCustomMessageAction(final EmailChannel emailChannel, final EmailActionHelper emailActionHelper) {
        super(emailChannel);
        this.emailActionHelper = emailActionHelper;
    }

    @Override
    protected DistributionEvent createChannelDistributionEvent(final CustomMessageConfigModel customMessageConfigModel) throws AlertException {
        final DistributionEvent newEvent = super.createChannelDistributionEvent(customMessageConfigModel);
        final FieldAccessor updatedFieldAccessor;
        try {
            updatedFieldAccessor = emailActionHelper.createUpdatedFieldAccessor(newEvent.getFieldAccessor(), newEvent.getDestination());
        } catch (IntegrationException e) {
            throw new AlertException("Problem updating FieldAccessor for email.", e);
        }
        return new DistributionEvent(newEvent.getConfigId(), newEvent.getDestination(), newEvent.getCreatedAt(), newEvent.getProvider(), newEvent.getFormatType(), newEvent.getContent(), updatedFieldAccessor);
    }

}
