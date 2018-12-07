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
package com.synopsys.integration.alert.common.descriptor.config.context;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class DescriptorActionApi {

    public abstract void validateConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors);

    public TestConfigModel createTestConfigModel(final FieldModel fieldModel, final String destination) throws AlertFieldException {
        return new TestConfigModel(fieldModel, destination);
    }

    public abstract void testConfig(final TestConfigModel testConfig) throws IntegrationException;

    public DistributionEvent createChannelEvent(final CommonDistributionConfiguration commmonDistributionConfig, final AggregateMessageContent messageContent) {
        return new DistributionEvent(commmonDistributionConfig.getId().toString(), commmonDistributionConfig.getChannelName(), RestConstants.formatDate(new Date()), commmonDistributionConfig.getProviderName(),
            commmonDistributionConfig.getFormatType().name(), messageContent,
            commmonDistributionConfig.getFieldAccessor());
    }

    public DistributionEvent createChannelTestEvent(final FieldModel fieldModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();

        final String channelName = fieldModel.getValue(CommonDistributionUIConfig.KEY_CHANNEL_NAME).orElse("");
        final String providerName = fieldModel.getValue(CommonDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        final String formatType = fieldModel.getValue(ProviderDistributionUIConfig.KEY_FORMAT_TYPE).orElse("");

        final FieldAccessor fieldAccessor = fieldModel.convertToFieldAccessor();

        return new DistributionEvent(fieldModel.getId(), channelName, RestConstants.formatDate(new Date()), providerName, formatType, messageContent, fieldAccessor);
    }

    public AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, Collections.emptyList());

    }

}
