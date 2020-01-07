/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.channel.key.ChannelKey;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointSelectField;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;

public abstract class ChannelDistributionUIConfig extends UIConfig {
    public static final String KEY_COMMON_CHANNEL_PREFIX = "channel.common.";

    public static final String KEY_NAME = KEY_COMMON_CHANNEL_PREFIX + "name";
    public static final String KEY_CHANNEL_NAME = KEY_COMMON_CHANNEL_PREFIX + "channel.name";
    public static final String KEY_PROVIDER_NAME = KEY_COMMON_CHANNEL_PREFIX + "provider.name";
    public static final String KEY_FREQUENCY = KEY_COMMON_CHANNEL_PREFIX + "frequency";

    private static final String LABEL_NAME = "Name";
    private static final String LABEL_FREQUENCY = "Frequency";
    private static final String LABEL_CHANNEL_NAME = "Type";
    private static final String LABEL_PROVIDER_NAME = "Provider Type";

    private static final String DESCRIPTION_NAME = "The name of the distribution job. Must be unique.";
    private static final String DESCRIPTION_FREQUENCY = "Select how frequently this job should check for notifications to send.";
    private static final String DESCRIPTION_CHANNEL_NAME = "Select the channel. Notifications generated through Alert will be sent through this channel.";
    private static final String DESCRIPTION_PROVIDER_NAME = "Select the provider. Only notifications for that provider will be processed in this distribution job.";

    private final ChannelKey channelKey;

    public ChannelDistributionUIConfig(ChannelKey channelKey, String label, String urlName) {
        super(label, "Channel distribution setup.", urlName);
        this.channelKey = channelKey;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField channelName = EndpointSelectField.createRequired(KEY_CHANNEL_NAME, LABEL_CHANNEL_NAME, DESCRIPTION_CHANNEL_NAME, true, false, true, false);
        ConfigField name = TextInputConfigField.createRequired(KEY_NAME, LABEL_NAME, DESCRIPTION_NAME);
        ConfigField frequency = SelectConfigField.createRequired(KEY_FREQUENCY, LABEL_FREQUENCY, DESCRIPTION_FREQUENCY, Arrays.stream(FrequencyType.values())
                                                                                                                                  .map(frequencyType -> new LabelValueSelectOption(frequencyType.getDisplayName(), frequencyType.name()))
                                                                                                                                  .sorted()
                                                                                                                                  .collect(Collectors.toList()));
        ConfigField providerName = EndpointSelectField.createRequired(KEY_PROVIDER_NAME, LABEL_PROVIDER_NAME, DESCRIPTION_PROVIDER_NAME, true, false, true, false);

        List<ConfigField> configFields = List.of(channelName, name, frequency, providerName);
        List<ConfigField> channelDistributionFields = createChannelDistributionFields();
        return Stream.concat(configFields.stream(), channelDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createChannelDistributionFields();

    public ChannelKey getChannelKey() {
        return channelKey;
    }

}
