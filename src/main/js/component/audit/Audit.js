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
		this.addDefaultEntries = this.addDefaultEntries.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.onStatusFailureClick = this.onStatusFailureClick.bind(this);
        this.statusColumnDataFormat = this.statusColumnDataFormat.bind(this);
	}

	addDefaultEntries() {
        const { entries } = this.state;
        entries.push({
            id: '999',
            jobName: 'Test Job',
            eventType: 'email_group_channel',
            notificationType: 'High Vulnerability',
            timeCreated: '12/01/2017 00:00:00',
            timeLastSent: '12/01/2017 00:00:00',
            status: 'Success'
        });
        entries.push({
            id: '111',
            jobName: 'Test Hipchat',
            eventType: 'hipchat_channel',
            notificationType: 'High Vulnerability',
            timeCreated: '12/01/2017 00:00:00',
            timeLastSent: '12/01/2017 00:00:00',
            status: 'Failure',
            errorMessage: 'Could not reach Hipchat',
            errorStackTrace: 'Exception : could not reach hipchat \n at someClass(line:55) \n at someClass(line:55) \n at someClass(line:55) \n at someClass(line:55) \n at someClass(line:55)'
        });
        this.setState({
			entries
		});
    }

    componentWillMount() {
		this.setState({
			message: 'Loading...',
			inProgress: true
		});
	}

	componentDidMount() {
		var self = this;
		fetch('/audit',{
			credentials: "same-origin"
		})
		.then(function(response) {
			self.handleSetState('inProgress', false);
			self.handleSetState('waitingForProjects', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('message', json.message);
				});
			} else {
				return response.json().then(jsonArray => {
					self.handleSetState('message', '');
					if (jsonArray != null && jsonArray.length > 0) {
						var projects = [];
						for (var index in jsonArray) {
							var newProject = {};
							newProject.id = jsonArray[index].id;
				            newProject.jobName = jsonArray[index].name;
				            newProject.eventType = jsonArray[index].eventType;
				            newProject.timeCreated = jsonArray[index].timeCreated;
				            newProject.timeLastSent = jsonArray[index].timeLastSent;
				            newProject.status =  jsonArray[index].status;
				            newProject.errorMessage =  jsonArray[index].errorMessage;
				            newProject.errorStackTrace = jsonArray[index].errorStackTrace;
							if (jsonArray[index].notification) {
					            newProject.notificationType = jsonArray[index].notification.notificationType;
					            newProject.notificationProjectName = jsonArray[index].notification.projectName;
								newProject.notificationProjectVersion = jsonArray[index].notification.projectVersion;
								newProject.notificationComponentName = jsonArray[index].notification.componentName;
								newProject.notificationComponentVersion = jsonArray[index].notification.componentVersion;
								newProject.notificationPolicyRuleName = jsonArray[index].notification.policyRuleName;
							}
							projects.push(newProject);
						}
						self.setState({
							projects
						});
					}
				});
			}
		});

		this.addDefaultEntries();
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
		console.log(currentRowSelected);
		var currentEntry = currentRowSelected;
		if (!currentRowSelected){
			currentEntry = this.state.currentRowSelected;
		}
		console.log(currentEntry);
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

	isExpandableRow(row) {
    	return true;
  	}

	expandComponent(row) {
		return <AuditDetails currentEntry={row}/>;
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
						<BootstrapTable data={this.state.entries} expandableRow={this.isExpandableRow} expandComponent={this.expandComponent} containerClass={tableStyles.table} striped hover condensed search={true} options={auditTableOptions} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='id' isKey hidden>Audit Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='jobName' dataSort columnClassName={tableStyles.tableCell}>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='eventType' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.typeColumnDataFormat }>Event Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationType' dataSort columnClassName={tableStyles.tableCell}>Notification Type</TableHeaderColumn>
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
