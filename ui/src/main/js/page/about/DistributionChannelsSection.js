import React from 'react';
import { NavLink } from 'react-router-dom';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { EXISTING_CHANNELS } from 'common/DescriptorInfo';
import SectionCard from 'common/component/SectionCard';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AZURE_BOARDS_URLS } from 'page/channel/azure/AzureBoardsModel';
import { EMAIL_URLS } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_URLS } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_URLS } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_URLS } from 'page/channel/msteams/MSTeamsModel';
import { SLACK_URLS } from 'page/channel/slack/SlackModels';

const useStyles = createUseStyles(theme => ({
    channelListContainer: {
        padding: '10px',
        borderRadius: '12px',
        display: 'grid',
        gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
        gap: '16px'
    },
    channelItem: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        minWidth: 'fit-content',
        height: '80px',
        borderRadius: '12px',
        backgroundColor: theme.colors.white.default,
        border: ['solid', '2px', theme.colors.borderColor],
        padding: '20px',
        textDecoration: 'none',
        color: 'inherit',
        transitionProperty: 'border, color, box-shadow',
        transitionDuration: '100ms',
        transitionTimingFunction: 'cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
            border: ['solid', '2px', theme.colors.purple.lightPurple],
            color: theme.colors.purple.default,
            boxShadow: `0 1px 3px 0 ${theme.colors.borderColor}, 0 1px 2px -1px ${theme.colors.borderColor}`,
            cursor: 'pointer'
        }
    },
    channelName: {
        fontSize: '18px',
        fontWeight: 'bold'
    },
    channelIcon: {
        color: theme.colors.grey.lightGrey,
        opacity: 0.5
    },
    channelItemDisabled: {
        extend: 'channelItem',
        color: theme.colors.grey.lightGrey,
        '&:hover': {
            border: ['solid', '2px', theme.colors.borderColor],
            color: theme.colors.grey.lightGrey,
            boxShadow: `0 1px 3px 0 ${theme.colors.borderColor}, 0 1px 2px -1px ${theme.colors.borderColor}`,
            cursor: 'not-allowed'
        }
    }
}));

function getIcon(channel) {
    switch (channel) {
        case 'channel_azure_boards':
            return ['fab', 'microsoft'];
        case 'msteamskey':
            return 'users';
        case 'channel_slack':
            return ['fab', 'slack'];
        case 'channel_email':
            return 'envelope';
        case 'channel_jira_cloud':
        case 'channel_jira_server':
            return ['fab', 'jira'];
        default:
            return 'windows';
    }
}

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

function generateChannelData(descriptorMapping, existingData) { 
    return Object.values(descriptorMapping)
        .filter((descriptor) => existingData[descriptor.name])
        .map((descriptor) => {
            const descriptorModel = existingData[descriptor.name];
            const url = descriptor.navigation ? descriptorModel.url : null;
            return {
                name: descriptorModel.label,
                urlName: url,
                icon: getIcon(descriptor.name)
            };
        });
}

const ChannelCard = ({ channel }) => {
    const classes = useStyles();

    if (!channel.urlName) {
        return (
            <div className={classes.channelItemDisabled}>
                <div className={classes.channelName}>
                    {channel.name}
                </div>
                <div className={classes.channelIcon}>
                    <FontAwesomeIcon icon={channel.icon} size="5x"/>
                </div> 
            </div>
        );
    }

    return (
        <NavLink to={getUrl(channel.urlName)} className={classes.channelItem}>
            <div className={classes.channelName}>
                {channel.name}
            </div>
            <div className={classes.channelIcon}>
                <FontAwesomeIcon icon={channel.icon} size="5x"/>
            </div> 
        </NavLink>
    );
}

ChannelCard.propTypes = {
    channel: PropTypes.shape({
        name: PropTypes.string.isRequired,
        urlName: PropTypes.string,
        icon: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.array
        ]).isRequired
    }).isRequired
};

const DistributionChannelsSection = ({ channelDescriptorData }) => {
    const classes = useStyles();
    const channelData = generateChannelData(channelDescriptorData, EXISTING_CHANNELS);

    // If there's no channel data or all channels are missing, don't render the section at all
    if (!channelData || channelData.length <= 0) {
        return null;
    }

    return (
        <>
            <SectionCard
                title="Distribution Channels"
                icon="layer-group"
                description="Available notification channels for Alert"
            >
                <div className={classes.channelListContainer}>
                    {channelData.map((channel) => (
                        <ChannelCard channel={channel} key={channel.name} />
                    ))}
                </div>
            </SectionCard>
        </>
    );
};

DistributionChannelsSection.propTypes = {
    channelDescriptorData: PropTypes.array
};

export default DistributionChannelsSection;
