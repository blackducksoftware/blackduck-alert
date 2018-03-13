import React, { Component } from 'react';
import {
    ReactBsTable,
    BootstrapTable,
    TableHeaderColumn,
    InsertButton,
    DeleteButton,
    ButtonGroup
} from 'react-bootstrap-table';

import AutoRefresh from '../../common/AutoRefresh';
import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';
import EditTableCellFormatter from '../../common/EditTableCellFormatter';

import JobAddModal from './JobAddModal';


/**
 * Selects className based on field value
 * @param fieldValue
 * @returns {string}
 */
function statusColumnClassNameFormat(fieldValue) {
    let className = '';
    if (fieldValue === 'Pending') {
        className = 'statusPending';
    } else if (fieldValue === 'Success') {
        className = 'statusSuccess';
    } else if (fieldValue === 'Failure') {
        className = 'statusFailure';
    }
    return `${className} tableCell`;
}

/**
 * Return type column data
 * @param cell
 * @returns {*}
 */
function typeColumnDataFormat(cell) {
    let fontAwesomeClass = '';
    let cellText = '';
    if (cell === 'email_group_channel') {
        fontAwesomeClass = 'fa fa-envelope fa-fw';
        cellText = 'Group Email';
    } else if (cell === 'hipchat_channel') {
        fontAwesomeClass = 'fa fa-comments fa-fw';
        cellText = 'HipChat';
    } else if (cell === 'slack_channel') {
        fontAwesomeClass = 'fa fa-slack fa-fw';
        cellText = 'Slack';
    }

    return (
        <div title={cellText}>
            <span key="icon" className={fontAwesomeClass} aria-hidden="true" />
            {cellText}
        </div>
    );
}


