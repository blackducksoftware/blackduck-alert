import React from 'react';
import PropTypes from 'prop-types';

const descriptorTranslation = {
    channel_azure_boards: 'Azure Boards',
    channel_email: 'Email',
    channel_jira_cloud: 'Jira Cloud',
    channel_jira_server: 'Jira Server',
    channel_slack: 'Slack',
    component_audit: 'Audit',
    component_authentication: 'Authentication',
    component_certificates: 'Certificates',
    component_scheduling: 'Scheduling',
    component_settings: 'Settings',
    component_tasks: 'Tasks Management',
    component_users: 'User Management',
    msteamskey: 'MS Teams',
    provider_blackduck: 'Blackduck'
};

const DescriptorNameCell = ({ data }) => {
    const { descriptorName: name } = data;

    if (descriptorTranslation[name]) {
        return (
            <span>
                {descriptorTranslation[name]}
            </span>
        );
    }
    return (
        <span>
            {name}
        </span>
    );
};

DescriptorNameCell.propTypes = {
    data: PropTypes.shape({
        descriptorName: PropTypes.string
    })
};

export default DescriptorNameCell;
