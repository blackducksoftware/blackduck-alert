'use strict';
import React from 'react';
import CheckboxInput from '../field/input/CheckboxInput';
import NumberInput from '../field/input/NumberInput';
import PasswordInput from '../field/input/PasswordInput';
import TextInput from '../field/input/TextInput';

import ConfigButtons from './ConfigButtons';
import Configuration from './Configuration';

import { alignCenter } from '../../css/main.css';

export default class GlobalConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}

	render() {
		return (
				<div>
					<h1 className={alignCenter}>Global Configuration</h1>
					
					<h2 className={alignCenter}>Hub Configuration</h2>
					<TextInput label="Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}></TextInput>
					<TextInput label="Username" name="hubUsername" value={this.state.values.hubUsername} onChange={this.handleChange} errorName="hubUsernameError" errorValue={this.state.errors.hubUsernameError}></TextInput>
					<PasswordInput label="Password" name="hubPassword" value={this.state.values.hubPassword} onChange={this.handleChange} errorName="hubPasswordError" errorValue={this.state.errors.hubPasswordError}></PasswordInput>
					<NumberInput label="Timeout" name="hubTimeout" readOnly="true" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
					<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>
					
					<h2 className={alignCenter}>Proxy Configuration</h2>
					<TextInput label="Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}></TextInput>
					<NumberInput label="Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}></NumberInput>
					<TextInput label="Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}></TextInput>

					<h2 className={alignCenter}>Scheduling Configuration</h2>
					<TextInput label="Accumulator Cron" name="accumulatorCron" value={this.state.values.accumulatorCron} onChange={this.handleChange} errorName="accumulatorCronError" errorValue={this.state.errors.accumulatorCronError}></TextInput>
					<TextInput label="Daily Digest Cron" name="dailyDigestCron" value={this.state.values.dailyDigestCron} onChange={this.handleChange} errorName="dailyDigestCronError" errorValue={this.state.errors.dailyDigestCronError}></TextInput>
					
					<ConfigButtons includeTest="true" onClick={this.handleSubmit} onTestClick={this.handleTestSubmit} />
					
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
