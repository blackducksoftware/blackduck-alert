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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;

@Component
public class EmailDistributionUIConfig extends UIConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckDataActions blackDuckDataActions;

    @Autowired
    public EmailDistributionUIConfig(final BlackDuckDataActions blackDuckDataActions) {
        this.blackDuckDataActions = blackDuckDataActions;
    }

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Email", "email", EmailGroupChannel.COMPONENT_NAME, "envelope", setupFields());
    }

    public List<ConfigField> setupFields() {
        final ConfigField subjectLine = new TextInputConfigField("emailSubjectLine", "Subject Line", false, false);
        return Arrays.asList(subjectLine);
    }
}
