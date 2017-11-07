import React from 'react';
import Field from '../field/Field';

class EmailConfiguration extends React.Component {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.state = {
				id: undefined,
				mailSmtpHost: '',
				mailSmtpUser: '',
				mailSmtpPassword: '',
				mailSmtpPort: undefined,
				mailSmtpConnectionTimeout: undefined,
				mailSmtpTimeout: undefined,
				mailSmtpFrom: '',
				mailSmtpLocalhost: '',
				mailSmtpEhlo: '',
				mailSmtpAuth: '',
				mailSmtpDnsNotify: '',
				mailSmtpDnsRet: '',
				mailSmtpAllow8bitmime: '',
				mailSmtpSendPartial: '',
				emailTemplateDirectory: '',
				emailTemplateLogoImage: '',
				emailSubjectLine: '',

				configurationMessage: '',
				mailSmtpHostError: '',
				mailSmtpUserError: '',
				mailSmtpPasswordError: '',
				mailSmtpPortError: '',
				mailSmtpConnectionTimeoutError: '',
				mailSmtpTimeoutError: '',
				mailSmtpFromError: '',
				mailSmtpLocalhostError: '',
				mailSmtpEhloError: '',
				mailSmtpAuthError: '',
				mailSmtpDnsNotifyError: '',
				mailSmtpDnsRetError: '',
				mailSmtpAllow8bitmimeError: '',
				mailSmtpSendPartialError: '',
				emailTemplateDirectoryError: '',
				emailTemplateLogoImageError: '',
				emailSubjectLineError: ''
		};
		this.handleChange = this.handleChange.bind(this);
	}

	resetMessageStates() {
		this.setState({
			configurationMessage: '',
			mailSmtpHostError: '',
			mailSmtpUserError: '',
			mailSmtpPasswordError: '',
			mailSmtpPortError: '',
			mailSmtpConnectionTimeoutError: '',
			mailSmtpTimeoutError: '',
			mailSmtpFromError: '',
			mailSmtpLocalhostError: '',
			mailSmtpEhloError: '',
			mailSmtpAuthError: '',
			mailSmtpDnsNotifyError: '',
			mailSmtpDnsRetError: '',
			mailSmtpAllow8bitmimeError: '',
			mailSmtpSendPartialError: '',
			emailTemplateDirectoryError: '',
			emailTemplateLogoImageError: '',
			emailSubjectLineError: ''
		});
	}

	//componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		this.resetMessageStates();
		var self = this;
		fetch('/configuration/email')  
		.then(function(response) {
			if (!response.ok) {
				return response.json().then(json => {
					self.setState({
						configurationMessage: json.message
					});
				});
			} else {
				return response.json();
			}
		}).then(function(body) {
			if (body != null && body.length > 0) {
				var configuration = body[0];
				self.setState({
					id: configuration.id,
					mailSmtpHost:  configuration.mailSmtpHost,
					mailSmtpUser:  configuration.mailSmtpUser,
					mailSmtpPassword:  configuration.mailSmtpPassword,
					mailSmtpPort:  configuration.mailSmtpPort,
					mailSmtpConnectionTimeout:  configuration.mailSmtpConnectionTimeout,
					mailSmtpTimeout:  configuration.mailSmtpTimeout,
					mailSmtpFrom:  configuration.mailSmtpFrom,
					mailSmtpLocalhost:  configuration.mailSmtpLocalhost,
					mailSmtpEhlo:  configuration.mailSmtpEhlo,
					mailSmtpAuth:  configuration.mailSmtpAuth,
					mailSmtpDnsNotify:  configuration.mailSmtpDnsNotify,
					mailSmtpDnsRet:  configuration.mailSmtpDnsRet,
					mailSmtpAllow8bitmime:  configuration.mailSmtpAllow8bitmime,
					mailSmtpSendPartial:  configuration.mailSmtpSendPartial,
					emailTemplateDirectory:  configuration.emailTemplateDirectory,
					emailTemplateLogoImage:  configuration.emailTemplateLogoImage,
					emailSubjectLine:  configuration.emailSubjectLine
				});
			}
		});
	}

	handleSubmit(event) {
		this.resetMessageStates();
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state);
		var method = 'POST';
		if (this.state.id) {
			method = 'PUT';
		}
		fetch('/configuration/email', {
			method: method,
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			return response.json().then(json => {
				let errors = json.errors;
				if (errors) {
					for (var key in errors) {
						if (errors.hasOwnProperty(key)) {
							let name = key.concat('Error');
							let value = errors[key];
							self.setState({
								[name]: value
							});
						}
					}
				}
				self.setState({
					configurationMessage: json.message
				});
			});
		});
	}

	handleTestSubmit(event) {
		this.resetMessageStates();
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state);
		fetch('/configuration/email/test', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			return response.json().then(json => {
				let errors = json.errors;
				if (errors) {
					for (var key in errors) {
						if (errors.hasOwnProperty(key)) {
							let name = key.concat('Error');
							let value = errors[key];
							self.setState({
								[name]: value
							});
						}
					}
				}
				self.setState({
					configurationMessage: json.message
				});
			});
		});
	}

	handleChange(event) {
		const target = event.target;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		const name = target.name;

		this.setState({
			[name]: value
		});
	}

	render() {
		return (
				<div>
				<h1>Email Configuration</h1>
				<form onSubmit={this.handleSubmit.bind(this)}>
				<Field label="Email Template Directory" type="text" name="emailTemplateDirectory" value={this.state.emailTemplateDirectory} onChange={this.handleChange} errorName="emailTemplateDirectoryError" errorValue={this.state.emailTemplateDirectoryError}></Field>

				<Field label="Email Template Logo" type="text" name="emailTemplateLogoImage" value={this.state.emailTemplateLogoImage} onChange={this.handleChange} errorName="emailTemplateLogoImageError" errorValue={this.state.emailTemplateLogoImageError}></Field>

				<Field label="Email Subject Line" type="text" name="emailSubjectLine" value={this.state.emailSubjectLine} onChange={this.handleChange} errorName="emailSubjectLineError" errorValue={this.state.emailSubjectLineError}></Field>

				<h2>Mail Smtp Configuration</h2>
				<Field label="Mail Smtp Host" type="text" name="mailSmtpHost" value={this.state.mailSmtpHost} onChange={this.handleChange} errorName="mailSmtpHostError" errorValue={this.state.mailSmtpHostError}></Field>

				<Field label="Mail Smtp User" type="text" name="mailSmtpUser" value={this.state.mailSmtpUser} onChange={this.handleChange} errorName="mailSmtpUserError" errorValue={this.state.mailSmtpUserError}></Field>

				<Field label="Mail Smtp Password" type="password" name="mailSmtpPassword" value={this.state.mailSmtpPassword} onChange={this.handleChange} errorName="mailSmtpPasswordError" errorValue={this.state.mailSmtpPasswordError}></Field>

				<Field label="Mail Smtp Port" type="number" name="mailSmtpPort" value={this.state.mailSmtpPort} onChange={this.handleChange} errorName="mailSmtpPortError" errorValue={this.state.mailSmtpPortError}></Field>

				<Field label="Mail Smtp Connection Timeout" type="number" name="mailSmtpConnectionTimeout" value={this.state.mailSmtpConnectionTimeout} onChange={this.handleChange} errorName="mailSmtpConnectionTimeoutError" errorValue={this.state.mailSmtpConnectionTimeoutError}></Field>

				<Field label="Mail Smtp Timeout" type="number" name="mailSmtpTimeout" value={this.state.mailSmtpTimeout} onChange={this.handleChange} errorName="mailSmtpTimeoutError" errorValue={this.state.mailSmtpTimeoutError}></Field>

				<Field label="Mail Smtp From" type="text" name="mailSmtpFrom" value={this.state.mailSmtpFrom} onChange={this.handleChange} errorName="mailSmtpFromError" errorValue={this.state.mailSmtpFromError}></Field>

				<Field label="Mail Smtp Localhost" type="text" name="mailSmtpLocalhost" value={this.state.mailSmtpLocalhost} onChange={this.handleChange} errorName="mailSmtpLocalhostError" errorValue={this.state.mailSmtpLocalhostError}></Field>

				<Field label="Mail Smtp Ehlo" type="checkbox" name="mailSmtpEhlo" value={this.state.mailSmtpEhlo} onChange={this.handleChange} errorName="mailSmtpEhloError" errorValue={this.state.mailSmtpEhloError}></Field>

				<Field label="Mail Smtp Auth" type="checkbox" name="mailSmtpAuth" value={this.state.mailSmtpAuth} onChange={this.handleChange} errorName="mailSmtpAuthError" errorValue={this.state.mailSmtpAuthError}></Field>

				<Field label="Mail Smtp Dns Notify" type="text" name="mailSmtpDnsNotify" value={this.state.mailSmtpDnsNotify} onChange={this.handleChange} errorName="mailSmtpDnsNotifyError" errorValue={this.state.mailSmtpDnsNotifyError}></Field>

				<Field label="Mail Smtp Dns Ret" type="text" name="mailSmtpDnsRet" value={this.state.mailSmtpDnsRet} onChange={this.handleChange} errorName="mailSmtpDnsRetError" errorValue={this.state.mailSmtpDnsRetError}></Field>

				<Field label="Mail Smtp Allow 8-bit Mime" type="checkbox" name="mailSmtpAllow8bitmime" value={this.state.mailSmtpAllow8bitmime} onChange={this.handleChange} errorName="mailSmtpAllow8bitmimeError" errorValue={this.state.mailSmtpAllow8bitmimeError}></Field>

				<Field label="Mail Smtp Send Partial" type="checkbox" name="mailSmtpSendPartial" value={this.state.mailSmtpSendPartial} onChange={this.handleChange} errorName="mailSmtpSendPartialError" errorValue={this.state.mailSmtpSendPartialError}></Field>
				<input type="submit" value="Save"></input>
				<input type="button" value="Test" onClick={this.handleTestSubmit.bind(this)}></input>
				<p name="configurationMessage">{this.state.configurationMessage}</p>
				</form>
				</div>
		)
	}
}

export default EmailConfiguration;
