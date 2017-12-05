import React from 'react';

import styles from '../../../css/distributionConfig.css';

import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton} from 'react-bootstrap-table';

	var jobs = [];

	var projects = [];

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
			type: 'HipChat',
			lastRun: '1/01/2017 00:00:00',
			status: 'Success'
		});
	}
	

	addJobs();

	function columnClassNameFormat(fieldValue, row, rowIdx, colIdx) {
		var className = styles.statusSuccess;
		if (fieldValue === 'Failure') {
			className = styles.statusFailure;
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
		this.handleChange = this.handleChange.bind(this);
		this.handleJobAddClick = this.handleJobAddClick.bind(this);
		this.handleJobDeleteClick = this.handleJobDeleteClick.bind(this);
		this.createCustomInsertButton = this.createCustomInsertButton.bind(this);
		this.createCustomDeleteButton = this.createCustomDeleteButton.bind(this);
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

	handleJobAddClick(onClick) {
		console.log('This is my custom function for InsertButton click event');
	}

	handleJobDeleteClick(onClick) {
		console.log('This is my custom function for DeleteButton click event');
	}

	createCustomInsertButton(onClick) {
		return (
			<InsertButton
			className={styles.addJobButton}
			onClick={ () => this.handleJobAddClick(onClick) }/>
		);
	}

	createCustomDeleteButton(onClick) {
		return (
			<DeleteButton
			className={styles.deleteJobButton}
			onClick={ () => this.handleJobDeleteClick(onClick) }/>
		);
	}
	
	render() {
		const jobTableOptions = {
	  		noDataText: 'No jobs configured',
	  		clearSearch: true,
	  		insertBtn: this.createCustomInsertButton,
	  		deleteBtn: this.createCustomDeleteButton
		};

		const projectTableOptions = {
	  		noDataText: 'No projects found',
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
		const showJobConfiguration = true;
		var jobConfigBlockClasses = '';
		if (showJobConfiguration) {
			jobConfigBlockClasses = styles.contentBlock;
		} else {
			jobConfigBlockClasses = styles.hidden;
		}
		return (
				<div>
					<div className={styles.contentBlock}>
						<BootstrapTable data={jobs} containerClass={styles.table} striped hover insertRow={true} deleteRow={true} selectRow={selectRowProp} search={true} options={jobTableOptions} trClassName={styles.tableRow} >
	      					<TableHeaderColumn dataField='jobName' isKey dataSort>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='type' dataSort>Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='lastRun' dataSort>Last Run</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnClassName={ columnClassNameFormat }>Status</TableHeaderColumn>
	  					</BootstrapTable>
  					</div>
  					<div className={jobConfigBlockClasses}>
  					</div>
  					<div className={jobConfigBlockClasses}>
  						<BootstrapTable data={projects} containerClass={styles.table} striped hover selectRow={selectRowProp} search={true} options={projectTableOptions} trClassName={styles.tableRow} >
	      					<TableHeaderColumn dataField='projectName' isKey dataSort>Project</TableHeaderColumn>
	  					</BootstrapTable>
  					</div>
				</div>
		)
	}
}