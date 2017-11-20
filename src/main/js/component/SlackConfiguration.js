import React from 'react';

import Field from '../field/Field';
import ConfigButtons from './ConfigButtons';
import Configuration from './Configuration';

import { alignCenter } from '../../css/main.css';

export default class SlackConfiguration extends Configuration {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.handleChange = this.handleChange.bind(this);
	}

	componentDidMount() {
		super.componentDidMount();
	}
	
	render() {
		return (
				<div>
				<h1 className={alignCenter}>Slack Configuration</h1>
				<Field label="Channel Name" type="text" name="channelName" value={this.state.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.state.channelNameError}></Field>
				
				<Field label="Username" type="text" name="username" value={this.state.username} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.usernameError}></Field>
				
				<Field label="Webhook" type="text" name="webhook" value={this.state.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.state.webhookError}></Field>
				
				<ConfigButtons includeTest="true" onClick={this.handleSubmit.bind(this)} onTestClick={this.handleTestSubmit.bind(this)} /> 
				<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
