import { createTableSelectColumn } from 'common/component/input/TableSelectInput';

export const JIRA_SERVER_INFO = {
    key: 'channel_jira_server',
    url: 'jira_server',
    label: 'Jira Server'
};

export const JIRA_SERVER_GLOBAL_FIELD_KEYS = {
    name: 'name',
    url: 'url',
    username: 'userName',
    password: 'password',
    isPasswordSet: 'isPasswordSet',
    disablePluginCheck: 'disablePluginCheck',
    configurePlugin: 'configurePlugin'
};

export const JIRA_SERVER_DISTRIBUTION_FIELD_KEYS = {
    comment: 'channel.jira.server.add.comments',
    fieldMapping: 'channel.jira.server.field.mapping',
    issueCreator: 'channel.jira.server.issue.creator',
    issueType: 'channel.jira.server.issue.type',
    project: 'channel.jira.server.project.name',
    resolveWorkflow: 'channel.jira.server.resolve.workflow',
    reopenWorkflow: 'channel.jira.server.reopen.workflow',
    issueSummary: 'channel.jira.server.issue.summary'
};

export const JIRA_SERVER_URLS = {
    jiraServerUrl: '/alert/channels/jira_server',
    jiraServerEditUrl: '/alert/channels/jira_server/edit',
    jiraServerCopyUrl: '/alert/channels/jira_server/copy',
    jiraServerConfigUrl: '/alert/api/configuration/jira_server',
    jiraServerPluginUrl: '/alert/api/configuration/jira_server/install-plugin'
};

export const JIRA_SERVER_DISTRIBUTION_GLOBAL_CONFIG_COLUMNS = [
    createTableSelectColumn('id', 'Jira Server Config Id', false, false, false),
    createTableSelectColumn('name', 'Jira Server Name', true, true, true)
];
