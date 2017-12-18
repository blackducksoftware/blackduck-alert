import React, { Component } from 'react';

import tableStyles from '../../../css/table.css';

import EditTableCellFormatter from '../EditTableCellFormatter';

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
	}

	addDefaultEntries() {
        const { entries } = this.state;
        entries.push({
            id: '999',
            jobName: 'Test Job',
            eventType: 'email_group_channel',
            notificationType: 'High Vulnerability',
            status: 'Success'
        });
        entries.push({
            id: '999',
            jobName: 'Test Hipchat',
            eventType: 'hipchat_channel',
            notificationType: 'High Vulnerability',
            status: 'Failure'
        });
        this.setState({
			entries
		});
    }

	componentDidMount() {
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

	}

	resendButton(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText='Re-send' />;
    }

    onStatusFailureClick(currentRowSelected){
    	this.handleSetState('currentRowSelected', currentRowSelected);
    }

    statusColumnDataFormat(cell, row) {
		var content = <div className={tableStyles.statusSuccess} aria-hidden='true'>
							{cell}
						</div>;
		if (cell === 'Failure') {
			content = <EditTableCellFormatter buttonClass="btn btn-danger" handleButtonClicked={this.onStatusFailureClick} currentRowSelected={row} buttonText={cell} />;
		}
		let data = <div>
						{content}
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

	getCurrentEntryDetails(currentRowSelected) {
		let currentEntryDetails = null;
		
		return currentEntryDetails;
	}

	render() {
		const auditTableOptions = {
	  		noDataText: 'No events',
	  		clearSearch: true
		};
		var content = <div>
						<BootstrapTable data={this.state.entries} containerClass={tableStyles.table} striped hover condensed search={true} options={auditTableOptions} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='id' isKey hidden>Audit Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='jobName' columnClassName={tableStyles.tableCell}>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='eventType' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.typeColumnDataFormat }>Event Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='notificationType' dataSort columnClassName={tableStyles.tableCell}>Notification Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.statusColumnDataFormat }>Status</TableHeaderColumn>
	                        <TableHeaderColumn dataField='' columnClassName={tableStyles.tableCell} dataFormat={ this.resendButton }></TableHeaderColumn>
	  					</BootstrapTable>
	  					<p name="message">{this.state.message}</p>
  					</div>;
  		var currentEntryDetails = this.getCurrentEntryDetails(this.state.currentRowSelected);
  		if (currentEntryDetails) {
  			content = currentEntryDetails;
  		}
		return (
				{content}
		)
	}

};

export default Audit;
