import React from 'react';

import { tableRow, statusSuccess, statusFailure } from '../../css/distributionConfig.css';

import {ReactBsTable, BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

	const jobs = [];

	function addJobs() {
		jobs.push({
			jobName: 'Test Job',
			type: 'Group Email',
			lastRun: '12/01/2017 00:00:00',
			status: 'Success'
		});
		jobs.push({
			jobName: 'Alert Slack Job',
			type: 'Slack',
			lastRun: '12/02/2017 00:00:00',
			status: 'Failure'
		});
		jobs.push({
			jobName: 'HipChat Job',
			type: 'Slack',
			lastRun: '1/01/2017 00:00:00',
			status: 'Success'
		});
	}
	

	addJobs();

	function columnClassNameFormat(fieldValue, row, rowIdx, colIdx) {
		var className = statusSuccess;
		if (fieldValue === 'Failure') {
			className = statusFailure;
		}
		return className;
	}

export default class DistributionConfiguration extends React.Component {
	constructor(props) {
		super(props);
		 this.state = {
			configurationMessage: '',
			errors: {},
			jobs: {}
		};
		this.handleJobAdd = this.handleJobAdd.bind(this);
		this.handleChange = this.handleChange.bind(this);
	}


	handleJobAdd(event) {
		
	}

	handleChange(event) {
		const target = event.target;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		const name = target.name;

		var values = this.state.values;
		values[name] = value;
		this.setState({
			values
		});
	}

	render() {
		const tableOptions = {
	  		noDataText: 'This is custom text for empty data',
	  		clearSearch: true
		};

		const selectRowProp = {
	  		mode: 'checkbox',
	  		clickToSelect: true,
			bgColor: function(row, isSelect) {
				if (isSelect) {
					return '#e8e8e8';
				}
				return null;
			}
		};
		return (
				<div>
					<input className='thing' type='submit' value='+ Add' onClick={this.handleJobAdd}></input>
					<BootstrapTable data={jobs} striped hover selectRow={ selectRowProp } search={ true } options={tableOptions} trClassName={ tableRow } >
      					<TableHeaderColumn dataField='jobName' isKey dataSort>Distribution Job</TableHeaderColumn>
      					<TableHeaderColumn dataField='type' dataSort>Type</TableHeaderColumn>
      					<TableHeaderColumn dataField='lastRun' dataSort>Last Run</TableHeaderColumn>
      					<TableHeaderColumn dataField='status' dataSort columnClassName={ columnClassNameFormat }>Status</TableHeaderColumn>
  					</BootstrapTable>
				</div>
		)
	}
}