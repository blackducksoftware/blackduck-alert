import React, { Component } from 'react';
import PropTypes from 'prop-types';
import styles from '../../../../css/distributionConfig.css';
import {fieldLabel, typeAheadField} from '../../../../css/field.css';

import TextInput from '../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';

import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

import ConfigButtons from '../../ConfigButtons'

class BaseJobConfiguration extends Component {
	constructor(props) {
		super(props);

		 this.state = {
		 	values: [],
		 	errors: [],
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
	}
    componentDidMount() {
        this.initializeValues();
    }

    initializeValues() {
        const { jobName, frequency, notificationTypeArray, includeAllProjects, projects, selectedProjects } = this.props;
        let values = this.state.values;
        values.jobName = jobName;
        let frequencyValue = this.state.frequencyOptions.find((option)=> {
            return option.value === frequency;
        });

        if (frequencyValue) {
            values.frequencyValue = frequencyValue.value;
        }

        let notificationValueArray = this.state.notificationOptions.filter((option) => {
            if (notificationTypeArray) {
                let includes = notificationTypeArray.includes(option.value);
                return includes;
            } else {
                return false;
            }
        });
        values.includeAllProjects = includeAllProjects;
        if (notificationValueArray) {
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

	handleFrequencyChanged (option) {
        if(option) {
	        this.handleStateValues('frequencyValue', option.value);
        } else {
            this.handleStateValues('frequencyValue', option);
        }
	}

	handleNotificationChanged (optionsList) {
		this.handleSelectedArrayChanged('notificationValue', optionsList);
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

	render(content) {
		var buttonsFixed = this.props.buttonsFixed || false;
		return(
			<div>
				<form onSubmit={this.props.handleCancel}>
					<div className={styles.contentBlock}>
						<TextInput label="Job Name" name="jobName" value={this.state.values.jobName} onChange={this.handleChange} errorName="jobNameError"></TextInput>
						{content}
						<div>
							<label className={fieldLabel}>Frequency</label>
							<Select className={typeAheadField}
								onChange={this.handleFrequencyChanged}
							    clearButton
							    options={this.state.frequencyOptions}
							    placeholder='Choose the frequency'
							    value={this.state.values.frequencyValue}
							  />
						</div>
						<div>
							<label className={fieldLabel}>Notification Types</label>
							<Select className={typeAheadField}
								onChange={this.handleNotificationChanged}
							    clearble={true}
							    multi
                                removeSelected={true}
							    options={this.state.notificationOptions}
							    placeholder='Choose the notification types'
							    value={this.state.values.notificationValue}
							  />
						</div>
					</div>
					<ProjectConfiguration includeAllProjects={this.state.values.includeAllProjects} handleChange={this.handleChange} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} selectedProjects={this.props.selectedProjects} projectTableMessage={this.props.projectTableMessage} />
					<ConfigButtons isFixed={buttonsFixed} includeTest={true} includeCancel={true} onCancelClick={this.props.handleCancel}  type="submit" />
				</form>
			</div>
		)
	}
}

export default BaseJobConfiguration;
