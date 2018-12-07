package com.synopsys.integration.alert.common.configuration;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;

public class CommonDistributionConfiguration extends Configuration {

    private final String name;
    private final String channelName;
    private final String providerName;
    private final FrequencyType frequencyType;
    private final FormatType formatType;
    private final Set<String> notificationTypes;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider when filtering (NotificationFilter).
    private final Boolean filterByProject;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider.
    private final String projectNamePattern;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider.
    private final Set<String> configuredProject;

    public CommonDistributionConfiguration(final ConfigurationModel configurationModel) {
        super(configurationModel);

        name = getFieldAccessor().getString(CommonDistributionUIConfig.KEY_NAME);
        channelName = getFieldAccessor().getString(CommonDistributionUIConfig.KEY_CHANNEL_NAME);
        providerName = getFieldAccessor().getString(CommonDistributionUIConfig.KEY_PROVIDER_NAME);
        notificationTypes = getFieldAccessor().getAllStrings(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES).stream().collect(Collectors.toSet());
        frequencyType = getFieldAccessor().getEnum(CommonDistributionUIConfig.KEY_FREQUENCY, FrequencyType.class);
        formatType = getFieldAccessor().getEnum(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.class);
        filterByProject = getFieldAccessor().getBoolean(CommonDistributionUIConfig.KEY_FILTER_BY_PROJECT);
        projectNamePattern = getFieldAccessor().getString(CommonDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
        configuredProject = getFieldAccessor().getAllStrings(CommonDistributionUIConfig.KEY_CONFIGURED_PROJECT).stream().collect(Collectors.toSet());
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

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public Set<String> getConfiguredProject() {
        return configuredProject;
    }

}
