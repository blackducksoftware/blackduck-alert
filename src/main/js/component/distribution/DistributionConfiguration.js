import React, { Component } from 'react';

import { progressIcon, fontAwesomeLabel } from '../../../css/main.css';

import styles from '../../../css/distributionConfig.css';

import tableStyles from '../../../css/table.css';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';
import EditTableCellFormatter from '../EditTableCellFormatter';

import CheckboxInput from '../../field/input/CheckboxInput';

import JobAddModal from './JobAddModal';

import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton, ButtonGroup} from 'react-bootstrap-table';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

class DistributionConfiguration extends Component {
	constructor(props) {
		super(props);
		 this.state = {
		 	autoRefresh: true,
			configurationMessage: '',
			errors: {},
			jobs: [],
			projects: [],
			groups: [],
			waitingForProjects: true,
			waitingForGroups: true
		};
		this.handleAutoRefreshChange = this.handleAutoRefreshChange.bind(this);
		this.createCustomModal = this.createCustomModal.bind(this);
		this.createCustomButtonGroup = this.createCustomButtonGroup.bind(this);
		this.cancelRowSelect = this.cancelRowSelect.bind(this);
		this.editButtonClicked = this.editButtonClicked.bind(this);
        this.editButtonClick = this.editButtonClick.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.customJobConfigDeletionConfirm = this.customJobConfigDeletionConfirm.bind(this);
	}

    componentDidMount() {
		this.retrieveProjects();
    	this.retrieveGroups();
		this.reloadPage();
		this.startAutoReload();
	}

	componentWillUnmount() {
		 this.cancelAutoReload();
	}

	startAutoReload() {
		// run the reload now and then every 10 seconds
		let reloadInterval = setInterval(() => this.reloadPage(), 10000);
		this.handleSetState('reloadInterval', reloadInterval);
	}

	cancelAutoReload() {
		clearInterval(this.state.reloadInterval);
	}

    reloadPage() {
    	this.setState({
			jobConfigTableMessage: 'Loading...',
			inProgress: true
		});
    	this.fetchDistributionJobs();
    }

