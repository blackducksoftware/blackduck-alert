'use strict';
import React from 'react';

import Field from '../field/Field';
import ConfigButtons from './ConfigButtons';
import Configuration from './Configuration';

import { alignCenter } from '../../css/main.css';

export default class SlackConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
				<div>
					<h1 className={alignCenter}>Slack Configuration</h1>
					<Field label="Channel Name" type="text" name="channelName" value={this.state.values.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.state.errors.channelNameError}></Field>
					<Field label="Username" type="text" name="username" value={this.state.values.username} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.errors.usernameError}></Field>
					<Field label="Webhook" type="text" name="webhook" value={this.state.values.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.state.errors.webhookError}></Field>
					
					<ConfigButtons includeTest="true" onClick={this.handleSubmit.bind(this)} onTestClick={this.handleTestSubmit.bind(this)} /> 
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
