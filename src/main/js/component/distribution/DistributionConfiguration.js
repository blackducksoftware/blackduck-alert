import React from 'react';

import styles from '../../../css/distributionConfig.css';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';


import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton} from 'react-bootstrap-table';

	var jobs = [];

	function addJobs() {
		jobs.push({
			jobId: '0',
			jobName: 'Test Job',
			type: 'Group Email',
			lastRun: '12/01/2017 00:00:00',
			status: 'Success'
		});
		jobs.push({
			jobId: '1',
			jobName: 'Alert Slack Job',
			type: 'Slack',
			lastRun: '12/02/2017 00:00:00',
			status: 'Failure'
		});
		jobs.push({
			jobId: '2',
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
		this.handleJobSelect = this.handleJobSelect.bind(this);
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

	handleJobSelect(row, isSelected, event) {
		// If the return value of this function is false, the select or deselect action will not be applied. 
		if (isSelected) {
			this.setState({
				currentJobSelected: row
			});
		} else {
			this.setState({
				currentJobSelected: null
			});
		}

		return true;
	}

	getCurrentJobConfig(currentJobSelected){
		let currentJobConfig = null;
		if (currentJobSelected != null) {
			if (currentJobSelected.type === 'Group Email') {
				currentJobConfig = <GroupEmailJobConfiguration projects={this.state.projects} projectTableMessage={this.state.projectTableMessage} />;
			} else if (currentJobSelected.type === 'HipChat') {
				currentJobConfig = <HipChatJobConfiguration projects={this.state.projects} projectTableMessage={this.state.projectTableMessage} />;
			} else if (currentJobSelected.type === 'Slack') {
				currentJobConfig = <SlackJobConfiguration projects={this.state.projects} projectTableMessage={this.state.projectTableMessage} />;
			}
		}
		return currentJobConfig;
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
	  		onSelect: this.handleJobSelect,
			bgColor: function(row, isSelect) {
				if (isSelect) {
					return '#e8e8e8';
				}
				return null;
			}
		};
		var currentJobContent = this.getCurrentJobConfig (this.state.currentJobSelected);
		return (
				<div>
					<div>
						<BootstrapTable data={jobs} containerClass={styles.table} striped hover condensed insertRow={true} deleteRow={true} selectRow={jobsSelectRowProp} search={true} options={jobTableOptions} trClassName={styles.tableRow} headerContainerClass={styles.scrollable} bodyContainerClass={styles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='jobId' isKey hidden>Job Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='jobName' dataSort>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='type' dataSort>Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='lastRun' dataSort>Last Run</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnClassName={ columnClassNameFormat }>Status</TableHeaderColumn>
	  					</BootstrapTable>
	  					<p name="jobConfigTableMessage">{this.state.jobConfigTableMessage}</p>
  					</div>
  					{currentJobContent}
				</div>
		)
	}
}