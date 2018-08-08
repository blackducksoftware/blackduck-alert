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
package com.blackducksoftware.integration.alert.channel.slack.descriptor;

import java.util.Arrays;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

//TODO This class only exists to make Slack appear in the global UI for now. Delete this config once we no longer need slack to appear.
@Component
public class SlackGlobalDescriptorConfig extends DescriptorConfig {

    public SlackGlobalDescriptorConfig() {
        super(null, null);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("Slack", "slack", "slack", Arrays.asList());
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
    }

}
