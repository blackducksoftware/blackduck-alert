import React from 'react';
import Field from '../field/Field';

class GlobalConfiguration extends React.Component {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.state = {
				id: '',
				hubUrl: '',
				hubUsername: '',
				hubPassword: '',
				hubTimeout: 0,
				hubAlwaysTrustCertificate: false,
				hubProxyHost: '',
				hubProxyPort: 0,
				hubProxyUsername: '',
				hubProxyPassword: '',
				accumulatorCron: '',
				dailyDigestCron: '',

				configurationMessage: '',
				hubUrlError: '',
				hubUsernameError: '',
				hubTimeoutError: '',
				hubAlwaysTrustCertificateError: '',
				hubProxyHostError: '',
				hubProxyPortError: '',
				hubProxyUsernameError: '',
				accumulatorCronError: '',
				dailyDigestCronError: ''
		};
		this.handleChange = this.handleChange.bind(this);
	}

	resetMessageStates() {
		this.setState({
			configurationMessage: '',
			hubUrlError: '',
			hubUsernameError: '',
			hubTimeoutError: '',
			hubAlwaysTrustCertificateError: '',
			hubProxyHostError: '',
			hubProxyPortError: '',
			hubProxyUsernameError: '',
			accumulatorCronError: '',
			dailyDigestCronError: ''
		});
	}

	//componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		this.resetMessageStates();
		var self = this;
		fetch('/configuration/global')  
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
			if(body != null && body.length > 0){
				var globalConfiguration = body[0];
				self.setState({
					id: globalConfiguration.id,
					hubUrl: globalConfiguration.hubUrl,
					hubUsername: globalConfiguration.hubUsername,
					hubPassword: globalConfiguration.hubPassword,
					hubTimeout: globalConfiguration.hubTimeout,
					hubAlwaysTrustCertificate: globalConfiguration.hubAlwaysTrustCertificate,
					hubProxyHost: globalConfiguration.hubProxyHost,
					hubProxyPort: globalConfiguration.hubProxyPort,
					hubProxyUsername: globalConfiguration.hubProxyUsername,
					hubProxyPassword: globalConfiguration.hubProxyPassword,
					accumulatorCron: globalConfiguration.accumulatorCron,
					dailyDigestCron: globalConfiguration.dailyDigestCron
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
		if (this.state.id){
			method = 'PUT';
		}
		fetch('/configuration/global', {
			method: method,
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			return response.json().then(json => {
				let errors = json.errors;
				if (errors){
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

	handleTestSubmit(event){
		this.resetMessageStates();
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state);
		fetch('/configuration/global/test', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			return response.json().then(json => {
				let errors = json.errors;
				if (errors){
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

	//render is part of the Component lifecycle, used to render the Html
	render() {
		return (
				<div>
				<h1>Global Configuration</h1>
				<form onSubmit={this.handleSubmit.bind(this)}>
				<h2>Hub Configuration</h2>
				<Field label="Url" type="text" name="hubUrl" value={this.state.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.hubUrlError}></Field>

				<Field label="Username" type="text" name="hubUsername" value={this.state.hubUsername} onChange={this.handleChange} errorName="hubUsernameError" errorValue={this.state.hubUsernameError}></Field>

				<Field label="Password" type="password" name="hubPassword" value={this.state.hubPassword} onChange={this.handleChange} ></Field>

				<Field label="Timeout" type="number" name="hubTimeout" value={this.state.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.hubTimeoutError}></Field>

				<Field label="Trust Https Certificates" type="checkbox" name="hubAlwaysTrustCertificate" value={this.state.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.hubAlwaysTrustCertificateError}></Field>

				<h2>Proxy Configuration</h2>
				<Field label="Host Name" type="text" name="hubProxyHost" value={this.state.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.hubProxyHostError}></Field>

				<Field label="Port" type="number" name="hubProxyPort" value={this.state.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.hubProxyPortError}></Field>

				<Field label="Username" type="text" name="hubProxyUsername" value={this.state.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.hubProxyUsernameError}></Field>

				<Field label="Password" type="text" name="hubProxyPassword" value={this.state.hubProxyPassword} onChange={this.handleChange} ></Field>

				<h2>Scheduling Configuration</h2>
				<Field label="Accumulator Cron" type="text" name="accumulatorCron" value={this.state.accumulatorCron} onChange={this.handleChange} errorName="accumulatorCronError" errorValue={this.state.accumulatorCronError}></Field>

				<Field label="Daily Digest Cron" type="text" name="dailyDigestCron" value={this.state.dailyDigestCron} onChange={this.handleChange} errorName="dailyDigestCronError" errorValue={this.state.dailyDigestCronError}></Field>

				<input type="submit" value="Save"></input>
				<input type="button" value="Test" onClick={this.handleTestSubmit.bind(this)}></input>
				<p name="configurationMessage">{this.state.configurationMessage}</p>
				</form>
				</div>
		)
	}
}

export default GlobalConfiguration;
