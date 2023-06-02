import React from 'react';
import PropTypes from 'prop-types';

function getEventType(event) {
    switch (event) {
        case 'channel_azure_boards':
            return 'Azure Boards';
        case 'channel_email':
            return 'Email';
        case 'channel_jira_cloud':
            return 'Jira Cloud';
        case 'channel_jira_server':
            return 'Jira Server';
        case 'msteamskey':
            return 'MS Teams';
        case 'channel_slack':
            return 'Slack';
        default:
            return 'Unknown';
    }
}

const EventTypeCell = ({ data }) => {
    const { eventType } = data;

    return (
        <>
            {getEventType(eventType)}
        </>
    );
};

EventTypeCell.propTypes = {
    data: PropTypes.object
};

export default EventTypeCell;
