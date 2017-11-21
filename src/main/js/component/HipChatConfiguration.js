'use strict';
import React from 'react';
import Field from '../field/Field';
import ConfigButtons from './ConfigButtons';
import Configuration from './Configuration';

import { alignCenter } from '../../css/main.css';

export default class HipChatConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
				<div>
					<h1 className={alignCenter}>HipChat Configuration</h1>
					<Field label="Api Key" type="text" name="apiKey" value={this.state.values.apiKey} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.state.errors.apiKeyError}></Field>
					<Field label="Room Id" type="number" name="roomId" value={this.state.values.roomId} onChange={this.handleChange} errorName="roomIdError" errorValue={this.state.errors.roomIdError}></Field>
					<Field label="Notify" type="checkbox" name="notify" value={this.state.values.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.state.errors.notifyError}></Field>
					<Field label="Color" type="text" name="color" value={this.state.values.color} onChange={this.handleChange} errorName="colorError" errorValue={this.state.errors.colorError}></Field>
					
					<ConfigButtons includeTest="true" onClick={this.handleSubmit} onTestClick={this.handleTestSubmit} />
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}