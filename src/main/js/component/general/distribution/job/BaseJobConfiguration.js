import React, { Component } from 'react';
import { connect } from 'react-redux';
import Select from 'react-select-2';

import TextInput from '../../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';
import ConfigButtons from '../../../common/ConfigButtons'

class BaseJobConfiguration extends Component {
	constructor(props) {
		super(props);
		 this.state = {
		 	values: {},
		 	errors: {},
            frequencyOptions: [
				{ label: 'Real Time', value: 'REAL_TIME'},
				{ label: 'Daily', value: 'DAILY' }
			],
            notificationOptions: [
				{ label: 'Policy Violation', value: 'POLICY_VIOLATION' },
				{ label: 'Policy Violation Cleared', value: 'POLICY_VIOLATION_CLEARED'},
				{ label: 'Policy Violation Override', value: 'POLICY_VIOLATION_OVERRIDE'},
				{ label: 'High Vulnerability', value: 'HIGH_VULNERABILITY'},
				{ label: 'Medium Vulnerability', value: 'MEDIUM_VULNERABILITY'},
				{ label: 'Low Vulnerability', value: 'LOW_VULNERABILITY'}
			]
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.handleFrequencyChanged = this.handleFrequencyChanged.bind(this);
        this.handleNotificationChanged = this.handleNotificationChanged.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleProjectChanged = this.handleProjectChanged.bind(this);
		this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
	}

    componentDidMount() {
        const { distributionConfigId } = this.props;
        this.readDistributionJobConfiguration(distributionConfigId);
    }

    readDistributionJobConfiguration(distributionId) {
        if(distributionId) {
            let urlString = this.props.getUrl || this.props.baseUrl;
            let getUrl = `${urlString}?id=${distributionId}`
            let self = this;
						const csrfToken = this.props.csrfToken;
            fetch(getUrl,{
    						credentials: "same-origin",
                headers: {
    							'Content-Type': 'application/json',
									'X-CSRF-TOKEN': csrfToken
    			}
    		})
    		.then(function(response) {
    			if (response.ok) {
                    response.json().then(jsonArray => {
                        if(jsonArray && jsonArray.length > 0) {
                            self.initializeValues(jsonArray[0]);
                        } else {
                            self.initializeValues(self.props);
                        }
                    });
                } else {
                    self.initializeValues(self.props);
                }
            })
            .catch(function(error) {
 		 		console.log(error);
 			});
        } else {
            this.initializeValues(this.props)
        }
    }

    initializeValues(data) {
        const { id, distributionConfigId, name, distributionType, frequency, notificationTypes, includeAllProjects, filterByProject, projects, configuredProjects } = data;
        let values = this.state.values;
        values.id = id;
        values.distributionConfigId = distributionConfigId;
        values.name = name;
        values.distributionType = distributionType;
        let frequencyFound = this.state.frequencyOptions.find((option)=> {
            return option.value === frequency;
        });

        if (frequencyFound) {
            values.frequency = frequencyFound.value;
        }
        if (includeAllProjects) {
        	values.includeAllProjects = includeAllProjects === true || includeAllProjects === 'true';
        } else if (filterByProject) {
        	values.includeAllProjects = (filterByProject == 'false');
	    }
        if (notificationTypes) {
            values.notificationTypes  = notificationTypes;
        }

        values.configuredProjects = configuredProjects;

        this.setState({values});
    }

    async handleSubmit(event) {
		this.setState({
			configurationMessage: 'Saving...',
			inProgress: true,
			errors: {}
		});
		if (event) {
			event.preventDefault();
		}

		var configuration = Object.assign({}, this.state.values);
		configuration.filterByProject = !configuration.includeAllProjects;
		configuration.includeAllProjects = null;
		if (configuration.notificationTypes && configuration.notificationTypes.length > 0) {
			configuration.notificationTypes = configuration.notificationTypes;
		} else {
			configuration.notificationTypes = null;
		}

		var self = this;
		let jsonBody = JSON.stringify(configuration);
		var method = 'POST';
		if (this.state.values.id) {
			method = 'PUT';
		}
		const csrfToken = this.props.csrfToken;
		return fetch(this.props.baseUrl, {
			method: method,
			credentials: "same-origin",
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-TOKEN': csrfToken
			},
			body: jsonBody
		}).then(function(response) {
			self.setState({
				inProgress: false
			});
			if (response.ok) {
				return response.json().then(json => {
					self.setState({
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
						self.setState({
							configurationMessage: json.message
						});
					} else {
						self.setState({
							configurationMessage: json.error
						});
					}
				});
			}
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
	}

	handleTestSubmit(event) {
		this.setState({
			configurationMessage: 'Testing...',
			inProgress: true,
			errors: {}
		});

		if (event) {
			event.preventDefault();
		}

		var configuration = Object.assign({}, this.state.values);
		configuration.filterByProject = !configuration.includeAllProjects;
		configuration.includeAllProjects = null;
		if (configuration.notificationTypes && configuration.notificationTypes.length > 0) {
			configuration.notificationTypes = configuration.notificationTypes;
		} else {
			configuration.notificationTypes = null;
		}

		var self = this;
		let jsonBody = JSON.stringify(configuration);
		const csrfToken = this.props.csrfToken;
		fetch(this.props.testUrl, {
			method: 'POST',
			credentials: "same-origin",
			headers: {
				'Content-Type': 'application/json',
				'X-CSRF-TOKEN': csrfToken
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
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
	}

	handleChange(event) {
		const target = event.target;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		const name = target.name;
		this.handleStateValues(name, value);
	}

	handleStateValues(name, value) {
		var values = this.state.values;
		values[name] = value;
		this.setState({
			values
		});
	}

	handleErrorValues(name, value) {
		var errors = this.state.errors;
		errors[name] = value;
		this.setState({
			errors
		});
	}

	handleSetState(name, value) {
		this.setState({
			[name] : value
		});
	}

	handleFrequencyChanged (option) {
        if(option) {
	        this.handleStateValues('frequency', option.value);
        } else {
            this.handleStateValues('frequency', option);
        }
	}

	handleNotificationChanged (selectedValues) {
		let selected = new Array();
        if(selectedValues && selectedValues.length > 0) {
            selected = selectedValues.map((item) => {
                return item.value;
            });
        }
        this.handleStateValues('notificationTypes', selected);
	}

    handleProjectChanged(selectedValues) {
    	let selected = new Array();
        if(selectedValues && selectedValues.length > 0) {
            selected = selectedValues.map((item) => {
                return item.value;
            });
        }
        this.handleStateValues('configuredProjects', selected);
    }

    async onSubmit(event) {
    	event.preventDefault();
        const { handleSaveBtnClick, handleCancel } = this.props;

        var jobName = null;
		if (this.state.values && this.state.values.name) {
			var trimmedName = this.state.values.name.trim();
			if (trimmedName.length > 0) {
				jobName = trimmedName;
			}
		}
		if (!jobName) {
			this.handleErrorValues('nameError', 'You must provide a Job name');
		} else {
			this.handleErrorValues('nameError', '');
			await this.handleSubmit();
			if (handleSaveBtnClick) {
				handleSaveBtnClick(this.state.values);
			} else if (handleCancel) {
				handleCancel();
			}
		}
    }

	render(content) {
		var buttonsFixed = this.props.buttonsFixed || false;
		return(
				<form className="form-horizontal" onSubmit={this.onSubmit}>
						<TextInput label="Job Name" name="name" value={this.state.values.name} onChange={this.handleChange} errorName="nameError" errorValue={this.state.errors.nameError}></TextInput>
						<div className="form-group">
							<label className="col-sm-3 control-label">Frequency</label>
							<div className="col-sm-8">
								<Select className="typeAheadField"
									onChange={this.handleFrequencyChanged}
									searchable={true}
									options={this.state.frequencyOptions}
									placeholder='Choose the frequency'
									value={this.state.values.frequency}
								  />
							</div>
						</div>
						<div className="form-group">
                            <label className="col-sm-3 control-label">Notification Types</label>
                            <div className="col-sm-8">
								<Select className="typeAheadField"
									onChange={this.handleNotificationChanged}
									searchable={true}
									multi
									removeSelected={true}
									options={this.state.notificationOptions}
									placeholder='Choose the notification types'
									value={this.state.values.notificationTypes}
								  />
							</div>
						</div>
						{content}
					<ProjectConfiguration includeAllProjects={this.state.values.includeAllProjects} handleChange={this.handleChange} handleProjectChanged={this.handleProjectChanged} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} configuredProjects={this.state.values.configuredProjects} projectTableMessage={this.props.projectTableMessage} />
					<ConfigButtons includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel} />
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</form>
		)
	}
}

const mapStateToProps = state => ({
	csrfToken: state.session.csrfToken
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(BaseJobConfiguration);
