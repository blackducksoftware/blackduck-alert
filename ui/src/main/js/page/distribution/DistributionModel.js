import { createTableSelectColumn } from 'common/input/TableSelectInput';
import { AZURE_INFO } from 'page/channel/azure/AzureModel';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';

export const DISTRIBUTION_INFO = {
    url: 'distribution',
    label: 'Distribution',
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
    projectVersionNamePattern: 'channel.common.project.version.name.pattern',
    configuredProjects: 'channel.common.configured.project',
    policyFilter: 'blackduck.policy.notification.filter',
    vulnerabilitySeverityFilter: 'blackduck.vulnerability.notification.filter'
};

export const DISTRIBUTION_URLS = {
    distributionTableUrl: '/alert/jobs/distribution',
    distributionConfigUrl: '/alert/jobs/distribution/edit',
    distributionConfigCopyUrl: '/alert/jobs/distribution/copy',
    endpointSelectPath: '/api/function'
};

export const DISTRIBUTION_TEST_FIELD_KEYS = {
    topic: 'channel.common.custom.message.topic',
    message: 'channel.common.custom.message.content'
};

export const DISTRIBUTION_FREQUENCY_OPTIONS = [
    { label: 'Daily', value: 'DAILY' },
    { label: 'Real Time', value: 'REAL_TIME' }
];

export const DISTRIBUTION_NOTIFICATION_TYPE_OPTIONS = [
    { label: 'License Limit', value: 'LICENSE_LIMIT' },
    { label: 'Policy Override', value: 'POLICY_OVERRIDE' },
    { label: 'Rule Violation', value: 'RULE_VIOLATION' },
    { label: 'Rule Violation Cleared', value: 'RULE_VIOLATION_CLEARED' },
    { label: 'Vulnerability', value: 'VULNERABILITY' },
    { label: 'Bom Edit', value: 'BOM_EDIT' },
    { label: 'Project', value: 'PROJECT' },
    { label: 'Project Version', value: 'PROJECT_VERSION' },
    { label: 'Component Unknown Version', value: 'COMPONENT_UNKNOWN_VERSION' }
];

export const DISTRIBUTION_CHANNEL_OPTIONS = [
    { label: AZURE_INFO.label, value: AZURE_INFO.key },
    { label: EMAIL_INFO.label, value: EMAIL_INFO.key },
    { label: JIRA_CLOUD_INFO.label, value: JIRA_CLOUD_INFO.key },
    { label: JIRA_SERVER_INFO.label, value: JIRA_SERVER_INFO.key },
    { label: MSTEAMS_INFO.label, value: MSTEAMS_INFO.key },
    { label: SLACK_INFO.label, value: SLACK_INFO.key }
];

export const DISTRIBUTION_PROCESSING_TYPES = [
    { label: 'Default', value: 'DEFAULT' },
    { label: 'Digest', value: 'DIGEST' },
    { label: 'Summary', value: 'SUMMARY' }
];

export const DISTRIBUTION_PROCESSING_DESCRIPTIONS = {
    DEFAULT: 'The message will contain all the relevant data found in your selected provider.',
    DIGEST: 'The message will contain a delta of the content found in your selected provider since it was last queried."\n Add and Delete operations will cancel each other out depending on the order they occurred.',
    SUMMARY: 'The message contains only a summarized form of the Digest data'
};

export const DISTRIBUTION_VULNERABILITY_SEVERITY_OPTIONS = [
    { label: 'Critical', value: 'CRITICAL' },
    { label: 'High', value: 'HIGH' },
    { label: 'Low', value: 'LOW' },
    { label: 'Medium', value: 'MEDIUM' }
];

export const DISTRIBUTION_PROJECT_SELECT_COLUMNS = [
    createTableSelectColumn('name', 'Project Name', true, true, true),
    createTableSelectColumn('href', 'Project URL', false, false, false),
    createTableSelectColumn('description', 'Project Description', false, false, true)
];

export const DISTRIBUTION_POLICY_SELECT_COLUMNS = [
    createTableSelectColumn('name', 'Name', true, true, true)
];
