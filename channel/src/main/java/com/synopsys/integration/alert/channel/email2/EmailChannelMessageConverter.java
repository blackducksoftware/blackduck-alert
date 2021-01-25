/**
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
package com.synopsys.integration.alert.channel.email2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopys.integration.alert.channel.api.convert.AbstractChannelMessageConverter;

@Component
public class EmailChannelMessageConverter extends AbstractChannelMessageConverter<EmailJobDetailsModel, EmailChannelMessageModel> {
    @Autowired
    public EmailChannelMessageConverter(EmailChannelMessageFormatter emailChannelMessageFormatter) {
        super(emailChannelMessageFormatter);
    }

    @Override
    protected List<EmailChannelMessageModel> convertSimpleMessageToChannelMessages(EmailJobDetailsModel distributionDetails, SimpleMessage simpleMessage, List<String> messageChunks) {
        // FIXME implement
        return null;
    }

    @Override
    protected List<EmailChannelMessageModel> convertProjectMessageToChannelMessages(EmailJobDetailsModel distributionDetails, ProjectMessage projectMessage, List<String> messageChunks) {
        // FIXME implement
        return null;
    }

}
