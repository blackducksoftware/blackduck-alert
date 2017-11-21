'use strict';

import React from 'react';
import Field from './field/Field';
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
							<Field label="Hub Url" type="text" name="hubUrl" value={this.state.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.hubUrlError}></Field>
			
							<Field label="Username" type="text" name="hubUsername" value={this.state.hubUsername} onChange={this.handleChange} errorName="hubUsernameError" errorValue={this.state.hubUsernameError}></Field>
			
							<Field label="Password" type="password" name="hubPassword" value={this.state.hubPassword} onChange={this.handleChange} errorName="hubPasswordError" errorValue={this.state.hubPasswordError}></Field>
			
							<div className={styles.advanced} onClick={this.handleAdvancedClicked}>{advancedDisplay}</div>
							<div className={advancedClass}>
								<Field label="Timeout" type="number" name="hubTimeout" value={this.state.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.hubTimeoutError}></Field>
				
								<Field label="Trust Https Certificates" type="checkbox" name="hubAlwaysTrustCertificate" value={this.state.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.hubAlwaysTrustCertificateError}></Field>
								
								<Field label="Proxy Host Name" type="text" name="hubProxyHost" value={this.state.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.hubProxyHostError}></Field>
				
								<Field label="Proxy Port" type="number" name="hubProxyPort" value={this.state.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.hubProxyPortError}></Field>
				
								<Field label="Proxy Username" type="text" name="hubProxyUsername" value={this.state.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.hubProxyUsernameError}></Field>
				
								<Field label="Proxy Password" type="password" name="hubProxyPassword" value={this.state.hubProxyPassword} onChange={this.handleChange} errorName="hubProxyPasswordError" errorValue={this.state.hubProxyPasswordError}></Field>
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