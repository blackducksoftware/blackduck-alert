import React from 'react';

export default class Configuration extends React.Component {
    constructor(props) {
		super(props);
        this.state = {
            id: undefined,
            configurationMessage: ''
        };
	}

    resetMessageStates() {
		this.setState({
			configurationMessage: ''
		});
	}

	getState() {
		return this.state;
	}

    //componentDidMount is part of the Component lifecycle, executes after construction
	componentDidMount() {
		this.resetMessageStates();
		var self = this;
		self.setState({
			configurationMessage: 'Loading...'
		});
		fetch(this.props.restUrl)  
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
							id: configuration.id
						});
						for (var key in configuration) {
							if (configuration.hasOwnProperty(key)) {
								let name = key;
								let value = configuration[key];
								self.setState({
									[name]: value
								});
							}
						}
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
		fetch(this.props.restUrl, {
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
		fetch(this.props.testUrl, {
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
}