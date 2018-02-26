import React, { Component } from 'react';
import { connect } from "react-redux";

import { getAuditData } from '../../../store/actions/audit';

import EditTableCellFormatter from '../../common/EditTableCellFormatter';
import AuditDetails from './Details';
import {ReactBsTable, BootstrapTable, TableHeaderColumn, ButtonGroup} from 'react-bootstrap-table';

import { progressIcon, fontAwesomeLabel, refreshCheckbox } from '../../../../css/main.css';
import tableStyles from '../../../../css/table.css';
import auditStyles from '../../../../css/audit.css';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

var policyViolationIcon = <i key="policyViolationIcon" alt="Policy Violation" title="Policy Violation" className={`fa fa-ban ${auditStyles.policyViolation}`} aria-hidden='true'></i>;
var policyViolationClearedIcon = <i key="policyViolationClearedIcon" alt="Policy Violation Cleared" title="Policy Violation Cleared" className={`fa fa-eraser ${auditStyles.policyViolationCleared}`} aria-hidden='true'></i>;
var policyViolationOverrideIcon = <i key="policyViolationOverrideIcon" alt="Policy Override" title="Policy Override" className={`fa fa-exclamation-circle ${auditStyles.policyViolationOverride}`} aria-hidden='true'></i>;
var highVulnerabilityIcon = <i key="highVulnerabilityIcon" alt="High Vulnerability" title="High Vulnerability" className={`fa fa-shield ${auditStyles.highVulnerability}`} aria-hidden='true'></i>;
var mediumVulnerabilityIcon = <i key="mediumVulnerabilityIcon" alt="Medium Vulnerability" title="Medium Vulnerability" className={`fa fa-shield ${auditStyles.mediumVulnerability}`} aria-hidden='true'></i>;
var lowVulnerabilityIcon = <i key="lowVulnerabilityIcon" alt="Low Vulnerability" title="Low Vulnerability" className={`fa fa-shield ${auditStyles.lowVulnerability}`} aria-hidden='true'></i>;
var vulnerabilityIcon = <i key="vulnerabilityIcon" alt="Vulnerability" title="Vulnerability" className={`fa fa-shield ${auditStyles.vulnerability}`} aria-hidden='true'></i>;


class Index extends Component {
	constructor(props) {
		super(props);
		 this.state = {
		 	autoRefresh: true,
			message: '',
			entries: [],
			modal: undefined
		};
		// this.addDefaultEntries = this.addDefaultEntries.bind(this);
		this.handleAutoRefreshChange = this.handleAutoRefreshChange.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.reloadAuditEntries = this.reloadAuditEntries.bind(this);
        this.setEntriesFromArray = this.setEntriesFromArray.bind(this);
        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.onStatusFailureClick = this.onStatusFailureClick.bind(this);
        this.statusColumnDataFormat = this.statusColumnDataFormat.bind(this);
        this.createCustomButtonGroup = this.createCustomButtonGroup.bind(this);
	}

	componentDidMount() {
        this.props.getAuditData();
		//this.reloadAuditEntries();
		//this.startAutoReload();
	}

	startAutoReload() {
		// run the reload now and then every 10 seconds
		let reloadInterval = setInterval(() => this.reloadAuditEntries(), 10000);
		this.handleSetState('reloadInterval', reloadInterval);
	}

	cancelAutoReload() {
		clearInterval(this.state.reloadInterval);
	}

	componentWillUnmount() {
		this.cancelAutoReload();
	}

