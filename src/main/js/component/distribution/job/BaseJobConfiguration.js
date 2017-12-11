import React, { Component } from 'react';
import PropTypes from 'prop-types';
import styles from '../../../../css/distributionConfig.css';
import {fieldLabel, typeAheadField} from '../../../../css/field.css';

import TextInput from '../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';

import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

import ConfigButtons from '../../ConfigButtons'

class BaseJobConfiguration extends Component {
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
				{ label: 'Low Vulnerability', id: 'LOW_VULNERABILITY'}
			]
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.handleFrequencyChanged = this.handleFrequencyChanged.bind(this);
        this.handleNotificationChanged = this.handleNotificationChanged.bind(this);
	}
    componentDidMount() {
        this.initializeValues();
    }

    initializeValues() {
        const { jobName, frequency, notificationTypeArray } = this.props;
        let values = this.state.values;
        values.jobName = jobName;
        let frequencyValue = this.state.frequencyOptions.find((option)=> {
            return option.id === frequency;
        });

        if(frequencyValue) {
            values.frequencyValue = [frequencyValue];
        }

        let notificationValueArray = this.state.notificationOptions.filter((option) => {
            let includes = notificationTypeArray.includes(option.id);
            return includes;
        });

        if(notificationValueArray) {
            values.notificationValue  = notificationValueArray;
        }
        this.setState({values});
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

	handleSetState(name, value) {
		this.setState({
			[name] : value
		});
	}

	handleFrequencyChanged (optionsList) {
		this.handleStateValues('frequencyValue', optionsList);
	}

	handleNotificationChanged (optionsList) {
		this.handleStateValues('notificationValue', optionsList);
	}

	render(content) {
		var buttonsFixed = this.props.buttonsFixed || "true";
		return(
			<div>
				<form onSubmit={this.props.handleCancel}>
					<div className={styles.contentBlock}>
						<TextInput label="Job Name" name="jobName" value={this.state.values.jobName} onChange={this.handleChange} errorName="jobNameError"></TextInput>
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
					<ProjectConfiguration waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} projectTableMessage={this.props.projectTableMessage} />
					<ConfigButtons isFixed={buttonsFixed} includeCancel='true' onCancelClick={this.props.handleCancel}  type="submit" />
				</form>
			</div>
		)
	}
}

export default BaseJobConfiguration;
