import React, { Component } from 'react';
import { connect } from "react-redux";
import { ReactBsTable, BootstrapTable, TableHeaderColumn, ButtonGroup } from 'react-bootstrap-table';
import { getAuditData } from '../../../store/actions/audit';
import AutoRefresh from '../../common/AutoRefresh';
import RefreshTableCellFormatter from '../../common/RefreshTableCellFormatter';
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
		this.cancelAutoReload = this.cancelAutoReload.bind(this);
		this.startAutoReload = this.startAutoReload.bind(this);
		this.handleAutoRefreshChange = this.handleAutoRefreshChange.bind(this);
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
		this.startAutoReload();
	}

    componentWillReceiveProps(nextProps) {
		if(nextProps.items !== this.props.items) {
			this.setState({'message': ''});
            this.setEntriesFromArray(nextProps.items);
		}
    }

	startAutoReload() {
		// run the reload now and then every 10 seconds
		this.reloadInterval = setInterval(() => this.props.getAuditData(), 10000);
	}

	cancelAutoReload() {
		clearInterval(this.reloadInterval);
	}

    componentWillUnmount() {
		this.cancelAutoReload();
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
        this.setState({
            [name]: target.checked
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

		const resendUrl = '/api/audit/' + currentEntry.id + '/resend';
		fetch(resendUrl, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			}
		}).then((response) => {
			this.setState({ 'inProgress': false });
			if (!response.ok) {
				return response.json().then(json => {
                    this.setState({ 'message': json.message });
				});
			}  else {
				return response.json().then(json => {
                    this.setState({ 'message': '' });
					this.setEntriesFromArray(JSON.parse(json.message));
				});
			}
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
	}

	resendButton(cell, row) {
        return <RefreshTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText='Re-send' />;
    }

    onStatusFailureClick(currentRowSelected){
		this.setState({
            currentRowSelected
		});
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
		switch(cell) {
			case 'email_group_channel':
				return (
					<div>
						<span key="icon" className="fa fa-envelope fa-fw" aria-hidden='true' />
						Group Email
					</div>
				);
			case 'hipchat_channel':
                return (
                    <div>
                        <span key="icon" className="fa fa-comments fa-fw" aria-hidden='true' />
                        HipChat
                    </div>
                );
            case 'slack_channel':
                return (
                    <div>
                        <span key="icon" className="fa fa-slack fa-fw" aria-hidden='true' />
                        Slack
                    </div>
                );
			default:
				return (
					<div>Unknown</div>
				);
		}
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
                        Alert / General / Audit
						<small className="pull-right">
							<AutoRefresh autoRefresh={this.state.autoRefresh} handleAutoRefreshChange={this.handleAutoRefreshChange} />
						</small>
					</h1>
					<div>
						<div className="legendContainer">
							<div className="inline">
								{highVulnerabilityIcon}
								<div className="legendDescription">
									High Vulnerability
								</div>
							</div>
							<div className="inline">
								{lowVulnerabilityIcon}
								<div className="legendDescription">
									Low Vulnerability
								</div>
							</div>
							<div className="inline">
								{policyViolationIcon}
								<div className="legendDescription">
									Policy Violation
								</div>
							</div>
							<div className="inline">
								{policyViolationClearedIcon}
								<div className="legendDescription">
									Policy Violation Cleared
								</div>
							</div>
							<br />
							<div className="inline">
								{mediumVulnerabilityIcon}
								<div className="legendDescription">
									Medium Vulnerability
								</div>
							</div>
							<div className="inline">
								{vulnerabilityIcon}
								<div className="legendDescription">
									Vulnerability
								</div>
							</div>
							<div className="inline">
								{policyViolationOverrideIcon}
								<div className="legendDescription">
									Policy Override
								</div>
							</div>
						</div>
						<BootstrapTable trClassName={this.trClassFormat} condensed data={this.state.entries} expandableRow={this.isExpandableRow} expandComponent={this.expandComponent} containerClass="table" search={true} options={auditTableOptions} headerContainerClass="scrollable" bodyContainerClass="tableScrollableBody">
	      					<TableHeaderColumn dataField='jobName' dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationProjectName' dataSort columnTitle columnClassName="tableCell">Project Name</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationTypes' width='145' dataSort columnClassName="tableCell" dataFormat={ this.notificationTypeDataFormat }>Notification Types</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeCreated' width='160' dataSort columnTitle columnClassName="tableCell">Time Created</TableHeaderColumn>
	      					<TableHeaderColumn dataField='timeLastSent' width='160' dataSort columnTitle columnClassName="tableCell">Time Last Sent</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' width='75' dataSort columnClassName="tableCell" dataFormat={ this.statusColumnDataFormat }>Status</TableHeaderColumn>
	                        <TableHeaderColumn dataField='' width='48' expandable={ false } columnClassName="tableCell" dataFormat={ this.resendButton }></TableHeaderColumn>
                            <TableHeaderColumn dataField='id' isKey hidden>Audit Id</TableHeaderColumn>
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
