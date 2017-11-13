import React from 'react';
import Field from '../field/Field';

import styles from '../../css/main.css';

class HipChatConfiguration extends React.Component {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.state = {
				id: undefined,
				apiKey: '',
				roomId: undefined,
				notify: false,
				color: '',

				configurationMessage: '',
				apiKeyError: '',
				roomIdError: '',
				notifyError: '',
				colorError: ''
		};
		this.handleChange = this.handleChange.bind(this);
	}

	resetMessageStates() {
		this.setState({
			configurationMessage: '',
			apiKeyError: '',
			roomIdError: '',
			notifyError: '',
			colorError: ''
		});
	}

	//componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		this.resetMessageStates();
		var self = this;
		self.setState({
			configurationMessage: 'Loading...'
		});
		fetch('/configuration/hipchat')  
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
					if(jsonArray != null && jsonArray.length > 0) {
						var configuration = jsonArray[0];
						self.setState({
							id: configuration.id,
							apiKey: configuration.apiKey,
							roomId: configuration.roomId,
							notify: configuration.notify,
							color: configuration.color
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
		if (this.state.id) {
			method = 'PUT';
		}
		self.setState({
			configurationMessage: 'Saving...'
		});
		fetch('/configuration/hipchat', {
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
		self.setState({
			configurationMessage: 'Testing...'
		});
		fetch('/configuration/hipchat/test', {
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
				<h1>HipChat Configuration</h1>
				<form onSubmit={this.handleSubmit.bind(this)}>
				<Field label="Api Key" type="text" name="apiKey" value={this.state.apiKey} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.state.apiKeyError}></Field>

				<Field label="Room Id" type="number" name="roomId" value={this.state.roomId} onChange={this.handleChange} errorName="roomIdError" errorValue={this.state.roomIdError}></Field>
				
				<Field label="Notify" type="checkbox" name="notify" value={this.state.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.state.notifyError}></Field>
				
				<Field label="Color" type="text" name="color" value={this.state.color} onChange={this.handleChange} errorName="colorError" errorValue={this.state.colorError}></Field>
				
				<div className={styles.submitContainers}>
					<input type="submit" value="Save"></input>
					<input type="button" value="Test" onClick={this.handleTestSubmit.bind(this)}></input>
				</div>
				<p name="configurationMessage">{this.state.configurationMessage}</p>
				</form>
				</div>
		)
	}
}

export default HipChatConfiguration;