	reloadAuditEntries(){
		this.setState({
			message: 'Loading...',
			inProgress: true
		});
		var self = this;
		fetch('/api/audit',{
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
		})
		.catch(function(error) {
 		 	console.log(error);
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
					newEntry.components = jsonArray[index].notification.components;
				}
				entries.push(newEntry);
			}
			this.setState({
				entries
			});
		}
	}

	handleAutoRefreshChange(event) {
		const target = event.target;
		if (target.checked) {
			this.startAutoReload();
		} else {
			this.cancelAutoReload();
		}
		const name = target.name;
		this.handleSetState(name, target.checked);
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
		var resendUrl = '/api/audit/' + currentEntry.id + '/resend';
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
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
	}

	resendButton(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText='Re-send' />;
    }

    onStatusFailureClick(currentRowSelected){
    	this.handleSetState('currentRowSelected', currentRowSelected);
    }

    statusColumnDataFormat(cell, row) {
		var statusClass = null;
		if (cell === 'Pending') {
			statusClass = tableStyles.statusPending;
		} else if (cell === 'Success') {
			statusClass = tableStyles.statusSuccess;
		} else if (cell === 'Failure') {
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
			fontAwesomeClass = `fa fa-envelope ${fontAwesomeLabel}`;
            cellText = "Group Email";
		} else if (cell === 'hipchat_channel') {
			fontAwesomeClass = `fa fa-comments ${fontAwesomeLabel}`;
            cellText = "HipChat";
		} else if (cell === 'slack_channel') {
			fontAwesomeClass = `fa fa-slack ${fontAwesomeLabel}`;
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
			let policyViolation = null;
			let policyViolationCleared = null;
			let policyViolationOverride = null;
			let highVulnerability = null;
			let mediumVulnerability = null;
			let lowVulnerability = null;
			let vulnerability = null;

			for (var i in cell) {
				if (cell[i] === "POLICY_VIOLATION") {
					policyViolation = policyViolationIcon;
				} else if (cell[i] === "POLICY_VIOLATION_CLEARED") {
					policyViolationCleared = policyViolationClearedIcon;
				} else if (cell[i] === "POLICY_VIOLATION_OVERRIDE") {
					policyViolationOverride = policyViolationOverrideIcon;
				} else if (cell[i] === "HIGH_VULNERABILITY") {
					highVulnerability = highVulnerabilityIcon;
				} else if (cell[i] === "MEDIUM_VULNERABILITY") {
					mediumVulnerability = mediumVulnerabilityIcon;
				} else if (cell[i] === "LOW_VULNERABILITY") {
					lowVulnerability = lowVulnerabilityIcon;
				} else if (cell[i] === "VULNERABILITY") {
					vulnerability = vulnerabilityIcon;
				}
			}
			let data = <div>
						{policyViolation}
						{policyViolationCleared}
						{policyViolationOverride}
						{highVulnerability}
						{mediumVulnerability}
						{lowVulnerability}
						{vulnerability}
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


	createCustomButtonGroup(buttons) {
		let refreshButton= null;
		if (!this.state.autoRefresh) {
			let classes = `btn btn-info react-bs-table-add-btn ${tableStyles.tableButton}`;
			let fontAwesomeIcon = `fa fa-refresh ${fontAwesomeLabel}`;
			let reloadEntries = () => this.reloadAuditEntries();
			refreshButton = <div className={classes} onClick={reloadEntries} >
								<i className={fontAwesomeIcon} aria-hidden='true'></i>Refresh
						</div>;
		}
	    return (
	    	<ButtonGroup>
	      		{refreshButton}
	      	</ButtonGroup>
	    );
  	}

	render() {
		const auditTableOptions = {
			defaultSortName: 'timeLastSent',
			defaultSortOrder: 'desc',
			btnGroup: this.createCustomButtonGroup,
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
					<h1>
                        General / Audit
						<small className="pull-right">
							<label className={refreshCheckbox}><input name="autoRefresh" type="checkbox" checked={this.state.autoRefresh} onChange={this.handleAutoRefreshChange} /> Enable Auto-Refresh</label>
						</small>
					</h1>
					<div>
						<div className={auditStyles.legendContainer}>
							<div className={`${auditStyles.inline}`}>
								{highVulnerabilityIcon}
								<div className={auditStyles.legendDescription}>
									High Vulnerability
								</div>
							</div>
							<div className={`${auditStyles.inline}`}>
								{lowVulnerabilityIcon}
								<div className={auditStyles.legendDescription}>
									Low Vulnerability
								</div>
							</div>
							<div className={`${auditStyles.inline}`}>
								{policyViolationIcon}
								<div className={auditStyles.legendDescription}>
									Policy Violation
								</div>
							</div>
							<div className={`${auditStyles.inline}`}>
								{policyViolationClearedIcon}
								<div className={auditStyles.legendDescription}>
									Policy Violation Cleared
								</div>
							</div>
							<br />
							<div className={`${auditStyles.inline}`}>
								{mediumVulnerabilityIcon}
								<div className={auditStyles.legendDescription}>
									Medium Vulnerability
								</div>
							</div>
							<div className={`${auditStyles.inline}`}>
								{vulnerabilityIcon}
								<div className={auditStyles.legendDescription}>
									Vulnerability
								</div>
							</div>
							<div className={`${auditStyles.inline}`}>
								{policyViolationOverrideIcon}
								<div className={auditStyles.legendDescription}>
									Policy Override
								</div>
							</div>
						</div>
						<BootstrapTable trClassName={this.trClassFormat} condensed data={this.state.entries} expandableRow={this.isExpandableRow} expandComponent={this.expandComponent} containerClass={tableStyles.table} search={true} options={auditTableOptions} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='id' isKey hidden>Audit Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='jobName' dataSort columnTitle columnClassName={tableStyles.tableCell}>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationProjectName' dataSort columnTitle columnClassName={tableStyles.tableCell}>Project Name</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationTypes' width='145' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.notificationTypeDataFormat }>Notification Types</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeCreated' width='160' dataSort columnTitle columnClassName={tableStyles.tableCell}>Time Created</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeLastSent' width='160' dataSort columnTitle columnClassName={tableStyles.tableCell}>Time Last Sent</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' width='75' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.statusColumnDataFormat }>Status</TableHeaderColumn>
	                        <TableHeaderColumn dataField='' width='85' expandable={ false } columnClassName={tableStyles.tableCell} dataFormat={ this.resendButton }></TableHeaderColumn>
	  					</BootstrapTable>
	  					{progressIndicator}
	  					<p name="message">{this.state.message}</p>
  					</div>
				</div>
		)
	}

};


const mapStateToProps = state => ({
	items: state.audit.items
});

const mapDispatchToProps = dispatch => ({
    getAuditData: () => dispatch(getAuditData())
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
