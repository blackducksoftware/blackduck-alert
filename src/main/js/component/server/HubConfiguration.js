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
					<TextInput label="Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}></TextInput>
					<TextInput label="API Token" name="hubApiKey" value={this.state.values.hubApiKey} onChange={this.handleChange} errorName="hubApiKeyError" errorValue={this.state.errors.hubApiKeyError}></TextInput>
					<NumberInput label="Timeout" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
					<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>

					<h2 className={alignCenter}>Proxy Configuration</h2>
					<TextInput label="Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}></TextInput>
					<NumberInput label="Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}></NumberInput>
					<TextInput label="Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername}  errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}></TextInput>
					<TextInput label="Proxy Password" name="hubProxyPassword" readOnly="true" isSet={this.state.values.hubProxyPasswordIsSet} errorName="hubProxyPasswordError" errorValue={this.state.errors.hubProxyPassword}></TextInput>
				</div>;
        return super.render(content);
	}
};

HubConfiguration.propTypes = {
    headerText: PropTypes.string,
    externaconfigButtonTest: PropTypes.bool,
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
