/*
 * channel
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
package com.synopsys.integration.alert.channel.email.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class EmailDescriptor extends ChannelDescriptor {
    public static final String EMAIL_PREFIX = "email.";

    public static final String KEY_SUBJECT_LINE = EMAIL_PREFIX + "subject.line";
    public static final String KEY_PROJECT_OWNER_ONLY = "project.owner.only";
    public static final String KEY_EMAIL_ADDRESSES = EMAIL_PREFIX + "addresses";
    public static final String KEY_EMAIL_ADDITIONAL_ADDRESSES = EMAIL_PREFIX + "additional.addresses";
    public static final String KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY = EMAIL_PREFIX + "additional.addresses.only";
    public static final String KEY_EMAIL_ATTACHMENT_FORMAT = EMAIL_PREFIX + "attachment.format";

    public static final String EMAIL_LABEL = "Email";
    public static final String EMAIL_URL = "email";
    public static final String EMAIL_DESCRIPTION = "Configure the email server that Alert will send emails to.";

    @Autowired
    public EmailDescriptor(EmailGlobalUIConfig emailGlobalUIConfig, EmailDistributionUIConfig emailDistributionUIConfig) {
        super(ChannelKeys.EMAIL, emailDistributionUIConfig, emailGlobalUIConfig);
    }

}
