'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import PasswordInput from '../../field/input/PasswordInput';
import TextInput from '../../field/input/TextInput';
import ReadOnlyField from '../../field/ReadOnlyField'
import ServerConfiguration from './ServerConfiguration';

import { alignCenter } from '../../../css/main.css';

class HubConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content =
				<div>
					<ReadOnlyField label="Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}/>
					<TextInput label="API Key" name="hubApiKey" value={this.state.values.hubApiKey} isSet={this.state.values.hubApiKeyIsSet} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.state.errors.apiKeyError}/>
					<NumberInput label="Timeout" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}/>
					<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}/>

					<h2 className={alignCenter}>Proxy Configuration</h2>
					<ReadOnlyField label="Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}/>
					<ReadOnlyField label="Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}/>
					<ReadOnlyField label="Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername}  errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}/>
					<ReadOnlyField label="Proxy Password" name="hubProxyPassword" readOnly="true" isSet={this.state.values.hubProxyPasswordIsSet} errorName="hubProxyPasswordError" errorValue={this.state.errors.hubProxyPassword}/>
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
    baseUrl: '/api/configuration/global',
    testUrl: '/api/configuration/global/test'
};

export default HubConfiguration;
