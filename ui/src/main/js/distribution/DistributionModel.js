import { createTableSelectColumn } from 'field/input/TableSelectInput';

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
    providerName: 'channel.common.provider.name',
    notificationTypes: 'provider.distribution.notification.types',
    processingType: 'provider.distribution.processing.type',
    // these were originally included in the channel distribution configuration in old versions of alert but they actually pertain to the provider now.
    filterByProject: 'channel.common.filter.by.project',
    projectNamePattern: 'channel.common.project.name.pattern',
    configuredProjects: 'channel.common.configured.project',
    policyFilter: 'blackduck.policy.notification.filter',
    vulnerabilitySeverityFilter: 'blackduck.vulnerability.notification.filter'
};

export const DISTRIBUTION_URLS = {
    distributionTableUrl: '/alert/jobs/distributionv2',
    distributionConfigUrl: '/alert/jobs/distributionv2/edit',
    distributionConfigCopyUrl: '/alert/jobs/distributionv2/copy',
    endpointSelectPath: '/api/function'
};

export const DISTRIBUTION_FREQUENCY_OPTIONS = [
    { label: 'Daily', value: 'DAILY' },
    { label: 'Real Time', value: 'REAL_TIME' }
];

export const DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS = [
    { label: 'LICENSE_LIMIT', value: 'LICENSE_LIMIT' },
    { label: 'POLICY_OVERRIDE', value: 'POLICY_OVERRIDE' },
    { label: 'RULE_VIOLATION', value: 'RULE_VIOLATION' },
    { label: 'RULE_VIOLATION_CLEARED', value: 'RULE_VIOLATION_CLEARED' },
    { label: 'VULNERABILITY', value: 'VULNERABILITY' },
    { label: 'BOM_EDIT', value: 'BOM_EDIT' },
    { label: 'PROJECT', value: 'PROJECT' },
    { label: 'PROJECT_VERSION', value: 'PROJECT_VERSION' }
];

export const DISTRIBUTION_PROJECT_SELECT_COLUMNS = [
    createTableSelectColumn('name', 'Project Name', true, true, true),
    createTableSelectColumn('href', 'Project URL', false, false, false),
    createTableSelectColumn('description', 'Project Description', false, false, true)
];

export const DISTRIBUTION_POLICY_SELECT_COLUMNS = [
    createTableSelectColumn('name', 'Name', true, true, true)
];
