'use strict';
import React from 'react';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import PasswordInput from '../../field/input/PasswordInput';
import TextInput from '../../field/input/TextInput';

import ConfigButtons from '../ConfigButtons';
import Configuration from '../Configuration';

import { alignCenter } from '../../../css/main.css';

export default class EmailConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}

	render() {
		return (
				<div>
					<h1 className={alignCenter}>Email Smtp Server Configuration</h1>
					<TextInput label="Mail Smtp Host" name="mailSmtpHost" value={this.state.mailSmtpHost} onChange={this.handleChange} errorName="mailSmtpHostError" errorValue={this.state.errors.mailSmtpHostError}></TextInput>
					<TextInput label="Mail Smtp User" name="mailSmtpUser" value={this.state.mailSmtpUser} onChange={this.handleChange} errorName="mailSmtpUserError" errorValue={this.state.errors.mailSmtpUserError}></TextInput>
					<PasswordInput label="Mail Smtp Password" name="mailSmtpPassword" value={this.state.mailSmtpPassword} onChange={this.handleChange} errorName="mailSmtpPasswordError" errorValue={this.state.errors.mailSmtpPasswordError}></PasswordInput>
					<NumberInput label="Mail Smtp Port" name="mailSmtpPort" value={this.state.mailSmtpPort} onChange={this.handleChange} errorName="mailSmtpPortError" errorValue={this.state.errors.mailSmtpPortError}></NumberInput>
					<NumberInput label="Mail Smtp Connection Timeout" name="mailSmtpConnectionTimeout" value={this.state.mailSmtpConnectionTimeout} onChange={this.handleChange} errorName="mailSmtpConnectionTimeoutError" errorValue={this.state.errors.mailSmtpConnectionTimeoutError}></NumberInput>
					<NumberInput label="Mail Smtp Timeout" name="mailSmtpTimeout" value={this.state.mailSmtpTimeout} onChange={this.handleChange} errorName="mailSmtpTimeoutError" errorValue={this.state.errors.mailSmtpTimeoutError}></NumberInput>
					<TextInput label="Mail Smtp From" name="mailSmtpFrom" value={this.state.mailSmtpFrom} onChange={this.handleChange} errorName="mailSmtpFromError" errorValue={this.state.errors.mailSmtpFromError}></TextInput>
					<TextInput label="Mail Smtp Localhost" name="mailSmtpLocalhost" value={this.state.mailSmtpLocalhost} onChange={this.handleChange} errorName="mailSmtpLocalhostError" errorValue={this.state.errors.mailSmtpLocalhostError}></TextInput>
					<CheckboxInput label="Mail Smtp Ehlo" name="mailSmtpEhlo" value={this.state.mailSmtpEhlo} onChange={this.handleChange} errorName="mailSmtpEhloError" errorValue={this.state.errors.mailSmtpEhloError}></CheckboxInput>
					<CheckboxInput label="Mail Smtp Auth" name="mailSmtpAuth" value={this.state.mailSmtpAuth} onChange={this.handleChange} errorName="mailSmtpAuthError" errorValue={this.state.errors.mailSmtpAuthError}></CheckboxInput>
					<TextInput label="Mail Smtp Dns Notify" name="mailSmtpDnsNotify" value={this.state.mailSmtpDnsNotify} onChange={this.handleChange} errorName="mailSmtpDnsNotifyError" errorValue={this.state.errors.mailSmtpDnsNotifyError}></TextInput>
					<TextInput label="Mail Smtp Dns Ret" name="mailSmtpDnsRet" value={this.state.mailSmtpDnsRet} onChange={this.handleChange} errorName="mailSmtpDnsRetError" errorValue={this.state.errors.mailSmtpDnsRetError}></TextInput>
					<CheckboxInput label="Mail Smtp Allow 8-bit Mime" name="mailSmtpAllow8bitmime" value={this.state.mailSmtpAllow8bitmime} onChange={this.handleChange} errorName="mailSmtpAllow8bitmimeError" errorValue={this.state.errors.mailSmtpAllow8bitmimeError}></CheckboxInput>
					<CheckboxInput label="Mail Smtp Send Partial" name="mailSmtpSendPartial" value={this.state.mailSmtpSendPartial} onChange={this.handleChange} errorName="mailSmtpSendPartialError" errorValue={this.state.errors.mailSmtpSendPartialError}></CheckboxInput>

					<ConfigButtons includeTest="true" onClick={this.handleSubmit} onTestClick={this.handleTestSubmit} />
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
