import React, { Component } from 'react';

export default class Configuration extends Component {
    constructor(props) {
		super(props);
        this.state = {
			configurationMessage: '',
			errors: {},
			values: {}
		};
		this.handleChange = this.handleChange.bind(this);
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleTestSubmit = this.handleTestSubmit.bind(this);
	}

	componentWillMount() {
		let getUrl = this.props.getUrl || this.props.baseUrl;
		if (getUrl) {
			this.setState({
				configurationMessage: 'Loading...',
				inProgress: true,
			});
		}
		this.setState({
			errors: {},
			values: {}
		});
	}

    //componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		var self = this;

		let getUrl = this.props.getUrl || this.props.baseUrl;
		if (!getUrl) {
			return;
		}
		fetch(getUrl,{
			credentials: "same-origin"
		})
		.then(function(response) {
			self.setState({
				inProgress: false
			});
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
						var values = self.state.values;
						values.id = configuration.id;
						for (var key in configuration) {
							if (configuration.hasOwnProperty(key)) {
								let name = key;
								let value = configuration[key];
								if (value != null) {
									values[name] = value;
								}
							}
						}
						self.setState({
							values
						});
					}
				});
			}
		});
    }

    handleSubmit(event) {
		this.setState({
			configurationMessage: 'Saving...',
			inProgress: true,
			errors: {}
		});
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state.values);
		var method = 'POST';
		if (this.state.values.id) {
			method = 'PUT';
		}
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
				return response.json().then(json => {
					var values = self.state.values;
					values.id = json.id;
					self.setState({
						values,
						configurationMessage: json.message
					});
				});
			} else {
				return response.json().then(json => {
					let jsonErrors = json.errors;
					if (jsonErrors) {
						var errors = {};
						for (var key in jsonErrors) {
							if (jsonErrors.hasOwnProperty(key)) {
								let name = key.concat('Error');
								let value = jsonErrors[key];
								errors[name] = value;
							}
						}
						self.setState({
							errors
						});
					}
					self.setState({
						configurationMessage: json.message
					});
				});
			}
		});
	}

	handleTestSubmit(event) {
		this.setState({
			configurationMessage: 'Testing...',
			inProgress: true,
			errors: {}
		});
		event.preventDefault();
		var self = this;
		let jsonBody = JSON.stringify(this.state.values);
		fetch(this.props.testUrl, {
			method: 'POST',
			credentials: "same-origin",
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonBody
		}).then(function(response) {
			self.setState({
				inProgress: false
			});
			return response.json().then(json => {
				let jsonErrors = json.errors;
				if (jsonErrors) {
					var errors = {};
					for (var key in jsonErrors) {
						if (jsonErrors.hasOwnProperty(key)) {
							let name = key.concat('Error');
							let value = jsonErrors[key];
							errors[name] = value;
						}
					}
					self.setState({
						errors
					});
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

		var values = this.state.values;
		values[name] = value;
		this.setState({
			values
		});
	}
}
