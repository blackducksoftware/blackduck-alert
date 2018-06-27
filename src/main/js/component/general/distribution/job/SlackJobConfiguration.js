import React from 'react';
import PropTypes from 'prop-types';
import TextInput from '../../../../field/input/TextInput';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class SlackJobConfiguration extends BaseJobConfiguration {
    initializeValues(data) {
        super.initializeValues(data);

        const webhook = data.webhook || this.props.webhook;
        const channelUsername = data.channelUsername || this.props.channelUsername;
        const channelName = data.channelName || this.props.channelName;

        super.handleStateValues('webhook', webhook);
        super.handleStateValues('channelUsername', channelUsername);
        super.handleStateValues('channelName', channelName);
    }

    render() {
        const content = (<div>
            <TextInput id="jobSlackWebhook" label="Webhook" name="webhook" value={this.state.values.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.state.errors.webhookError} />
            <TextInput id="jobSlackChannelName" label="Channel Name" name="channelName" value={this.state.values.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.state.errors.channelNameError} />
            <TextInput id="jobSlackChannelUsername" label="Channel Username" name="channelUsername" value={this.state.values.channelUsername} onChange={this.handleChange} errorName="channelUsernameError" errorValue={this.props.channelUsernameError} />
        </div>);

        return super.render(content);
    }
}

SlackJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    csrfToken: PropTypes.string
};

SlackJobConfiguration.defaultProps = {
    baseUrl: '/alert/api/configuration/channel_slack/distribution',
    testUrl: '/alert/api/configuration/channel_slack/distribution',
    distributionType: 'channel_slack'
};
