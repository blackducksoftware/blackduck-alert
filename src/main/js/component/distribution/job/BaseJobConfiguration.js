import React from 'react';

import styles from '../../../../css/distributionConfig.css';

import ProjectConfiguration from '../ProjectConfiguration';

import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

export default class BaseJobConfiguration extends React.Component {
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
					{content}
					<Typeahead
						onChange={this.handleFrequencyChanged}
					    clearButton
					    options={this.state.frequencyOptions}
					    placeholder='Choose the frequency'
					    selected={this.state.frequencyValue}
					  />
					<Typeahead
						onChange={this.handleNotificationChanged}
					    clearButton
					    multiple
					    options={this.state.notificationOptions}
					    placeholder='Choose the notification types'
					    selected={this.state.notificationValue}
					  />
				</div>
				<ProjectConfiguration projects={this.props.projects} projectTableMessage={this.props.projectTableMessage} />
			</div>
		)
	}
}
