'use strict';
import React from 'react';
import Field from '../field/Field';
import ConfigButtons from './ConfigButtons';
import Configuration from './Configuration';

import { alignCenter } from '../../css/main.css';

export default class EmailConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}

	render() {
		return (
				<div>
					<h1 className={alignCenter}>Email Configuration</h1>
					<Field label="Email Template Directory" type="text" name="emailTemplateDirectory" value={this.state.emailTemplateDirectory} onChange={this.handleChange} errorName="emailTemplateDirectoryError" errorValue={this.state.errors.emailTemplateDirectoryError}></Field>
					<Field label="Email Template Logo" type="text" name="emailTemplateLogoImage" value={this.state.emailTemplateLogoImage} onChange={this.handleChange} errorName="emailTemplateLogoImageError" errorValue={this.state.errors.emailTemplateLogoImageError}></Field>
					<Field label="Email Subject Line" type="text" name="emailSubjectLine" value={this.state.emailSubjectLine} onChange={this.handleChange} errorName="emailSubjectLineError" errorValue={this.state.errors.emailSubjectLineError}></Field>

					<h2 className={alignCenter}>Mail Smtp Configuration</h2>
					<Field label="Mail Smtp Host" type="text" name="mailSmtpHost" value={this.state.mailSmtpHost} onChange={this.handleChange} errorName="mailSmtpHostError" errorValue={this.state.errors.mailSmtpHostError}></Field>
					<Field label="Mail Smtp User" type="text" name="mailSmtpUser" value={this.state.mailSmtpUser} onChange={this.handleChange} errorName="mailSmtpUserError" errorValue={this.state.errors.mailSmtpUserError}></Field>
					<Field label="Mail Smtp Password" type="password" name="mailSmtpPassword" value={this.state.mailSmtpPassword} onChange={this.handleChange} errorName="mailSmtpPasswordError" errorValue={this.state.errors.mailSmtpPasswordError}></Field>
					<Field label="Mail Smtp Port" type="number" name="mailSmtpPort" value={this.state.mailSmtpPort} onChange={this.handleChange} errorName="mailSmtpPortError" errorValue={this.state.errors.mailSmtpPortError}></Field>
					<Field label="Mail Smtp Connection Timeout" type="number" name="mailSmtpConnectionTimeout" value={this.state.mailSmtpConnectionTimeout} onChange={this.handleChange} errorName="mailSmtpConnectionTimeoutError" errorValue={this.state.errors.mailSmtpConnectionTimeoutError}></Field>
					<Field label="Mail Smtp Timeout" type="number" name="mailSmtpTimeout" value={this.state.mailSmtpTimeout} onChange={this.handleChange} errorName="mailSmtpTimeoutError" errorValue={this.state.errors.mailSmtpTimeoutError}></Field>
					<Field label="Mail Smtp From" type="text" name="mailSmtpFrom" value={this.state.mailSmtpFrom} onChange={this.handleChange} errorName="mailSmtpFromError" errorValue={this.state.errors.mailSmtpFromError}></Field>
					<Field label="Mail Smtp Localhost" type="text" name="mailSmtpLocalhost" value={this.state.mailSmtpLocalhost} onChange={this.handleChange} errorName="mailSmtpLocalhostError" errorValue={this.state.errors.mailSmtpLocalhostError}></Field>
					<Field label="Mail Smtp Ehlo" type="checkbox" name="mailSmtpEhlo" value={this.state.mailSmtpEhlo} onChange={this.handleChange} errorName="mailSmtpEhloError" errorValue={this.state.errors.mailSmtpEhloError}></Field>
					<Field label="Mail Smtp Auth" type="checkbox" name="mailSmtpAuth" value={this.state.mailSmtpAuth} onChange={this.handleChange} errorName="mailSmtpAuthError" errorValue={this.state.errors.mailSmtpAuthError}></Field>
					<Field label="Mail Smtp Dns Notify" type="text" name="mailSmtpDnsNotify" value={this.state.mailSmtpDnsNotify} onChange={this.handleChange} errorName="mailSmtpDnsNotifyError" errorValue={this.state.errors.mailSmtpDnsNotifyError}></Field>
					<Field label="Mail Smtp Dns Ret" type="text" name="mailSmtpDnsRet" value={this.state.mailSmtpDnsRet} onChange={this.handleChange} errorName="mailSmtpDnsRetError" errorValue={this.state.errors.mailSmtpDnsRetError}></Field>
					<Field label="Mail Smtp Allow 8-bit Mime" type="checkbox" name="mailSmtpAllow8bitmime" value={this.state.mailSmtpAllow8bitmime} onChange={this.handleChange} errorName="mailSmtpAllow8bitmimeError" errorValue={this.state.errors.mailSmtpAllow8bitmimeError}></Field>
					<Field label="Mail Smtp Send Partial" type="checkbox" name="mailSmtpSendPartial" value={this.state.mailSmtpSendPartial} onChange={this.handleChange} errorName="mailSmtpSendPartialError" errorValue={this.state.errors.mailSmtpSendPartialError}></Field>
					
					<ConfigButtons includeTest="true" onClick={this.handleSubmit} onTestClick={this.handleTestSubmit} />
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
