'use strict';

import React from 'react';
import PropTypes from 'prop-types';
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
		fetch(this.props.baseUrl, {
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
					var jsonArray = JSON.parse(json.message);
					let responseErrors = jsonArray['errors'];
					if (responseErrors) {
						var fieldErrors = {};
						for (var key in responseErrors) {
							if (responseErrors.hasOwnProperty(key)) {
								let name = key.concat('Error');
								let value = responseErrors[key];
								fieldErrors[name] = value;
							}
						}
						self.setState({
							errors: fieldErrors
						});
					}
					self.setState({
						configurationMessage: jsonArray['message']
					});
				});
			}
		});
	}

	handleAdvancedClicked(event){
		event.preventDefault();
		let advancedState = !this.state.advancedShown;
		this.setState({
			advancedShown : advancedState
		});
		return false;
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
						<form onSubmit={this.handleSubmit} className={styles.loginBox}>
							<Header></Header>
							<TextInput label="Hub Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}></TextInput>
							<TextInput label="Username" name="hubUsername" value={this.state.values.hubUsername} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.errors.usernameError}></TextInput>
							<PasswordInput label="Password" name="hubPassword" value={this.state.values.hubPassword} onChange={this.handleChange} errorName="passwordError" errorValue={this.state.errors.passwordError}></PasswordInput>
							<div className={styles.advancedWrapper}>
								<a href="#" className={styles.advanced} onClick={this.handleAdvancedClicked}>{advancedDisplay}</a>
							</div>
							<div className={advancedClass}>
								<NumberInput label="Timeout" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
								<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>
								<TextInput label="Proxy Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}></TextInput>
								<NumberInput label="Proxy Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}></NumberInput>
								<TextInput label="Proxy Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}></TextInput>
							</div>
							<ConfigButtons isFixed="false" includeTest="false" type="submit" text="Login" />
							<p name="configurationMessage">{this.state.configurationMessage}</p>
						</form>
					</div>
				</div>
		)
	}
}

LoginPage.propTypes = {
    getUrl: PropTypes.string,
    baseUrl: PropTypes.string
}

LoginPage.defaultProps = {
    getUrl: '/configuration/global',
    baseUrl: '/login'
}

export default LoginPage;
