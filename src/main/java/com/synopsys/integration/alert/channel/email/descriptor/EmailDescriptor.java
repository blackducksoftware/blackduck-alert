/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;

@Component
public class EmailDescriptor extends ChannelDescriptor {
    public static final String KEY_SUBJECT_LINE = "subject.line";
    public static final String KEY_PROJECT_OWNER_ONLY = "project.owner.only";
    public static final String KEY_EMAIL_ADDRESSES = "email.addresses";

    public static final String EMAIL_LABEL = "Email";
    public static final String EMAIL_URL = "email";
    public static final String EMAIL_ICON = "envelope";

    @Autowired
    public EmailDescriptor(final EmailGroupChannel channelListener, final EmailGlobalDescriptorActionApi globalRestApi, final EmailGlobalUIConfig emailGlobalUIConfig,
        final EmailDistributionDescriptorActionApi distributionRestApi, final EmailDistributionUIConfig emailDistributionUIConfig) {
        super(EmailGroupChannel.COMPONENT_NAME, EmailGroupChannel.COMPONENT_NAME, channelListener, distributionRestApi, emailDistributionUIConfig, globalRestApi, emailGlobalUIConfig);
    }

    @Override
    public Set<DefinedFieldModel> getAllDefinedFields(final ConfigContextEnum context) {
        if (ConfigContextEnum.DISTRIBUTION.equals(context)) {
            final Set<DefinedFieldModel> definedFieldModels = super.getAllDefinedFields(context);
            definedFieldModels.add(new DefinedFieldModel(KEY_EMAIL_ADDRESSES, context, false));
            return definedFieldModels;
        }
        return super.getAllDefinedFields(context);
    }
}
