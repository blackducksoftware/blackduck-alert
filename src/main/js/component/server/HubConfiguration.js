'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import PasswordInput from '../../field/input/PasswordInput';
import TextInput from '../../field/input/TextInput';
import ServerConfiguration from './ServerConfiguration';

import { alignCenter } from '../../../css/main.css';

class HubConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content =

				<div>
					<TextInput label="Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}></TextInput>
					<TextInput label="Username" name="hubUsername" value={this.state.values.hubUsername} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.errors.usernameError}></TextInput>
					<PasswordInput label="Password" name="hubPassword" value={this.state.values.hubPassword} onChange={this.handleChange} errorName="passwordError" errorValue={this.state.errors.passwordError}></PasswordInput>
					<NumberInput label="Timeout" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
					<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>

					<h2 className={alignCenter}>Proxy Configuration</h2>
					<TextInput label="Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}></TextInput>
					<NumberInput label="Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}></NumberInput>
					<TextInput label="Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}></TextInput>
				</div>;
        return super.render(content);
	}
};

HubConfiguration.propTypes = {
    headerText: PropTypes.string,
    externaconfigButtonTest: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string
};

HubConfiguration.defaultProps = {
    headerText: 'Hub Configuration',
    configButtonTest: true,
    baseUrl: '/configuration/global',
    testUrl: '/configuration/global/test'
};

export default HubConfiguration;
