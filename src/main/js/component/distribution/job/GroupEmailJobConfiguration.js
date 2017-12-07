'use strict';
import React from 'react';

import TextInput from '../../../field/input/TextInput';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class GroupEmailJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content = 
			<TextInput label="Group(TODO add autoComplete or switch to multiselect)" name="group" value={this.props.group} onChange={this.props.handleGroupChange} errorName="groupError" errorValue={this.props.groupError}></TextInput>
		var renderResult =  super.render(content);
		return renderResult;
	}
}
