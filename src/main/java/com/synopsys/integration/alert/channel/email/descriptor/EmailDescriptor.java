/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;

@Component
public class EmailDescriptor extends ChannelDescriptor {
    public static final String KEY_SUBJECT_LINE = "subject.line";
    public static final String KEY_PROJECT_OWNER_ONLY = "project.owner.only";
    public static final String KEY_EMAIL_ADDRESSES = "email.addresses";

    @Autowired
    public EmailDescriptor(final EmailGroupChannel channelListener, final EmailGlobalDescriptorActionApi globalRestApi, final EmailGlobalUIConfig emailGlobalUIConfig,
        final EmailDistributionDescriptorActionApi distributionRestApi, final EmailDistributionUIConfig emailDistributionUIConfig) {
        super(EmailGroupChannel.COMPONENT_NAME, EmailGroupChannel.COMPONENT_NAME, channelListener, distributionRestApi, emailDistributionUIConfig, globalRestApi, emailGlobalUIConfig);
    }

    @Override
    public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
        if (ConfigContextEnum.GLOBAL == context) {
            final EnumSet<EmailPropertyKeys> emailPropertyKeys = EnumSet.allOf(EmailPropertyKeys.class);
            emailPropertyKeys.remove(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY);

            final Collection<DefinedFieldModel> fields = createFieldsFromEnum(emailPropertyKeys);

            fields.add(DefinedFieldModel.createGlobalSensitiveField(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()));

            return fields;
        } else if (ConfigContextEnum.DISTRIBUTION == context) {
            final DefinedFieldModel subjectLine = DefinedFieldModel.createDistributionField(KEY_SUBJECT_LINE);
            final DefinedFieldModel projectOwnerOnly = DefinedFieldModel.createDistributionField(KEY_PROJECT_OWNER_ONLY);
            return List.of(subjectLine, projectOwnerOnly);
        }

        return Collections.emptyList();
    }

    private Collection<DefinedFieldModel> createFieldsFromEnum(final EnumSet<EmailPropertyKeys> emailPropertyKeys) {
        return emailPropertyKeys.stream()
                   .map(emailPropertyKey -> DefinedFieldModel.createGlobalField(emailPropertyKey.getPropertyKey()))
                   .collect(Collectors.toList());
    }

}
