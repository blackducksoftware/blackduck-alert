package com.synopsys.integration.alert.database.channel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationFieldModel;

public class CommonConfigurationModel {
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
    private final Map<String, ConfigurationFieldModel> configurationFieldModelMap;
    private final Set<String> notificationTypes;

    // FIXME this field is here temporarily as there is some tight coupling to the BD provider when filtering (NotificationFilter).
    private final Boolean filterByProject;

    public CommonConfigurationModel(final ConfigurationModel configurationModel) {
        configurationFieldModelMap = configurationModel.getCopyOfKeyToFieldMap();

        id = configurationModel.getConfigurationId();
        name = getFieldValue(KEY_NAME, configurationModel);
        channelName = getFieldValue(KEY_CHANNEL_NAME, configurationModel);
        providerName = getFieldValue(KEY_PROVIDER_NAME, configurationModel);
        notificationTypes = getFieldValues(KEY_NOTIFICATION_TYPES, configurationModel);

        final String frequency = getFieldValue(KEY_FREQUENCY, configurationModel);
        frequencyType = EnumUtils.getEnum(FrequencyType.class, frequency);

        final String format = getFieldValue(KEY_FORMAT_TYPE, configurationModel);
        formatType = EnumUtils.getEnum(FormatType.class, format);

        final String projectFilter = getFieldValue(KEY_FILTER_BY_PROJECT, configurationModel);
        filterByProject = Boolean.parseBoolean(projectFilter);
    }

    private String getFieldValue(final String key, final ConfigurationModel configurationModel) {
        final Optional<ConfigurationFieldModel> field = configurationModel.getField(key);
        if (!field.isPresent()) {
            return "";
        }
        return field.get().getFieldValue().orElse("");
    }

    private Set<String> getFieldValues(final String key, final ConfigurationModel configurationModel) {
        final Optional<ConfigurationFieldModel> field = configurationModel.getField(key);
        if (!field.isPresent()) {
            return Collections.emptySet();
        }
        return field.get().getFieldValues().stream().collect(Collectors.toSet());
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

    public Map<String, ConfigurationFieldModel> getConfigurationFieldModelMap() {
        return configurationFieldModelMap;
    }
}