	retrieveProjects() {
		var self = this;
		fetch('/hub/projects',{
			credentials: "same-origin"
		})
		.then(function(response) {
			self.handleSetState('waitingForProjects', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('projectTableMessage', json.message);
				});
			} else {
				return response.json().then(json => {
					self.handleSetState('projectTableMessage', '');
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
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
	}

	retrieveGroups() {
		var self = this;
		fetch('/hub/groups',{
			credentials: "same-origin",
		})
		.then(function(response) {
			self.handleSetState('waitingForGroups', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('groupError', json.message);
				});
			} else {
				return response.json().then(json => {
					self.handleSetState('groupError', '');
					var jsonArray = JSON.parse(json.message);
					if (jsonArray != null && jsonArray.length > 0) {
						var groups = [];
						for (var index in jsonArray) {
							groups.push({
								name: jsonArray[index].name,
								active: jsonArray[index].active,
								url: jsonArray[index].url
							});
						}
						self.setState({
							groups
						});
					}
				});
			}
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
    }

    fetchDistributionJobs() {
        let self = this;
        fetch('/configuration/distribution/common',{
			credentials: "same-origin",
            headers: {
				'Content-Type': 'application/json'
			}
		})
		.then(function(response) {
			self.handleSetState('inProgress', false);
			if (response.ok) {
				self.handleSetState('jobConfigTableMessage', '');
                response.json().then(jsonArray => {
                    let newJobs = new Array();
					if (jsonArray != null && jsonArray.length > 0) {
                        jsonArray.forEach((item) =>{
                            let jobConfig = {
                            	id: item.id,
                                distributionConfigId: item.distributionConfigId,
                    			name: item.name,
                    			distributionType: item.distributionType,
                    			lastRan: item.lastRan,
                    			status: item.status,
                                frequency: item.frequency,
                                notificationTypes: item.notificationTypes,
                                configuredProjects: item.configuredProjects
                            };

                            newJobs.push(jobConfig);
                        });
                    }
                   self.setState({
						jobs: newJobs
					});
                });
            } else {
            	return response.json().then(json => {
					self.handleSetState('jobConfigTableMessage', json.message);
				});
            }
        })
        .catch(function(error) {
 		 	console.log(error);
 		});
    }

    statusColumnClassNameFormat(fieldValue, row, rowIdx, colIdx) {
		var className = null;
		if (fieldValue === 'Pending') {
			className = tableStyles.statusPending;
		} else if (fieldValue === 'Success') {
			className = tableStyles.statusSuccess;
		} else if (fieldValue === 'Failure') {
			className = tableStyles.statusFailure;
		}
		className = `${className} ${tableStyles.tableCell}`
		return className;
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

		let data = <div title={cellText}>
						<i key="icon" className={fontAwesomeClass} aria-hidden='true'></i>
						{cellText}
					</div>;

		return data;
	}

    createCustomModal(onModalClose, onSave, columns, validateState, ignoreEditable) {
        return (
	    	<JobAddModal
	    		waitingForProjects={this.state.waitingForProjects}
	    		waitingForGroups={this.state.waitingForGroups}
	    		projects={this.state.projects}
	    		includeAllProjects={true}
	    		groups={this.state.groups}
	    		groupError={this.state.groupError}
	    		projectTableMessage={this.state.projectTableMessage}
	    		handleCancel={this.cancelRowSelect}
                onModalClose= { () => {
                	this.fetchDistributionJobs();
                	onModalClose();
                }}
		    	onSave= { onSave }
		    	columns={ columns }
		        validateState={ validateState }
		        ignoreEditable={ ignoreEditable } />
	    );
	}

	customJobConfigDeletionConfirm(next, dropRowKeys) {
	  if (confirm("Are you sure you want to delete these Job configurations?")) {
	  	console.log('Deleting the Job configs');
	  	//TODO delete the Job configs from the backend
	  	// dropRowKeys are the Id's of the Job configs
		let self = this;
		var jobs = self.state.jobs;

		var matchingJobs = jobs.filter(job => {
			return dropRowKeys.includes(job.id);
		});
	  	matchingJobs.forEach(function(job){
	  		let jsonBody = JSON.stringify(job);
		    fetch('/configuration/distribution/common',{
		    	method: 'DELETE',
				credentials: "same-origin",
	            headers: {
					'Content-Type': 'application/json'
				},
				body: jsonBody
			}).then(function(response) {
				if (!response.ok) {
					return response.json().then(json => {
					let jsonErrors = json.errors;
					if (jsonErrors) {
						var errors = {};
						for (var key in jsonErrors) {
							if (jsonErrors.hasOwnProperty(key)) {
								let name = key.concat('Error');
								let value = jsonErrors[key];
								errors[name] = value;
							}
						}
						self.setState({
							errors
						});
					}
					self.setState({
						jobConfigTableMessage: json.message
					});
				});
				}
			})
			.catch(function(error) {
 		 		console.log(error);
 			});
		});
	  	next();
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

	getCurrentJobConfig(currentRowSelected) {
		let currentJobConfig = null;
		if (currentRowSelected != null) {
            const { id, name, distributionConfigId, distributionType, frequency, notificationTypes, groupName, includeAllProjects, configuredProjects } = currentRowSelected;
			if (distributionType === 'email_group_channel') {
				currentJobConfig = <GroupEmailJobConfiguration buttonsFixed={true} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForGroups={this.state.waitingForGroups} groups={this.state.groups} groupName={groupName} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} />;
			} else if (distributionType === 'hipchat_channel') {
				currentJobConfig = <HipChatJobConfiguration buttonsFixed={true} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} />;
			} else if (distributionType === 'slack_channel') {
				currentJobConfig = <SlackJobConfiguration buttonsFixed={true} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} />;
			}
		}
		return currentJobConfig;
	}

	editButtonClicked(currentRowSelected) {
		this.handleSetState('currentRowSelected', currentRowSelected);
	}

    editButtonClick(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.editButtonClicked} currentRowSelected= {row} />;
    }


	createCustomButtonGroup(buttons) {
		let classes = `btn btn-info react-bs-table-add-btn ${tableStyles.tableButton}`;
		let fontAwesomeIcon = `fa fa-refresh ${fontAwesomeLabel}`;
		let insertOnClick = buttons.insertBtn.props.onClick;
		let deleteOnClick = buttons.deleteBtn.props.onClick;
		let reloadEntries = () => this.reloadPage();
		let refreshButton= null;
		if (!this.state.autoRefresh) {
			refreshButton =  <div className={classes} onClick={reloadEntries} >
					 			<i className={fontAwesomeIcon} aria-hidden='true'></i>Refresh
							</div>;
		}
	    return (
	      <ButtonGroup>
	      	<InsertButton className={tableStyles.addJobButton} onClick={insertOnClick}/>
	      	<DeleteButton className={tableStyles.deleteJobButton} onClick={deleteOnClick}/>
	      	{refreshButton}
	      </ButtonGroup>
	    );
  	}


	render() {
		const jobTableOptions = {
			btnGroup: this.createCustomButtonGroup,
	  		noDataText: 'No jobs configured',
	  		clearSearch: true,
	  		insertModal: this.createCustomModal,
	  		handleConfirmDeleteRow: this.customJobConfigDeletionConfirm
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
		var progressIndicator = null;
        if (this.state.inProgress) {
            const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
            progressIndicator = <div className={progressIcon}>
                                    <i className={fontAwesomeIcon} aria-hidden='true'></i>
                                </div>;
        }
		var content = <div>
						<CheckboxInput label="Enable auto refresh" name="autoRefresh" value={this.state.autoRefresh} onChange={this.handleAutoRefreshChange} errorName="autoRefreshError" errorValue={this.state.autoRefreshError}></CheckboxInput>
						<BootstrapTable striped condensed data={this.state.jobs} containerClass={tableStyles.table} insertRow={true} deleteRow={true} selectRow={jobsSelectRowProp} search={true} options={jobTableOptions} trClassName={tableStyles.tableRow} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='id' isKey hidden>Job Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='distributionConfigId' hidden>Distribution Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='name' dataSort columnTitle columnClassName={tableStyles.tableCell} >Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='distributionType' dataSort columnClassName={tableStyles.tableCell} dataFormat={ this.typeColumnDataFormat }>Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='lastRan' dataSort columnTitle columnClassName={tableStyles.tableCell}>Last Run</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnTitle columnClassName={ this.statusColumnClassNameFormat }>Status</TableHeaderColumn>
                            <TableHeaderColumn dataField='' width='65' columnClassName={tableStyles.tableCell} dataFormat={ this.editButtonClick }></TableHeaderColumn>
	  					</BootstrapTable>
	  					{progressIndicator}
	  					<p name="jobConfigTableMessage">{this.state.jobConfigTableMessage}</p>
  					</div>;
		var currentJobContent = this.getCurrentJobConfig (this.state.currentRowSelected);
		if (currentJobContent != null) {
			content = currentJobContent;
		}
		return (
				<div>
					{content}
				</div>
		)
	}
};

export default DistributionConfiguration;
