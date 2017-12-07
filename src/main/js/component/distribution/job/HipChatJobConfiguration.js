'use strict';
import React from 'react';

import TextInput from '../../../field/input/TextInput';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class HipChatJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content = 
			<TextInput label="Room Name" name="roomName" value={this.props.roomName} onChange={this.props.handleRoomNameChange} errorName="roomNameError" errorValue={this.props.roomNameError}></TextInput>
		return super.render(content);
	}
}