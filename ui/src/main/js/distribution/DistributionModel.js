export const DISTRIBUTION_INFO = {
    url: 'distributionv2',
    label: 'Distribution V2',
    description: 'Create jobs from the channels Alert provides. Double click the row to edit that job.'
};

export const DISTRIBUTION_COMMON_FIELD_KEYS = {
    // Fields in the channel portion of the field model
    enabled: 'channel.common.enabled',
    name: 'channel.common.name',
    channelName: 'channel.common.channel.name',
    frequency: 'channel.common.frequency',
    // Fields in the provider portion of the field model
    providerConfigId: 'provider.common.config.id',
    notificationTypes: 'provider.distribution.notification.types',
    processingType: 'provider.distribution.processing.type',
    // these were originally included in the channel distribution configuration in old versions of alert but they actually pertain to the provider now.
    filterByProject: 'channel.common.filter.by.project',
    projectNamePattern: 'channel.common.project.name.pattern',
    selectedProjects: 'channel.common.configured.project'
};

export const DISTRIBUTION_URLS = {
    distributionConfigUrl: 'alert/jobs/distributionv2/edit'
};
