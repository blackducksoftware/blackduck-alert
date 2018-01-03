import React, { Component } from 'react';

import { progressIcon } from '../../../css/main.css';
import tableStyles from '../../../css/table.css';

import EditTableCellFormatter from '../EditTableCellFormatter';
import AuditDetails from './AuditDetails';

import Modal from 'react-modal';

import {ReactBsTable, BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

class Audit extends Component {
	constructor(props) {
		super(props);
		 this.state = {
			message: '',
			entries: [],
			modal: undefined
		};
		// this.addDefaultEntries = this.addDefaultEntries.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.reloadAuditEntries = this.reloadAuditEntries.bind(this);
        this.setEntriesFromArray = this.setEntriesFromArray.bind(this);
        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.onStatusFailureClick = this.onStatusFailureClick.bind(this);
        this.statusColumnDataFormat = this.statusColumnDataFormat.bind(this);
	}

	// addDefaultEntries() {
 //        const { entries } = this.state;
 //        entries.push({
 //            id: '999',
 //            jobName: 'Test Job',
 //            eventType: 'email_group_channel',
 //            notificationType: 'High Vulnerability',
 //            timeCreated: '12/01/2017 00:00:00',
 //            timeLastSent: '12/01/2017 00:00:00',
 //            status: 'Success'
 //        });
 //        entries.push({
 //            id: '111',
 //            jobName: 'Test Hipchat',
 //            eventType: 'hipchat_channel',
 //            notificationType: 'High Vulnerability',
 //            timeCreated: '12/01/2017 00:00:00',
 //            timeLastSent: '12/01/2017 00:00:00',
 //            status: 'Failure',
 //            errorMessage: 'Could not reach Hipchat',
 //            errorStackTrace: 'Exception : could not reach hipchat \n at someClass(line:55) \n at someClass(line:55) \n at someClass(line:55) \n at someClass(line:55) \n at someClass(line:55)'
 //        });
 //        this.setState({
	// 		entries
	// 	});
 //    }

    componentWillMount() {
		this.setState({
			message: 'Loading...',
			inProgress: true
		});
	}

	componentDidMount() {
		// run the reload now and then every 10 seconds
		this.reloadAuditEntries();
		let reloadInterval = setInterval(() => this.reloadAuditEntries(), 10000);
		this.handleSetState('reloadInterval', reloadInterval);
	}

	componentWillUnmount() {
		 clearInterval(this.state.reloadInterval);
	}

	reloadAuditEntries(){
		var self = this;
		fetch('/audit',{
			credentials: "same-origin"
		})
		.then(function(response) {
			self.handleSetState('inProgress', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('message', json.message);
				});
			} else {
				return response.json().then(jsonArray => {
					self.handleSetState('message', '');
					self.setEntriesFromArray(jsonArray);
				});
			}
		});
	}

	setEntriesFromArray(jsonArray) {
		if (jsonArray != null && jsonArray.length > 0) {
			var entries = [];
			for (var index in jsonArray) {
				var newEntry = {};
				newEntry.id = jsonArray[index].id;
	            newEntry.jobName = jsonArray[index].name;
	            newEntry.eventType = jsonArray[index].eventType;
	            newEntry.timeCreated = jsonArray[index].timeCreated;
	            newEntry.timeLastSent = jsonArray[index].timeLastSent;
	            newEntry.status =  jsonArray[index].status;
	            newEntry.errorMessage =  jsonArray[index].errorMessage;
	            newEntry.errorStackTrace = jsonArray[index].errorStackTrace;
				if (jsonArray[index].notification) {
					newEntry.notificationTypes = jsonArray[index].notification.notificationTypes;
		            newEntry.notificationProjectName = jsonArray[index].notification.projectName;
					newEntry.notificationProjectVersion = jsonArray[index].notification.projectVersion;
					newEntry.notificationComponentName = jsonArray[index].notification.componentName;
					newEntry.notificationComponentVersion = jsonArray[index].notification.componentVersion;
					newEntry.notificationPolicyRuleName = jsonArray[index].notification.policyRuleName;
				}
				entries.push(newEntry);
			}
			this.setState({
				entries
			});
		}
	}



	handleSetState(name, value) {
		this.setState({
			[name]: value
		});
	}

	cancelRowSelect() {
		this.setState({
			currentRowSelected: null
		});
	}

	onResendClick(currentRowSelected){
		var currentEntry = currentRowSelected;
		if (!currentRowSelected){
			currentEntry = this.state.currentRowSelected;
		}

		this.setState({
			message: 'Sending...',
			inProgress: true
		});

		var self = this;		
		var resendUrl = '/audit/' + currentEntry.id + '/resend';
		fetch(resendUrl, {
			method: 'POST',
			credentials: "same-origin",
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(function(response) {
			self.handleSetState('inProgress', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('message', json.message);
				});
			}  else {
				return response.json().then(json => {
					self.handleSetState('message', '');
					var jsonArray = JSON.parse(json.message);
					self.setEntriesFromArray(jsonArray);
				});
			}
		});
	}

	resendButton(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText='Re-send' />;
    }

    onStatusFailureClick(currentRowSelected){
    	this.handleSetState('currentRowSelected', currentRowSelected);
    }

    statusColumnDataFormat(cell, row) {
		var statusClass = tableStyles.statusSuccess;
		if (cell === 'Failure') {
			statusClass = tableStyles.statusFailure;
		}
		let data = <div className={statusClass} aria-hidden='true'>
						{cell}
					</div>;

		return data;
	}

	typeColumnDataFormat(cell, row) {
		let fontAwesomeClass = "";
        let cellText = '';
		if (cell === 'email_group_channel') {
			fontAwesomeClass = 'fa fa-envelope';
            cellText = "Group Email";
		} else if (cell === 'hipchat_channel') {
			fontAwesomeClass = 'fa fa-comments';
            cellText = "HipChat";
		} else if (cell === 'slack_channel') {
			fontAwesomeClass = 'fa fa-slack';
            cellText = "Slack";
		}

		let data = <div>
						<i key="icon" className={fontAwesomeClass} aria-hidden='true'></i>
						{cellText}
					</div>;

		return data;
	}

	notificationTypeDataFormat(cell, row) {
		if (cell && cell.length > 0) {
			let cellText = '';
			for (var i in cell) {
				if (cell[i] === "POLICY_VIOLATION") {
					cellText = cellText + " PV";
				} else if (cell[i] === "POLICY_VIOLATION_CLEARED") {
					cellText = cellText + " PVC";
				} else if (cell[i] === "POLICY_VIOLATION_OVERRIDE") {
					cellText = cellText + " PVO";
				} else if (cell[i] === "HIGH_VULNERABILITY") {
					cellText = cellText + " HV";
				} else if (cell[i] === "MEDIUM_VULNERABILITY") {
					cellText = cellText + " MV";
				} else if (cell[i] === "LOW_VULNERABILITY") {
					cellText = cellText + " LV";
				} else if (cell[i] === "VULNERABILITY") {
					cellText = cellText + " V";
				}
			}
			cellText = cellText.trim();			
			let data = <div>
						{cellText}
					</div>;
			return data;
		}
		return null;
	}

	isExpandableRow(row) {
    	return true;
  	}

	expandComponent(row) {
		return <AuditDetails currentEntry={row}/>;
	}

	trClassFormat(row, rowIndex) {
		// color the row correctly, since Striped does not work with expandable rows
		var isEven = rowIndex % 2 === 0;
		var className = isEven ? tableStyles.tableEvenRow : tableStyles.tableRow; 
		return className; 
	}



	render() {
		const auditTableOptions = {
	  		noDataText: 'No events',
	  		clearSearch: true,
	  		expandBy : 'column',
	  		expandRowBgColor: '#e8e8e8'
		};
		var progressIndicator = null;
        if (this.state.inProgress) {
            const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
            progressIndicator = <div className={progressIcon}>
                                    <i className={fontAwesomeIcon} aria-hidden='true'></i>
                                </div>;
        }
		return (
				<div>
					<div>
						<BootstrapTable trClassName={this.trClassFormat} hover condensed data={this.state.entries} expandableRow={this.isExpandableRow} expandComponent={this.expandComponent} containerClass={tableStyles.table} search={true} options={auditTableOptions} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='id' isKey hidden>Audit Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='jobName' dataSort columnClassName={tableStyles.tableCell}>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='eventType' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.typeColumnDataFormat }>Event Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationTypes' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.notificationTypeDataFormat }>Notification Types</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeCreated' dataSort columnClassName={tableStyles.tableCell}>Time Created</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeLastSent' dataSort columnClassName={tableStyles.tableCell}>Time Last Sent</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.statusColumnDataFormat }>Status</TableHeaderColumn>
	                        <TableHeaderColumn dataField='' expandable={ false } columnClassName={tableStyles.tableCell} dataFormat={ this.resendButton }></TableHeaderColumn>
	  					</BootstrapTable>
	  					{progressIndicator}
	  					<p name="message">{this.state.message}</p>
  					</div>
				</div>
		)
	}

};

export default Audit;