class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            autoRefresh: true,
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

    getCurrentJobConfig(currentRowSelected) {
        if (currentRowSelected != null) {
            const {
                id, name, distributionConfigId, distributionType, frequency, notificationTypes, groupName, includeAllProjects, configuredProjects
            } = currentRowSelected;

            if (distributionType === 'email_group_channel') {
                return (<GroupEmailJobConfiguration
                    id={id}
                    distributionConfigId={distributionConfigId}
                    name={name}
                    includeAllProjects={includeAllProjects}
                    frequency={frequency}
                    notificationTypes={notificationTypes}
                    waitingForGroups={this.state.waitingForGroups}
                    groups={this.state.groups}
                    groupName={groupName}
                    waitingForProjects={this.state.waitingForProjects}
                    projects={this.state.projects}
                    configuredProjects={configuredProjects}
                    handleCancel={this.cancelRowSelect}
                    projectTableMessage={this.state.projectTableMessage}
                />);
            } else if (distributionType === 'hipchat_channel') {
                return (<HipChatJobConfiguration
                    id={id}
                    distributionConfigId={distributionConfigId}
                    name={name}
                    includeAllProjects={includeAllProjects}
                    frequency={frequency}
                    notificationTypes={notificationTypes}
                    waitingForProjects={this.state.waitingForProjects}
                    projects={this.state.projects}
                    configuredProjects={configuredProjects}
                    handleCancel={this.cancelRowSelect}
                    projectTableMessage={this.state.projectTableMessage}
                />);
            } else if (distributionType === 'slack_channel') {
                return (<SlackJobConfiguration
                    id={id}
                    distributionConfigId={distributionConfigId}
                    name={name}
                    includeAllProjects={includeAllProjects}
                    frequency={frequency}
                    notificationTypes={notificationTypes}
                    waitingForProjects={this.state.waitingForProjects}
                    projects={this.state.projects}
                    configuredProjects={configuredProjects}
                    handleCancel={this.cancelRowSelect}
                    projectTableMessage={this.state.projectTableMessage}
                />);
            }
        }
        return null;
    }

    cancelRowSelect() {
        this.setState({
            currentRowSelected: null
        });
    }

    handleAutoRefreshChange({ target }) {
        if (target.checked) {
            this.startAutoReload();
        } else {
            this.cancelAutoReload();
        }
        this.setState({ [target.name]: target.checked });
    }

    reloadPage() {
        this.cancelAutoReload();
        this.setState({
            jobConfigTableMessage: 'Loading...',
            inProgress: true
        });
        this.fetchDistributionJobs();
    }

    cancelAutoReload() {
        clearInterval(this.reloadInterval);
    }

    startAutoReload() {
        // run the reload now and then every 10 seconds
        this.reloadInterval = setInterval(() => this.reloadPage(), 10000);
    }

    createCustomModal(onModalClose, onSave, columns, validateState, ignoreEditable) {
        return (
            <JobAddModal
                waitingForProjects={this.state.waitingForProjects}
                waitingForGroups={this.state.waitingForGroups}
                projects={this.state.projects}
                includeAllProjects
                groups={this.state.groups}
                groupError={this.state.groupError}
                projectTableMessage={this.state.projectTableMessage}
                handleCancel={this.cancelRowSelect}
                onModalClose={() => {
                    this.fetchDistributionJobs();
                    onModalClose();
                }}
                onSave={onSave}
                columns={columns}
                validateState={validateState}
                ignoreEditable={ignoreEditable}
            />
        );
    }

    customJobConfigDeletionConfirm(next, dropRowKeys) {
        if (confirm('Are you sure you want to delete these Job configurations?')) {
            console.log('Deleting the Job configs');
            // TODO delete the Job configs from the backend
            // dropRowKeys are the Id's of the Job configs
            const { jobs } = this.state;

            const matchingJobs = jobs.filter(job => dropRowKeys.includes(job.id));
            matchingJobs.forEach((job) => {
                const jsonBody = JSON.stringify(job);
                fetch('/api/configuration/distribution/common', {
                    method: 'DELETE',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: jsonBody
                }).then((response) => {
                    if (!response.ok) {
                        response.json().then((json) => {
                            const jsonErrors = json.errors;
                            if (jsonErrors) {
                                const errors = {};
                                for (const key in jsonErrors) {
                                    if (jsonErrors.hasOwnProperty(key)) {
                                        const name = key.concat('Error');
                                        const value = jsonErrors[key];
                                        errors[name] = value;
                                    }
                                }
                                this.setState({
                                    errors
                                });
                            }
                            this.setState({
                                jobConfigTableMessage: json.message
                            });
                        });
                    }
                }).catch(console.error);
            });
            next();
        }
    }

    retrieveProjects() {
        const self = this;
        fetch('/api/hub/projects', {
            credentials: 'same-origin'
        })
            .then((response) => {
                this.setState({ waitingForProjects: false });
                if (!response.ok) {
                    return response.json().then((json) => {
                        this.setState({ projectTableMessage: json.message });
                    });
                }
                return response.json().then((json) => {
                    this.setState({ projectTableMessage: ''});
                    const jsonArray = JSON.parse(json.message);
                    if (jsonArray != null && jsonArray.length > 0) {
                        const projects = [];
                        for (const index in jsonArray) {
                            projects.push({
                                name: jsonArray[index].name,
                                url: jsonArray[index].url
                            });
                        }
                        this.setState({
                            projects
                        });
                    }
                });
            })
            .catch((error) => {
                console.log(error);
            });
    }

    retrieveGroups() {
        const self = this;
        fetch('/api/hub/groups', {
            credentials: 'same-origin'
        })
            .then((response) => {
                this.setState({ waitingForGroups: false });
                if (!response.ok) {
                    return response.json().then((json) => {
                        setState({ 'groupError': json.message });
                    });
                }
                return response.json().then((json) => {
                    this.setState({ groupError: '' });
                    const jsonArray = JSON.parse(json.message);
                    if (jsonArray != null && jsonArray.length > 0) {
                        const groups = jsonArray.map(({ name, active, url }) => ({ name, active, url }));
                        this.setState({
                            groups
                        });
                    }
                });
            })
            .catch((error) => {
                console.log(error);
            });
    }

    fetchDistributionJobs() {
        const self = this;
        fetch('/api/configuration/distribution/common', {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then((response) => {
                this.setState({ inProgress: false });
                if (response.ok) {
                    this.setState({ jobConfigTableMessage: '' });
                    response.json().then((jsonArray) => {
                        const newJobs = new Array();
                        if (jsonArray != null && jsonArray.length > 0) {
                            jsonArray.forEach((item) => {
                                const jobConfig = {
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
                        this.setState({
                            jobs: newJobs
                        });
                    });
                } else {
                    response.json().then((json) => {
                        this.setState({ jobConfigTableMessage: json.message });
                    });
                }
            })
            .catch((error) => {
                console.log(error);
            });
    }

    editButtonClicked(currentRowSelected) {
        setState({ 'currentRowSelected': currentRowSelected });
    }

    editButtonClick(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.editButtonClicked} currentRowSelected={row} />;
    }


    createCustomButtonGroup(buttons) {
        const classes = 'btn btn-info react-bs-table-add-btn tableButton';
        const fontAwesomeIcon = 'fa fa-refresh fa-fw';
        const insertOnClick = buttons.insertBtn.props.onClick;
        const deleteOnClick = buttons.deleteBtn.props.onClick;
        const reloadEntries = () => this.reloadPage();
        let refreshButton = null;
        if (!this.state.autoRefresh) {
            refreshButton = (
                <div role="button" tabIndex={0} className={classes} onClick={reloadEntries}>
                    <span className={fontAwesomeIcon} aria-hidden="true" />Refresh
                </div>
            );
        }
        return (
            <div>
                <InsertButton className="addJobButton" onClick={insertOnClick} />
                <DeleteButton className="deleteJobButton" onClick={deleteOnClick} />
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
            bgColor(row, isSelect) {
                if (isSelect) {
                    return '#e8e8e8';
                }
                return null;
            }
        };

        let content = (
            <div>
                <BootstrapTable
                    hover
                    condensed
                    data={this.state.jobs}
                    containerClass="table"
                    insertRow
                    deleteRow
                    selectRow={jobsSelectRowProp}
                    search
                    options={jobTableOptions}
                    trClassName="tableRow"
                    headerContainerClass="scrollable"
                    bodyContainerClass="tableScrollableBody"
                >
                    <TableHeaderColumn dataField="id" isKey hidden>
                        Job Id
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="distributionConfigId" hidden>
                        Distribution Id
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">
                        Distribution Job
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="distributionType" dataSort columnClassName="tableCell" dataFormat={typeColumnDataFormat}>
                        Type
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="lastRan" dataSort columnTitle columnClassName="tableCell">
                        Last Run
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="status" dataSort columnTitle columnClassName={statusColumnClassNameFormat}>
                        Status
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="" width="48" columnClassName="tableCell" dataFormat={this.editButtonClick} />
                </BootstrapTable>

                {this.state.inProgress &&
                    <div className="progressIcon">
                        <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                    </div>
                }

                <p name="jobConfigTableMessage">{this.state.jobConfigTableMessage}</p>
            </div>
        );

        const currentJobContent = this.getCurrentJobConfig(this.state.currentRowSelected);
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
        );
    }
}

export default Index;
