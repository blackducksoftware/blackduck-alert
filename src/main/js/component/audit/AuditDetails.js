import React, { Component } from 'react';

import tableStyles from '../../../css/table.css';

import TextInput from '../../field/input/TextInput';
import TextArea from '../../field/input/TextArea';

class AuditDetails extends Component {
	constructor(props) {
		super(props);
		var currentEntry = props.currentEntry;
		let values = {};
		values.notificationProjectName = currentEntry.notificationProjectName;
		values.notificationProjectVersion = currentEntry.notificationProjectVersion;
		values.notificationComponentName = currentEntry.notificationComponentName;
		values.notificationComponentVersion = currentEntry.notificationComponentVersion;
		values.notificationPolicyRuleName = currentEntry.notificationPolicyRuleName;

		values.errorMessage = currentEntry.errorMessage;
		values.errorStackTrace = currentEntry.errorStackTrace;
		this.state = {
			message: '',
			values
		};
	}

	render(content) {
		var notificationProjectName = null;
		if (this.state.values.notificationProjectName) {
			notificationProjectName = <TextInput label="Project Name" readOnly={true} name="notificationProjectName" value={this.state.values.notificationProjectName}></TextInput>;
		}
		var notificationProjectVersion = null;
		if (this.state.values.notificationProjectVersion) {
			notificationProjectVersion = <TextInput label="Project Version" readOnly={true} name="notificationProjectVersion" value={this.state.values.notificationProjectVersion}></TextInput>;
		}
		var notificationComponentName = null;
		if (this.state.values.notificationComponentName) {
			notificationComponentName = <TextInput label="Component Name" readOnly={true} name="notificationComponentName" value={this.state.values.notificationComponentName}></TextInput>;
		}
		var notificationComponentVersion = null;
		if (this.state.values.notificationComponentVersion) {
			notificationComponentVersion = <TextInput label="Component Version" readOnly={true} name="notificationComponentVersion" value={this.state.values.notificationComponentVersion}></TextInput>;
		}
		var notificationPolicyRuleName = null;
		if (this.state.values.notificationPolicyRuleName) {
			notificationPolicyRuleName = <TextInput label="Policy Rule Name" readOnly={true} name="notificationPolicyRuleName" value={this.state.values.notificationPolicyRuleName}></TextInput>;
		}

		var errorMessage = null;
		if (this.state.values.errorMessage) {
			errorMessage = <TextInput label="Error" readOnly={true} name="errorMessage" value={this.state.values.errorMessage}></TextInput>;
		}
		var errorStackTrace = null;
		if (this.state.values.errorStackTrace) {
			errorStackTrace = <TextArea label="Stack Trace" readOnly={true} name="errorStackTrace" value={this.state.values.errorStackTrace}></TextArea>;
		}
		return(
			<div className={tableStyles.expandableContainer}>
				{notificationProjectName}
				{notificationProjectVersion}
				{notificationComponentName}
				{notificationComponentVersion}
				{notificationPolicyRuleName}
				{errorMessage}
				{errorStackTrace}
			</div>
		)
	}
};
export default AuditDetails;