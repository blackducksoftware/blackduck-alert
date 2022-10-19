import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { AZURE_BOARD_INFO } from 'page/channel/azure/AzureBoardModel';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';

export const EXISTING_PROVIDERS = {
    [BLACKDUCK_INFO.key]: BLACKDUCK_INFO
};
export const EXISTING_CHANNELS = {
    [AZURE_BOARD_INFO.key]: AZURE_BOARD_INFO,
    [EMAIL_INFO.key]: EMAIL_INFO,
    [JIRA_CLOUD_INFO.key]: JIRA_CLOUD_INFO,
    [JIRA_SERVER_INFO.key]: JIRA_SERVER_INFO,
    [MSTEAMS_INFO.key]: MSTEAMS_INFO,
    [SLACK_INFO.key]: SLACK_INFO
};
