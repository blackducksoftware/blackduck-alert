import React, { Component } from 'react';

import styles from '../../../../css/distributionConfig.css';
import {fieldLabel, typeAheadField} from '../../../../css/field.css';

import TextInput from '../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';

import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

import ConfigButtons from '../../ConfigButtons'

export default class BaseJobConfiguration extends Component {
	constructor(props) {
		super(props);
		 this.state = {
		 	values: [],
		 	errors: [],
            frequencyOptions: [
				{ label: 'Real Time', id: 'REAL_TIME'},
				{ label: 'Daily', id: 'DAILY' }
			],
            notificationOptions: [
				{ label: 'Policy Violation', id: 'POLICY_VIOLATION' },
				{ label: 'Policy Violation Cleared', id: 'POLICY_VIOLATION_CLEARED'},
				{ label: 'Policy Violation Override', id: 'POLICY_VIOLATION_OVERRIDE'},
				{ label: 'High Vulnerability', id: 'HIGH_VULNERABILITY'},
				{ label: 'Medium Vulnerability', id: 'MEDIUM_VULNERABILITY'},
				{ label: 'Low Vulnerability', id: 'LOW_VULNERABILITY'},
				{ label: 'Vulnerability', id: 'VULNERABILITY'}
			]
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.handleFrequencyChanged = this.handleFrequencyChanged.bind(this);
        this.handleNotificationChanged = this.handleNotificationChanged.bind(this);
	}

	handleChange(event) {
		const target = event.target;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		const name = target.name;

		handleStateValues(name, value);
	}

	handleStateValues(name, value) {
		var values = this.state.values;
		values[name] = value;
		this.setState({
			values
		});
	}

	handleSetState(name, value) {
		this.setState({
			[name] : value
		});
	}

	handleFrequencyChanged (optionsList) {
		handleStateValues('frequencyValue', optionsList);
	}

	handleNotificationChanged (optionsList) {
		handleStateValues('notificationValue', optionsList);
	}

	render(content) {
		var buttonsFixed = this.props.buttonsFixed || "true";
		return(
			<div>
				<form onSubmit={this.props.handleCancel}>
					<div className={styles.contentBlock}>
						<TextInput label="Job Name" name="jobName" value={this.state.values.jobName} onChange={this.handleChange} errorName="jobNameError" errorValue={this.state.errors.jobNameError}></TextInput>
						{content}
						<div>
							<label className={fieldLabel}>Frequency</label>
							<Typeahead className={typeAheadField}
								onChange={this.handleFrequencyChanged}
							    clearButton
							    options={this.state.frequencyOptions}
							    placeholder='Choose the frequency'
							    selected={this.state.values.frequencyValue}
							  />
						</div>
						<div>
							<label className={fieldLabel}>Notification Types</label>
							<Typeahead className={typeAheadField}
								onChange={this.handleNotificationChanged}
							    clearButton
							    multiple
							    options={this.state.notificationOptions}
							    placeholder='Choose the notification types'
							    selected={this.state.values.notificationValue}
							  />
						</div>
					</div>
					<ProjectConfiguration projects={this.props.projects} projectTableMessage={this.props.projectTableMessage} />
					<ConfigButtons isFixed={buttonsFixed} includeCancel='true' onCancelClick={this.props.handleCancel}  type="submit" />
				</form>
			</div>
		)
	}
}
