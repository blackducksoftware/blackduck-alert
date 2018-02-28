import React, { Component } from 'react';
import { connect } from "react-redux";
import { ReactBsTable, BootstrapTable, TableHeaderColumn, ButtonGroup } from 'react-bootstrap-table';
import { getAuditData } from '../../../store/actions/audit';
import AutoRefresh from '../../common/AutoRefresh';
import EditTableCellFormatter from '../../common/EditTableCellFormatter';
import AuditDetails from './Details';

import '../../../../css/audit.scss';

var policyViolationIcon = <i key="policyViolationIcon" alt="Policy Violation" title="Policy Violation" className={`fa fa-ban policyViolation`} aria-hidden='true'></i>;
var policyViolationClearedIcon = <i key="policyViolationClearedIcon" alt="Policy Violation Cleared" title="Policy Violation Cleared" className={`fa fa-eraser policyViolationCleared`} aria-hidden='true'></i>;
var policyViolationOverrideIcon = <i key="policyViolationOverrideIcon" alt="Policy Override" title="Policy Override" className={`fa fa-exclamation-circle policyViolationOverride`} aria-hidden='true'></i>;
var highVulnerabilityIcon = <i key="highVulnerabilityIcon" alt="High Vulnerability" title="High Vulnerability" className={`fa fa-shield highVulnerability`} aria-hidden='true'></i>;
var mediumVulnerabilityIcon = <i key="mediumVulnerabilityIcon" alt="Medium Vulnerability" title="Medium Vulnerability" className={`fa fa-shield mediumVulnerability`} aria-hidden='true'></i>;
var lowVulnerabilityIcon = <i key="lowVulnerabilityIcon" alt="Low Vulnerability" title="Low Vulnerability" className={`fa fa-shield lowVulnerability`} aria-hidden='true'></i>;
var vulnerabilityIcon = <i key="vulnerabilityIcon" alt="Vulnerability" title="Vulnerability" className={`fa fa-shield vulnerability`} aria-hidden='true'></i>;


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
			credentials: 'include'
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
			credentials: 'include',
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
			statusClass = "statusPending";
		} else if (cell === 'Success') {
			statusClass = "statusSuccess";
		} else if (cell === 'Failure') {
			statusClass = "statusFailure";
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
			fontAwesomeClass = `fa fa-envelope fa-fw`;
            cellText = "Group Email";
		} else if (cell === 'hipchat_channel') {
			fontAwesomeClass = `fa fa-comments fa-fw`;
            cellText = "HipChat";
		} else if (cell === 'slack_channel') {
			fontAwesomeClass = `fa fa-slack fa-fw`;
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
		return rowIndex % 2 === 0 ? "tableEvenRow" : "tableRow";
	}


	createCustomButtonGroup(buttons) {
	    return (
	    	<ButtonGroup>
	      		{!this.state.autoRefresh && <div className="btn btn-info react-bs-table-add-btn tableButton" onClick={this.reloadAuditEntries}>
                    <span className="fa fa-refresh fa-fw" aria-hidden='true'></span> Refresh
                </div>}
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

		return (
				<div>
					<h1>
                        General / Audit
						<small className="pull-right">
							<AutoRefresh autoRefresh={this.state.autoRefresh} handleAutoRefreshChange={this.handleAutoRefreshChange} />
						</small>
					</h1>
					<div>
						<div className="legendContainer">
							<div className={`inline`}>
								{highVulnerabilityIcon}
								<div className="legendDescription">
									High Vulnerability
								</div>
							</div>
							<div className={`inline`}>
								{lowVulnerabilityIcon}
								<div className="legendDescription">
									Low Vulnerability
								</div>
							</div>
							<div className={`inline`}>
								{policyViolationIcon}
								<div className="legendDescription">
									Policy Violation
								</div>
							</div>
							<div className={`inline`}>
								{policyViolationClearedIcon}
								<div className="legendDescription">
									Policy Violation Cleared
								</div>
							</div>
							<br />
							<div className={`inline`}>
								{mediumVulnerabilityIcon}
								<div className="legendDescription">
									Medium Vulnerability
								</div>
							</div>
							<div className={`inline`}>
								{vulnerabilityIcon}
								<div className="legendDescription">
									Vulnerability
								</div>
							</div>
							<div className={`inline`}>
								{policyViolationOverrideIcon}
								<div className="legendDescription">
									Policy Override
								</div>
							</div>
						</div>
						<BootstrapTable trClassName={this.trClassFormat} condensed data={this.state.entries} expandableRow={this.isExpandableRow} expandComponent={this.expandComponent} containerClass="table" search={true} options={auditTableOptions} headerContainerClass="scrollable" bodyContainerClass="tableScrollableBody">
	      					<TableHeaderColumn dataField='id' isKey hidden>Audit Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='jobName' dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationProjectName' dataSort columnTitle columnClassName="tableCell">Project Name</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationTypes' width='145' dataSort columnClassName="tableCell" dataFormat={ this.notificationTypeDataFormat }>Notification Types</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeCreated' width='160' dataSort columnTitle columnClassName="tableCell">Time Created</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeLastSent' width='160' dataSort columnTitle columnClassName="tableCell">Time Last Sent</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' width='75' dataSort columnClassName="tableCell" dataFormat={ this.statusColumnDataFormat }>Status</TableHeaderColumn>
	                        <TableHeaderColumn dataField='' width='85' expandable={ false } columnClassName="tableCell" dataFormat={ this.resendButton }></TableHeaderColumn>
	  					</BootstrapTable>

						{ this.state.inProgress && <div className="progressIcon">
                            <span className="fa fa-spinner fa-pulse fa-fw" aria-hidden='true'></span>
                        </div>}

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
