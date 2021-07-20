package com.synopsys.integration.alert.test.common;

import java.util.Map;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class ValidationConstants {
    public static final Map<String, FieldValueModel> COMMON_CHANNEL_FIELDS = Map.of(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, FieldModelUtils.createFieldValue("Channel Name"),
        ChannelDistributionUIConfig.KEY_NAME, FieldModelUtils.createFieldValue("Name"),
        ChannelDistributionUIConfig.KEY_FREQUENCY, FieldModelUtils.createFieldValue(FrequencyType.REAL_TIME.toString()),
        ChannelDistributionUIConfig.KEY_PROVIDER_NAME, FieldModelUtils.createFieldValue("Provider Name")
    );
}
