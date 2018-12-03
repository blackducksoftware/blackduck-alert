package com.synopsys.integration.alert.common.configuration;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor.ConfigurationModel;

public class CommonDistributionConfiguration {
    public static final String KEY_NAME = "channel.common.name";
    public static final String KEY_CHANNEL_NAME = "channel.common.channel.name";
    public static final String KEY_PROVIDER_NAME = "channel.common.provider.name";
    public static final String KEY_FREQUENCY = "channel.common.frequency";
    public static final String KEY_FORMAT_TYPE = "channel.common.format.type";
    public static final String KEY_NOTIFICATION_TYPES = "channel.common.notification.types";
    public static final String KEY_FILTER_BY_PROJECT = "channel.common.filter.by.project";

    private final Long id;
    private final String name;
    private final String channelName;
    private final String providerName;
    private final FrequencyType frequencyType;
    private final FormatType formatType;
    private final Set<String> notificationTypes;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider when filtering (NotificationFilter).
    private final Boolean filterByProject;
    private final FieldAccessor fieldAccessor;

    public CommonDistributionConfiguration(final ConfigurationModel configurationModel) {
        fieldAccessor = new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap());

        id = configurationModel.getConfigurationId();
        name = fieldAccessor.getString(KEY_NAME);
        channelName = fieldAccessor.getString(KEY_CHANNEL_NAME);
        providerName = fieldAccessor.getString(KEY_PROVIDER_NAME);
        notificationTypes = fieldAccessor.getAllStrings(KEY_NOTIFICATION_TYPES).stream().collect(Collectors.toSet());
        frequencyType = fieldAccessor.getEnum(KEY_FREQUENCY, FrequencyType.class);
        formatType = fieldAccessor.getEnum(KEY_FORMAT_TYPE, FormatType.class);
        filterByProject = fieldAccessor.getBoolean(KEY_FILTER_BY_PROJECT);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getProviderName() {
        return providerName;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public FormatType getFormatType() {
        return formatType;
    }

    public Collection<String> getNotificationTypes() {
        return notificationTypes;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }
}
