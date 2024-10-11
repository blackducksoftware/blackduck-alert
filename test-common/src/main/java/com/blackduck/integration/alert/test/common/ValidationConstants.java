/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.test.common;

import java.util.Map;

import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;

public class ValidationConstants {
    public static final Map<String, FieldValueModel> COMMON_CHANNEL_FIELDS = Map.of(ChannelDescriptor.KEY_CHANNEL_NAME, FieldModelUtils.createFieldValue("Channel Name"),
        ChannelDescriptor.KEY_NAME, FieldModelUtils.createFieldValue("Name"),
        ChannelDescriptor.KEY_FREQUENCY, FieldModelUtils.createFieldValue(FrequencyType.REAL_TIME.toString()),
        ChannelDescriptor.KEY_PROVIDER_TYPE, FieldModelUtils.createFieldValue("Provider Name")
    );

}
