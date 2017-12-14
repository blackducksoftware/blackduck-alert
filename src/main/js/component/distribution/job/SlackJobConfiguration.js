'use strict';
import React from 'react';
import PropTypes from 'prop-types';

import TextInput from '../../../field/input/TextInput';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class SlackJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
	}

    initializeValues(data) {
       super.initializeValues(data);

       let webhook = data.webhook || this.props.webhook;
       let channelUsername = data.channelUsername || this.props.channelUsername;
       let channelName = data.channelName || this.props.channelName;

       super.handleStateValues('webhook', webhook);
       super.handleStateValues('channelUsername', channelUsername);
       super.handleStateValues('channelName', channelName);
    }

	render() {
		let content = <div>
							<TextInput label="Webhook" name="webhook" value={this.props.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.props.webhookError}></TextInput>
							<TextInput label="Channel Name" name="channelName" value={this.props.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.props.channelNameError}></TextInput>
							<TextInput label="Channel Username" name="channelUsername" value={this.props.channelUsername} onChange={this.handleChange} errorName="channelUsernameError" errorValue={this.props.channelUsernameError}></TextInput>
						</div>;

		return super.render(content);
	}
}

SlackJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string
};

SlackJobConfiguration.defaultProps = {
    baseUrl: '/configuration/distribution/slack',
    testUrl: '/configuration/distribution/slack/test',
    distributionType: 'slack_channel'
};
