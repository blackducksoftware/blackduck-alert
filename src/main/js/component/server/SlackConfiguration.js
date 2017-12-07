'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import TextInput from '../../field/input/TextInput';
import ServerConfiguration from './ServerConfiguration';

import { alignCenter } from '../../../css/main.css';

class SlackConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
        let content =
            <div>
				<TextInput label="Username" name="username" value={this.state.values.username} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.errors.usernameError}></TextInput>
				<TextInput label="Webhook" name="webhook" value={this.state.values.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.state.errors.webhookError}></TextInput>
			</div>;
        return super.render(content);
	}
};

SlackConfiguration.propTypes = {
    headerText: PropTypes.string,
    externaconfigButtonTest: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string
};

SlackConfiguration.defaultProps = {
    headerText: 'Slack Configuration',
    configButtonTest: 'true',
    baseUrl: '/configuration/slack',
    testUrl: '/configuration/slack/test'
};

export default SlackConfiguration;
