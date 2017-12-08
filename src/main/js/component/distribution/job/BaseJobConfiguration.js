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
		 	frequencyValue: [],
		 	notificationValue: [],
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
        this.handleFrequencyChanged = this.handleFrequencyChanged.bind(this);
        this.handleNotificationChanged = this.handleNotificationChanged.bind(this);
	}

	handleFrequencyChanged (optionsList) {
		console.log('You\'ve selected:', optionsList);
		this.setState({
			frequencyValue: optionsList
		});
	}

	handleNotificationChanged (optionsList) {
		console.log('You\'ve selected:', optionsList);
		this.setState({
			notificationValue: optionsList
		});
	}

	render(content) {
		return(
			<div>
				<div className={styles.contentBlock}>
					<TextInput label="Job Name" name="jobName" value={this.props.jobName} onChange={this.props.handleJobNameChange} errorName="jobNameError" errorValue={this.props.jobNameError}></TextInput>
					{content}
					<div>
						<label className={fieldLabel}>Frequency</label>
						<Typeahead className={typeAheadField}
							onChange={this.handleFrequencyChanged}
						    clearButton
						    options={this.state.frequencyOptions}
						    placeholder='Choose the frequency'
						    selected={this.state.frequencyValue}
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
						    selected={this.state.notificationValue}
						  />
					</div>
				</div>
				<ProjectConfiguration projects={this.props.projects} projectTableMessage={this.props.projectTableMessage} />
				<ConfigButtons includeCancel='true' onCancelClick={this.props.handleCancel} onClick={this.props.handleCancel} />
			</div>
		)
	}
}
