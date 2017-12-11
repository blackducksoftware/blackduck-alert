'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import TextInput from '../../field/input/TextInput';
import ServerConfiguration from './ServerConfiguration';

import { alignCenter } from '../../../css/main.css';

class HipChatConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content =
				<div>
					<TextInput label="Api Key" type="text" name="apiKey" value={this.state.values.apiKey} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.state.errors.apiKeyError}></TextInput>
					<CheckboxInput label="Notify" name="notify" value={this.state.values.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.state.errors.notifyError}></CheckboxInput>
					<TextInput label="Color" name="color" value={this.state.values.color} onChange={this.handleChange} errorName="colorError" errorValue={this.state.errors.colorError}></TextInput>
				</div>;
        return super.render(content);
	}
};

HipChatConfiguration.propTypes = {
    headerText: PropTypes.string,
    configButtonTest: PropTypes.bool,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string
};

HipChatConfiguration.defaultProps = {
    headerText: 'HipChat Configuration',
    configButtonTest: false,
    baseUrl: '/configuration/hipchat'
};

export default HipChatConfiguration;
