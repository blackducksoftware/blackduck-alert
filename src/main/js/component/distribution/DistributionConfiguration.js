import React from 'react';

import styles from '../../../css/distributionConfig.css';

import ProjectConfiguration from './ProjectConfiguration';

import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton} from 'react-bootstrap-table';

	var jobs = [];

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
			jobs: [],
			projects: []
		};
		this.handleJobAddClick = this.handleJobAddClick.bind(this);
		this.handleJobDeleteClick = this.handleJobDeleteClick.bind(this);
		this.createCustomInsertButton = this.createCustomInsertButton.bind(this);
		this.createCustomDeleteButton = this.createCustomDeleteButton.bind(this);
	}

	componentDidMount() {
		var self = this;
		
		fetch('/hub/projects',{
			credentials: "same-origin"
		})  
		.then(function(response) {
			if (!response.ok) {
				return response.json().then(json => {
					self.setState({
						projectTableMessage: json.message
					});
				});
			} else {
				return response.json().then(json => {
					self.setState({
						projectTableMessage: ''
					});
					var jsonArray = JSON.parse(json.message);
					if (jsonArray != null && jsonArray.length > 0) {
						var projects = [];
						for (var index in jsonArray) {
							projects.push({
								name: jsonArray[index].name,
								url: jsonArray[index].url
							});
						}
						self.setState({
							projects
						});
					}
				});
			}
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
		const jobsSelectRowProp = {
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
		var projectsBlockClasses = '';
		if (!showJobConfiguration) {
			jobConfigBlockClasses = styles.hidden;
			projectsBlockClasses = styles.hidden;
		} else {
			jobConfigBlockClasses = styles.contentBlock;
		}
		return (
				<div>
					<div>
						<BootstrapTable data={jobs} containerClass={styles.table} striped hover condensed insertRow={true} deleteRow={true} selectRow={jobsSelectRowProp} search={true} options={jobTableOptions} trClassName={styles.tableRow} headerContainerClass={styles.scrollable} bodyContainerClass={styles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='jobName' isKey dataSort>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='type' dataSort>Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='lastRun' dataSort>Last Run</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnClassName={ columnClassNameFormat }>Status</TableHeaderColumn>
	  					</BootstrapTable>
	  					<p name="jobConfigTableMessage">{this.state.jobConfigTableMessage}</p>
  					</div>
  					<div className={jobConfigBlockClasses}>
  						<p name="jobConfigMessage">{this.state.jobConfigMessage}</p>
  					</div>
  					<div className={projectsBlockClasses}>
  						<ProjectConfiguration projects={this.state.projects} projectTableMessage={this.state.projectTableMessage} />
  					</div>
				</div>
		)
	}
}