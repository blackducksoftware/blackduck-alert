import React, { Component } from 'react';

import { fontAwesomeLabel } from '../../../css/main.css';
import { inline } from '../../../css/audit.css';
import tableStyles from '../../../css/table.css';

import TextInput from '../../field/input/TextInput';
import LabeledField from '../../field/LabeledField';
import TextArea from '../../field/input/TextArea';

import {ReactBsTable, BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

class AuditDetails extends Component {
	constructor(props) {
		super(props);
		var currentEntry = props.currentEntry;
		let values = {};
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
		var eventType = null;
		let fontAwesomeClass = "";
        let cellText = '';
		if (this.state.values.eventType === 'email_group_channel') {
			fontAwesomeClass = `fa fa-envelope ${fontAwesomeLabel}`;
            cellText = "Group Email";
		} else if (this.state.values.eventType === 'hipchat_channel') {
			fontAwesomeClass = `fa fa-comments ${fontAwesomeLabel}`;
            cellText = "HipChat";
		} else if (this.state.values.eventType === 'slack_channel') {
			fontAwesomeClass = `fa fa-slack ${fontAwesomeLabel}`;
            cellText = "Slack";
		}

		eventType = <div className={inline} >
						<i key="icon" className={fontAwesomeClass} aria-hidden='true'></i>
						{cellText}
					</div>;

		return <LabeledField label="Event Type" field={eventType} />;
	}


	render(content) {
		var notificationProjectVersion = null;
		if (this.state.values.notificationProjectVersion) {
			notificationProjectVersion = <TextInput label="Project Version" readOnly={true} name="notificationProjectVersion" value={this.state.values.notificationProjectVersion}></TextInput>;
		}
		var eventType = this.getEventType();

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
				{notificationProjectVersion}
				{eventType}
				<BootstrapTable data={this.state.values.components} containerClass={tableStyles.auditDetailsTable} striped hover condensed trClassName={this.assignClassName} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.auditDetailsTableBody} >
                    <TableHeaderColumn dataField='componentName' isKey dataSort columnTitle columnClassName={tableStyles.tableCell}>Component</TableHeaderColumn>
                    <TableHeaderColumn dataField='componentVersion' dataSort columnTitle columnClassName={tableStyles.tableCell}>Version</TableHeaderColumn>
                    <TableHeaderColumn dataField='policyRuleName'  dataSort columnTitle columnClassName={tableStyles.tableCell}>Policy Rule</TableHeaderColumn>
                </BootstrapTable>
				{errorMessage}
				{errorStackTrace}
			</div>
		)
	}
};
export default AuditDetails;
