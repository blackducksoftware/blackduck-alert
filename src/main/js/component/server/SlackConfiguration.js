'use strict';
import React from 'react';

import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../ConfigButtons';
import Configuration from '../Configuration';

import { alignCenter } from '../../../css/main.css';

export default class SlackConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}

	render() {
		return (
				<div>
					<h1 className={alignCenter}>Slack Configuration</h1>
					<TextInput label="Username" name="username" value={this.state.values.username} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.errors.usernameError}></TextInput>
					<TextInput label="Webhook" name="webhook" value={this.state.values.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.state.errors.webhookError}></TextInput>

					<ConfigButtons includeTest="true" onClick={this.handleSubmit.bind(this)} onTestClick={this.handleTestSubmit.bind(this)} />
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
