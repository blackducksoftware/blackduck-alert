import React, { Component } from 'react';

import TextInput from '../../../field/input/TextInput';
import LabeledField from '../../../field/LabeledField';
import TextArea from '../../../field/input/TextArea';

import {ReactBsTable, BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

class Details extends Component {
	constructor(props) {
		super(props);

		const { currentEntry } = props
		const values = {};
		values.notificationProjectName = currentEntry.notificationProjectName;
		values.notificationProjectVersion = currentEntry.notificationProjectVersion;
		values.components = currentEntry.components;

		values.eventType = currentEntry.eventType;

		values.errorMessage = currentEntry.errorMessage;
		values.errorStackTrace = currentEntry.errorStackTrace;
		this.state = {
			message: '',
			values
		};
	}

	getEventType() {
		let fontAwesomeClass = "";
        let cellText = '';
		if (this.state.values.eventType === 'email_group_channel') {
			fontAwesomeClass = `fa fa-envelope fa-fw`;
            cellText = "Group Email";
		} else if (this.state.values.eventType === 'hipchat_channel') {
			fontAwesomeClass = `fa fa-comments fa-fw`;
            cellText = "HipChat";
		} else if (this.state.values.eventType === 'slack_channel') {
			fontAwesomeClass = `fa fa-slack  fa-fw`;
            cellText = "Slack";
		}

		const eventType = <div className="inline">
			<span key="icon" className={fontAwesomeClass} aria-hidden='true'></span>
				{cellText}
			</div>;

		return <LabeledField label="Event Type" field={eventType} />;
	}


	render(content) {
		let notificationProjectVersion = null;
		if (this.state.values.notificationProjectVersion) {
			notificationProjectVersion = <TextInput label="Project Version" readOnly={true} name="notificationProjectVersion" value={this.state.values.notificationProjectVersion}></TextInput>;
		}
		const eventType = this.getEventType();

		let errorMessage = null;
		if (this.state.values.errorMessage) {
			errorMessage = <TextInput label="Error" readOnly={true} name="errorMessage" value={this.state.values.errorMessage}></TextInput>;
		}
		let errorStackTrace = null;
		if (this.state.values.errorStackTrace) {
			errorStackTrace = <TextArea label="Stack Trace" readOnly={true} name="errorStackTrace" value={this.state.values.errorStackTrace}></TextArea>;
		}

		return(
			<div className="expandableContainer">
				{notificationProjectVersion}
				{eventType}
				<BootstrapTable data={this.state.values.components} containerClass="auditDetailsTable" striped condensed trClassName={this.assignClassName} headerContainerClass="scrollable" bodyContainerClass="auditDetailsTableBody">
                    <TableHeaderColumn dataField='componentName' isKey dataSort columnTitle columnClassName="tableCell">Component</TableHeaderColumn>
                    <TableHeaderColumn dataField='componentVersion' dataSort columnTitle columnClassName="tableCell">Version</TableHeaderColumn>
                    <TableHeaderColumn dataField='policyRuleName'  dataSort columnTitle columnClassName="tableCell">Policy Rule</TableHeaderColumn>
                </BootstrapTable>
				{errorMessage}
				{errorStackTrace}
			</div>
		)
	}
};
export default Details;
