'use strict';

import React from 'react';

import CheckboxInput from './field/input/CheckboxInput';
import NumberInput from './field/input/NumberInput';
import PasswordInput from './field/input/PasswordInput';
import TextInput from './field/input/TextInput';
import ConfigButtons from './component/ConfigButtons';
import Configuration from './component/Configuration';
import Header from './component/Header';

import styles from '../css/main.css';

class LoginPage extends Configuration {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.handleAdvancedClicked = this.handleAdvancedClicked.bind(this);
	}
	
	handleSubmit(event) {
		this.setState({
			configurationMessage: 'Logging in...',
			errors: {}
		});
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state.values);
		var method = 'POST';
		fetch(this.props.restUrl, {
			method: method,
			credentials: "same-origin",
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			if (response.ok) {
				self.props.handleState('loggedIn', true)
			} else {
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
			}
		});
	}
	
	handleAdvancedClicked(event){
		let advancedState = !this.state.advancedShown;
		this.setState({
			advancedShown : advancedState
		});
	}

	//render is part of the Component lifecycle, used to render the Html
	render() {
		let advancedDisplay = "Show Advanced";
		let advancedClass = styles.hidden;
		if (this.state.advancedShown) {
			advancedClass = "";
			advancedDisplay = "Hide Advanced";
		}
		return (
				<div className={styles.wrapper}>
					<div className={styles.loginContainer}>
						<div className={styles.loginBox}>
							<Header></Header>
							<TextInput label="Hub Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}></TextInput>
							<TextInput label="Username" name="hubUsername" value={this.state.values.hubUsername} onChange={this.handleChange} errorName="hubUsernameError" errorValue={this.state.errors.hubUsernameError}></TextInput>
							<PasswordInput label="Password" name="hubPassword" value={this.state.values.hubPassword} onChange={this.handleChange} errorName="hubPasswordError" errorValue={this.state.hubPasswordError}></PasswordInput>
							<div className={styles.advanced} onClick={this.handleAdvancedClicked}>{advancedDisplay}</div>
							<div className={advancedClass}>
								<NumberInput label="Timeout" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
								<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>
								<TextInput label="Proxy Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}></TextInput>
								<NumberInput label="Proxy Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}></NumberInput>
								<TextInput label="Proxy Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}></TextInput>
							</div>
							<ConfigButtons includeTest="false" onClick={this.handleSubmit} text="Login" />
							<p name="configurationMessage">{this.state.configurationMessage}</p>
						</div>
					</div>
				</div>
		)
	}
}

export default LoginPage;