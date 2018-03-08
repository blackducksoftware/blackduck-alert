import React, { Component } from 'react';
import { connect } from 'react-redux';
import { ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton, ButtonGroup } from 'react-bootstrap-table';

import AutoRefresh from '../../common/AutoRefresh';
import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';
import EditTableCellFormatter from '../../common/EditTableCellFormatter';

import JobAddModal from './JobAddModal';

class Index extends Component {
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
		 this.startAutoReload = this.startAutoReload.bind(this);
		 this.cancelAutoReload = this.cancelAutoReload.bind(this);
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
		this.reloadInterval = setInterval(() => this.reloadPage(), 10000);
	}

	cancelAutoReload() {
		clearInterval(this.reloadInterval);
	}

    reloadPage() {
		this.cancelAutoReload();
    	this.setState({
			jobConfigTableMessage: 'Loading...',
			inProgress: true
		});
    	this.fetchDistributionJobs();
    }

	retrieveProjects() {
		var self = this;
		const csrfToken = this.props.csrfToken;
		fetch('/api/hub/projects',{
			credentials: "same-origin",
			headers: {
				'X-CSRF-TOKEN': csrfToken
			}
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
		const csrfToken = this.props.csrfToken;
		fetch('/api/hub/groups',{
			credentials: "same-origin",
			headers: {
				'X-CSRF-TOKEN': csrfToken
			}
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
				const csrfToken = this.props.csrfToken;
        fetch('/api/configuration/distribution/common',{
						credentials: "same-origin",
            headers: {
							'Content-Type': 'application/json',
							'X-CSRF-TOKEN': csrfToken
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
			className = "statusPending";
		} else if (fieldValue === 'Success') {
			className = "statusSuccess";
		} else if (fieldValue === 'Failure') {
			className = "statusFailure";
		}
		className = `${className} tableCell`
		return className;
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

		let data = <div title={cellText}>
						<i key="icon" className={fontAwesomeClass} aria-hidden='true'></i>
						{cellText}
					</div>;

		return data;
	}

    createCustomModal(onModalClose, onSave, columns, validateState, ignoreEditable) {
        return (
	    	<JobAddModal
				  csrfToken={this.props.csrfToken}
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
				const csrfToken = this.props.csrfToken
		    fetch('/api/configuration/distribution/common',{
		    	method: 'DELETE',
					credentials: "same-origin",
          headers: {
						'Content-Type': 'application/json',
						'X-CSRF-TOKEN': csrfToken
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
		const csrfToken = this.props.csrfToken;
		if (currentRowSelected != null) {
            const { id, name, distributionConfigId, distributionType, frequency, notificationTypes, groupName, includeAllProjects, configuredProjects } = currentRowSelected;
			if (distributionType === 'email_group_channel') {
				currentJobConfig = <GroupEmailJobConfiguration csrfToken={csrfToken} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForGroups={this.state.waitingForGroups} groups={this.state.groups} groupName={groupName} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} />;
			} else if (distributionType === 'hipchat_channel') {
				currentJobConfig = <HipChatJobConfiguration csrfToken={csrfToken} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} />;
			} else if (distributionType === 'slack_channel') {
				currentJobConfig = <SlackJobConfiguration csrfToken={csrfToken} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} />;
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
		const classes = `btn btn-info react-bs-table-add-btn tableButton`;
        const fontAwesomeIcon = `fa fa-refresh fa-fw`;
        const insertOnClick = buttons.insertBtn.props.onClick;
        const deleteOnClick = buttons.deleteBtn.props.onClick;
        const reloadEntries = () => this.reloadPage();
		let refreshButton = null;
		if (!this.state.autoRefresh) {
			refreshButton =  <div className={classes} onClick={reloadEntries} >
					 			<i className={fontAwesomeIcon} aria-hidden='true'></i>Refresh
							</div>;
		}
	    return (
	      <div>
	      	<InsertButton className="addJobButton" onClick={insertOnClick}/>
	      	<DeleteButton className="deleteJobButton" onClick={deleteOnClick}/>
	      	{refreshButton}
	      </div>
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

		let content = <div>
			<BootstrapTable hover condensed data={this.state.jobs} containerClass="table" insertRow={true} deleteRow={true} selectRow={jobsSelectRowProp} search={true} options={jobTableOptions} trClassName="tableRow" headerContainerClass="scrollable" bodyContainerClass="tableScrollableBody">
	      		<TableHeaderColumn dataField='id' isKey hidden>Job Id</TableHeaderColumn>
				<TableHeaderColumn dataField='distributionConfigId' hidden>Distribution Id</TableHeaderColumn>
				<TableHeaderColumn dataField='name' dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
				<TableHeaderColumn dataField='distributionType' dataSort columnClassName="tableCell" dataFormat={ this.typeColumnDataFormat }>Type</TableHeaderColumn>
				<TableHeaderColumn dataField='lastRan' dataSort columnTitle columnClassName="tableCell">Last Run</TableHeaderColumn>
				<TableHeaderColumn dataField='status' dataSort columnTitle columnClassName={ this.statusColumnClassNameFormat }>Status</TableHeaderColumn>
				<TableHeaderColumn dataField='' width='48' columnClassName="tableCell" dataFormat={ this.editButtonClick }></TableHeaderColumn>
			</BootstrapTable>

			{ this.state.inProgress && <div className="progressIcon">
                <span className="fa fa-spinner fa-pulse" aria-hidden='true'></span>
            </div>}

			<p name="jobConfigTableMessage">{this.state.jobConfigTableMessage}</p>
		</div>;

		const currentJobContent = this.getCurrentJobConfig (this.state.currentRowSelected);
		if (currentJobContent !== null) {
			content = currentJobContent;
		}
		return (
				<div>
                    <h1>
                        Alert / General / Distribution
                        <small className="pull-right">
                            <AutoRefresh
								autoRefresh={this.state.autoRefresh}
								handleAutoRefreshChange={this.handleAutoRefreshChange}
							/>
                        </small>
                    </h1>
					{content}
				</div>
		)
	}
};

const mapStateToProps = state => ({
	csrfToken: state.session.csrfToken
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
