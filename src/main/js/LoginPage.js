'use strict';

import React from 'react';
import PropTypes from 'prop-types';
import CheckboxInput from './field/input/CheckboxInput';
import NumberInput from './field/input/NumberInput';
import PasswordInput from './field/input/PasswordInput';
import TextInput from './field/input/TextInput';
import SubmitButton from './field/input/SubmitButton';
import ReadOnlyField from './field/ReadOnlyField';
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
			inProgress: true,
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
			self.setState({
				inProgress: false
			});
			if (response.ok) {
				self.props.handleState('loggedIn', true)
			} else {
				return response.json().then(json => {
					var message = json.message;
					var fieldErrors = {};
					var showAdvanced = self.state.advancedShown;
					try {
						var jsonArray = JSON.parse(message);
						let responseErrors = jsonArray['errors'];
						if (responseErrors) {
							for (var key in responseErrors) {
								if (responseErrors.hasOwnProperty(key)) {
									let name = key.concat('Error');
									let value = responseErrors[key];
									fieldErrors[name] = value;
								}
							}
						}
						if (fieldErrors.hubTimeoutError || fieldErrors.hubAlwaysTrustCertificateError  || fieldErrors.hubProxyHostError  || fieldErrors.hubProxyPortError || fieldErrors.hubProxyUsernameError) {
							showAdvanced = true;
						}
						message = jsonArray['message']
					} catch (e) {
						// ignore exception
					}


					self.setState({
						advancedShown : showAdvanced,
						errors: fieldErrors,
						configurationMessage: message
					});
				});
			}
		})
		.catch(function(error) {
 		 	console.log(error);
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
		var progressIndicator = null;
		if (this.state.inProgress) {
        	const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
			progressIndicator = <div className={styles.progressIcon}>
									<i className={fontAwesomeIcon} aria-hidden='true'></i>
								</div>;
		}
		return (
				<div className={styles.wrapper}>
					<div className={styles.loginContainer}>
						<div className={styles.loginBox}>
                            <Header />
							<form className={`form-horizontal ${styles.loginForm}`} onSubmit={this.handleSubmit}>
								{ this.state.configurationMessage && <div className="alert alert-danger">
									<p name="configurationMessage">{this.state.configurationMessage}</p>
								</div> }

								<ReadOnlyField label="Hub Url" name="hubUrl" readOnly="true" value={this.state.values.hubUrl} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}/>

								<TextInput label="Username" name="hubUsername" value={this.state.values.hubUsername} onChange={this.handleChange} errorName="usernameError" errorValue={this.state.errors.usernameError}/>

								<PasswordInput label="Password" name="hubPassword" value={this.state.values.hubPassword} onChange={this.handleChange} errorName="passwordError" errorValue={this.state.errors.passwordError}/>

								<div className="row">
									<div className="col-sm-8 col-sm-offset-4">
										<a href="#" className={styles.advanced} onClick={this.handleAdvancedClicked}>{advancedDisplay}</a>
									</div>
								</div>

								<div className={advancedClass}>
									<NumberInput label="Timeout" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
									<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.state.values.hubAlwaysTrustCertificate} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>
									<ReadOnlyField label="Proxy Host Name" name="hubProxyHost" readOnly="true" value={this.state.values.hubProxyHost} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}/>
									<ReadOnlyField label="Proxy Port" name="hubProxyPort" readOnly="true" value={this.state.values.hubProxyPort} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}/>
									<ReadOnlyField label="Proxy Username" name="hubProxyUsername" readOnly="true" value={this.state.values.hubProxyUsername} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}/>
									<ReadOnlyField label="Proxy Password" name="hubProxyPassword" readOnly="true"  isSet={this.state.values.hubProxyPasswordIsSet} errorName="hubProxyPasswordError" errorValue={this.state.errors.hubProxyPassword}/>
								</div>
								<div className="row">
									<div className="col-sm-12 text-right">
										<SubmitButton label="Login" />
									</div>
								</div>
                                <div className="row">
									<div className="col-sm-12">
										{progressIndicator}
									</div>
								</div>
							</form>
						</div>
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
