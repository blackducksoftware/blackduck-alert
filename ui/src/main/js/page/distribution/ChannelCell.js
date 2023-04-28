import React from 'react';
import PropTypes from 'prop-types';

const channelTranslation = {
    channel_azure_boards: 'Azure Boards',
    channel_email: 'Email',
    channel_jira_cloud: 'Jira Cloud',
    channel_jira_server: 'Jira Server',
    channel_slack: 'Slack',
    msteamskey: 'MS Teams',
};

const ChannelCell = ({ data }) => {
    const { channelName: name } = data;

    if (channelTranslation[name]) {
        return (
            <span>
                {channelTranslation[name]}
            </span>
        );
    }
    return (
        <span>
            {name}
        </span>
    );
};

ChannelCell.propTypes = {
    data: PropTypes.shape({
        channelName: PropTypes.string
    })
};

export default ChannelCell;
