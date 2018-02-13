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
		const content = <TextInput label="Api Key" type="text" name="apiKey" value={this.state.values.apiKey} isSet={this.state.values.apiKeyIsSet} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.state.errors.apiKeyError}></TextInput>
        return super.render(content);
	}
};

HipChatConfiguration.propTypes = {
    headerText: PropTypes.string,
    configButtonTest: PropTypes.bool,
    configButtonsSave: PropTypes.bool,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string
};

HipChatConfiguration.defaultProps = {
    headerText: 'HipChat',
    configButtonTest: true,
    baseUrl: '/api/configuration/global/hipchat',
    testUrl: '/api/configuration/global/hipchat/test'
};

export default HipChatConfiguration;
