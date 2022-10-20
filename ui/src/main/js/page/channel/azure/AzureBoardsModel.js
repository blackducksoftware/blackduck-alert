export const AZURE_BOARDS_INFO = {
    key: 'channel_azure_boards',
    url: 'azure_boards',
    label: 'Azure Boards'
};

// (temporary) Remove before merging `bs_IALERT-2955_azure-concrete-model-ui`
export const AZURE_GLOBAL_FIELD_KEYS = {
    organization: 'azure.boards.organization.name',
    clientId: 'azure.boards.client.id',
    clientSecret: 'azure.boards.client.secret',
    configureOAuth: 'azure.boards.oauth'
};

// Remove 'UPDATED' prior to merge
export const AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED = {
    organization: 'organizationName',
    appId: 'appId',
    clientSecret: 'clientSecret',
    configureOAuth: 'oAuth',
    name: 'name'
};

export const AZURE_BOARDS_DISTRIBUTION_FIELD_KEYS = {
    comment: 'channel.azure.boards.work.item.comment',
    project: 'channel.azure.boards.project',
    workItemType: 'channel.azure.boards.work.item.type',
    workItemCompleted: 'channel.azure.boards.work.item.completed.state',
    workItemReopen: 'channel.azure.boards.work.item.reopen.state'
};

export const AZURE_BOARDS_URLS = {
    mainUrl: '/alert/channels/azure_boards',
    editUrl: '/alert/channels/azure_boards/edit',
    copyUrl: '/alert/channels/azure_boards/copy'
};