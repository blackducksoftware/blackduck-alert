import React from 'react';
import PropTypes from 'prop-types';
import { AZURE_BOARDS_URLS } from 'page/channel/azure/AzureBoardsModel';
import { EMAIL_URLS } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_URLS } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_URLS } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_URLS } from 'page/channel/msteams/MSTeamsModel';
import { SLACK_URLS } from 'page/channel/slack/SlackModels';

function getUrl(channel) {
    switch (channel) {
        case 'azure_boards':
            return AZURE_BOARDS_URLS.mainUrl;
        case 'email':
            return EMAIL_URLS.mainUrl;
        case 'jira':
            return JIRA_CLOUD_URLS.mainUrl;
        case 'jira_server':
            return JIRA_SERVER_URLS.jiraServerUrl;
        case 'msteams':
            return MSTEAMS_URLS.mainUrl;
        case 'slack':
            return SLACK_URLS.mainUrl;
        default:
            return '#';
    }
}

const AboutChannelCell = ({ data }) => (
    <a href={getUrl(data.urlName)}>
        {data.name}
    </a>
);

AboutChannelCell.propTypes = {
    data: PropTypes.shape({
        name: PropTypes.string,
        urlName: PropTypes.string
    })
};

export default AboutChannelCell;
