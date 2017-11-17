'use strict';

import React from 'react';
import Field from './field/Field';
import Header from './component/Header';

import styles from '../css/main.css';

class LoginPage extends React.Component {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.state = {
				id: undefined,
				hubUrl: '',
				hubUsername: '',
				hubPassword: '',
				hubTimeout: 60,
				hubAlwaysTrustCertificate: false,
				hubProxyHost: '',
				hubProxyPort: undefined,
				hubProxyUsername: '',
				hubProxyPassword: '',
				hideAdvanced: true,

				configurationMessage: '',
				hubUrlError: '',
				hubUsernameError: '',
				hubPasswordError: '',
				hubTimeoutError: '',
				hubAlwaysTrustCertificateError: '',
				hubProxyHostError: '',
				hubProxyPortError: '',
				hubProxyUsernameError: '',
				hubProxyPasswordError: ''
		};
		this.handleChange = this.handleChange.bind(this);
		this.handleAdvancedClicked = this.handleAdvancedClicked.bind(this);
	}

	resetMessageStates() {
		this.setState({
			configurationMessage: '',
			hubUrlError: '',
			hubUsernameError: '',
			hubPasswordError: '',
			hubTimeoutError: '',
			hubAlwaysTrustCertificateError: '',
			hubProxyHostError: '',
			hubProxyPortError: '',
			hubProxyUsernameError: '',
			hubProxyPasswordError: ''
		});
	}

	//componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		this.resetMessageStates();
		var self = this;
		self.setState({
			configurationMessage: 'Loading...'
		});
		fetch('/configuration/global',{
			credentials: "same-origin"
		})  
		.then(function(response) {
			if (!response.ok) {
				return response.json().then(json => {
					self.setState({
						configurationMessage: json.message
					});
				});
			} else {
				return response.json().then(jsonArray => {
					self.setState({
						configurationMessage: ''
					});
					if (jsonArray != null && jsonArray.length > 0) {
						var configuration = jsonArray[0];
						self.setState({
							id: configuration.id,
							hubUrl: configuration.hubUrl,
							hubUsername: configuration.hubUsername,
							hubPassword: configuration.hubPassword,
							hubTimeout: configuration.hubTimeout,
							hubAlwaysTrustCertificate: configuration.hubAlwaysTrustCertificate,
							hubProxyHost: configuration.hubProxyHost,
							hubProxyPort: configuration.hubProxyPort,
							hubProxyUsername: configuration.hubProxyUsername,
							hubProxyPassword: configuration.hubProxyPassword
						});
					}
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
//		if (this.state.id) {
//			method = 'PUT';
//		}
		self.setState({
			configurationMessage: 'Saving...'
		});
		fetch('/login', {
			method: method,
			credentials: "same-origin",
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
				} else {
					self.props.handleState('loggedIn', true)
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
	
	handleAdvancedClicked(event){
		let advancedState = !this.state.hideAdvanced;
		this.setState({
			hideAdvanced : advancedState
		});
	}

	//render is part of the Component lifecycle, used to render the Html
	render() {
		let advancedClass = "";
		if (this.state.hideAdvanced) {
			advancedClass = styles.hidden;
		}
		return (
				<div className={styles.wrapper}>
				<div className={styles.loginContainer}>
				<div className={styles.loginBox}>
				<Header></Header>
				<form onSubmit={this.handleSubmit.bind(this)}>
				<Field label="Hub Url" type="text" name="hubUrl" value={this.state.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.hubUrlError}></Field>

				<Field label="Username" type="text" name="hubUsername" value={this.state.hubUsername} onChange={this.handleChange} errorName="hubUsernameError" errorValue={this.state.hubUsernameError}></Field>

				<Field label="Password" type="password" name="hubPassword" value={this.state.hubPassword} onChange={this.handleChange} errorName="hubPasswordError" errorValue={this.state.hubPasswordError}></Field>

				<div className={styles.advanced} onClick={this.handleAdvancedClicked}>Advanced</div>
				<div className={advancedClass}>
				<Field label="Timeout" type="number" name="hubTimeout" value={this.state.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.hubTimeoutError}></Field>

				<Field label="Trust Https Certificates" type="checkbox" name="hubAlwaysTrustCertificate" value={this.state.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.hubAlwaysTrustCertificateError}></Field>
				
				<Field label="Proxy Host Name" type="text" name="hubProxyHost" value={this.state.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.hubProxyHostError}></Field>

				<Field label="Proxy Port" type="number" name="hubProxyPort" value={this.state.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.hubProxyPortError}></Field>

				<Field label="Proxy Username" type="text" name="hubProxyUsername" value={this.state.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.hubProxyUsernameError}></Field>

				<Field label="Proxy Password" type="password" name="hubProxyPassword" value={this.state.hubProxyPassword} onChange={this.handleChange} errorName="hubProxyPasswordError" errorValue={this.state.hubProxyPasswordError}></Field>
				</div>
				<p name="configurationMessage">{this.state.configurationMessage}</p>
				<div className={styles.submitContainers}>
				<input className={styles.inputButton} type="submit" value="Login"></input>
				</div>
				</form>
				</div>
				</div>
				</div>
		)
	}
}

export default LoginPage;