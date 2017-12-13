import React, { Component } from 'react';
import PropTypes from 'prop-types';
import styles from '../../../../css/distributionConfig.css';
import {fieldLabel, typeAheadField} from '../../../../css/field.css';

import TextInput from '../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

import ConfigButtons from '../../ConfigButtons'

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
        this.initializeValues();
    }

    initializeValues() {
        const { name, distributionType, frequency, notificationType, includeAllProjects, projects, configuredProjects } = this.props;
        let values = this.state.values;
        values.name = name;
        values.distributionType = distributionType;
        let frequencyFound = this.state.frequencyOptions.find((option)=> {
            return option.value === frequency;
        });

        if (frequencyFound) {
            values.frequency = frequencyFound.value;
        }

        values.includeAllProjects = includeAllProjects;
        if (notificationType) {
            values.notificationType  = notificationType;
        }

        values.configuredProjects = configuredProjects;

        this.setState({values});
    }

    handleSubmit(event) {
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
		if (configuration.notificationType && configuration.notificationType.length > 0) {
			configuration.notificationType = configuration.notificationType[0];
		} else {
			configuration.notificationType = null;
		}

		var self = this;
		let jsonBody = JSON.stringify(configuration);
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
					var values = {};
					values.id = json.id;
					values.distributionConfigId = json.distributionConfigId;
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

		if (event) {
			event.preventDefault();
		}

		var configuration = Object.assign({}, this.state.values);
		configuration.filterByProject = !configuration.includeAllProjects;
		configuration.includeAllProjects = null;
		if (configuration.notificationType && configuration.notificationType.length > 0) {
			configuration.notificationType = configuration.notificationType[0];
		} else {
			configuration.notificationType = null;
		}

		var self = this;
		let jsonBody = JSON.stringify(configuration);
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

	handleNotificationChanged (optionsList) {
		this.handleSelectedArrayChanged('notificationType', optionsList);
	}

    handleProjectChanged(projectList) {
        this.handleSelectedArrayChanged('configuredProjects', projectList);
    }

    handleSelectedArrayChanged(stateKey, selectedValues) {
        let selected = new Array();
        if(selectedValues && selectedValues.length > 0) {
            selected = selectedValues.map((item) => {
                return item.value;
            });
        }
        this.handleStateValues(stateKey, selected);
    }

    onSubmit(event) {
        const { handleSaveBtnClick, handleCancel } = this.props;

        var jobName = null;
		if (this.state.values && this.state.values.name) {
			var trimmedName = this.state.values.name.trim();
			if (trimmedName.length > 0) {
				jobName = trimmedName;
			}
		}
		if (!jobName) {
			event.preventDefault();
			this.handleErrorValues('nameError', 'You must provide a Job name');
		} else {
			this.handleErrorValues('nameError', '');
			this.handleSubmit();
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
			<div>
				<form onSubmit={this.onSubmit}>
					<div className={styles.contentBlock}>
						<TextInput label="Job Name" name="name" value={this.state.values.name} onChange={this.handleChange} errorName="nameError" errorValue={this.state.errors.nameError}></TextInput>
						<div>
							<label className={fieldLabel}>Frequency</label>
							<Select className={typeAheadField}
								onChange={this.handleFrequencyChanged}
							    clearble={true}
                                searchable={true}
							    options={this.state.frequencyOptions}
							    placeholder='Choose the frequency'
							    value={this.state.values.frequency}
							  />
						</div>
						<div>
							<label className={fieldLabel}>Notification Types</label>
							<Select className={typeAheadField}
								onChange={this.handleNotificationChanged}
							    clearble={true}
                                searchable={true}
							    multi
                                removeSelected={true}
							    options={this.state.notificationOptions}
							    placeholder='Choose the notification types'
							    value={this.state.values.notificationType}
							  />
						</div>
						{content}
					</div>
					<ProjectConfiguration includeAllProjects={this.state.values.includeAllProjects} handleChange={this.handleChange} handleProjectChanged={this.handleProjectChanged} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} configuredProjects={this.props.configuredProjects} projectTableMessage={this.props.projectTableMessage} />
					<ConfigButtons isFixed={buttonsFixed} includeTest={true} includeCancel={true} onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel} type="submit" />
					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</form>
			</div>
		)
	}
}

export default BaseJobConfiguration;
