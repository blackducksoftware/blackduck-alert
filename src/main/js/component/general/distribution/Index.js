import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {BootstrapTable, DeleteButton, InsertButton, ReactBsTable, TableHeaderColumn} from 'react-bootstrap-table';

import AutoRefresh from '../../common/AutoRefresh';
import DescriptorLabel from '../../common/DescriptorLabel';
import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';
import EditTableCellFormatter from '../../common/EditTableCellFormatter';

import JobAddModal from './JobAddModal';
import {logout} from '../../../store/actions/session';

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
function frequencyColumnDataFormat(cell) {
    let cellText = '';
    if (cell === 'REAL_TIME') {
        cellText = 'Real Time';
    } else if (cell === 'DAILY') {
        cellText = 'Daily';
    }

    return (
        <div title={cellText}>
            {cellText}
        </div>
    );
}

class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            jobs: [],
            groups: [],
            waitingForGroups: true
        };
        this.startAutoReload = this.startAutoReload.bind(this);
        this.startAutoReloadIfConfigured = this.startAutoReloadIfConfigured.bind(this);
        this.cancelAutoReload = this.cancelAutoReload.bind(this);
        this.createCustomModal = this.createCustomModal.bind(this);
        this.createCustomButtonGroup = this.createCustomButtonGroup.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.editButtonClicked = this.editButtonClicked.bind(this);
        this.editButtonClick = this.editButtonClick.bind(this);
        this.customJobConfigDeletionConfirm = this.customJobConfigDeletionConfirm.bind(this);
        this.reloadJobs = this.reloadJobs.bind(this);
        this.saveBtn = this.saveBtn.bind(this);
        this.typeColumnDataFormat = this.typeColumnDataFormat.bind(this);
        this.providerColumnDataFormat = this.providerColumnDataFormat.bind(this);
    }

    componentDidMount() {
        this.retrieveGroups();
        this.reloadJobs();
    }

    componentWillUnmount() {
        this.cancelAutoReload();
    }

    getCurrentJobConfig(currentRowSelected) {
        if (currentRowSelected != null) {
            const { distributionConfigId, distributionType } = currentRowSelected;
            if (distributionType === 'channel_email') {
                return (<GroupEmailJobConfiguration
                    distributionConfigId={distributionConfigId}
                    groups= {this.state.groups}
                    waitingForGroups={this.state.waitingForGroups}
                    handleCancel={this.cancelRowSelect}
                    handleSaveBtnClick={this.saveBtn}
                />);
            } else if (distributionType === 'channel_hipchat') {
                return (<HipChatJobConfiguration
                    distributionConfigId={distributionConfigId}
                    handleCancel={this.cancelRowSelect}
                    handleSaveBtnClick={this.saveBtn}
                />);
            } else if (distributionType === 'channel_slack') {
                return (<SlackJobConfiguration
                    distributionConfigId={distributionConfigId}
                    handleCancel={this.cancelRowSelect}
                    handleSaveBtnClick={this.saveBtn}
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

    saveBtn() {
        this.cancelRowSelect();
        this.reloadJobs();
    }

    reloadJobs() {
        this.setState({
            jobConfigTableMessage: 'Loading...',
            inProgress: true
        });
        this.fetchDistributionJobs();
    }

    cancelAutoReload() {
        clearTimeout(this.timeout);
    }

    startAutoReload() {
        // Run reload in 10seconds - kill an existing timer if it exists.
        this.cancelAutoReload();
        this.timeout = setTimeout(() => this.reloadJobs(), 10000);
    }

    startAutoReloadIfConfigured() {
        if (this.props.autoRefresh) {
            this.startAutoReload();
        }
    }

    createCustomModal(onModalClose, onSave, columns, validateState, ignoreEditable) {
        return (
            <JobAddModal
                waitingForGroups={this.state.waitingForGroups}
                projects={this.state.projects}
                includeAllProjects
                groups={this.state.groups}
                groupError={this.state.groupError}
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
            const {jobs} = this.state;
            const matchingJobs = jobs.filter(job => dropRowKeys.includes(job.id));

            matchingJobs.forEach((job) => {
                const deleteUrl = `/alert/api/configuration/channel/distribution/${job.distributionType}?id=${job.id}`;
                fetch(deleteUrl, {
                    method: 'DELETE',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': this.props.csrfToken
                    }
                }).then((response) => {
                    if (!response.ok) {
                        response.json().then((json) => {
                            const jsonErrors = json.errors;
                            if (jsonErrors) {
                                const errors = {};

                                Object.keys(jsonErrors).forEach((key) => {
                                    errors[`${key}Error`] = jsonErrors[key];
                                });
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

    retrieveGroups() {
        fetch('/alert/api/blackduck/groups', {
            credentials: 'same-origin'
        }).then((response) => {
            this.setState({waitingForGroups: false});
            if (!response.ok) {
                return response.json().then((json) => {
                    this.setState({groupError: json.message});
                });
            }
            return response.json().then((json) => {
                this.setState({groupError: ''});
                const jsonArray = JSON.parse(json.message);
                if (jsonArray != null && jsonArray.length > 0) {
                    const groups = jsonArray.map(({name, active, url}) => ({name, active, url}));
                    this.setState({
                        groups
                    });
                }
            });
        }).catch((error) => {
            console.log(error);
        });
    }

    fetchDistributionJobs() {
        fetch('/alert/api/configuration/channel/distribution', {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            this.setState({inProgress: false});
            this.startAutoReloadIfConfigured();
            if (response.ok) {
                this.setState({jobConfigTableMessage: ''});
                response.json().then((jsonArray) => {
                    const newJobs = [];
                    if (jsonArray != null && jsonArray.length > 0) {
                        jsonArray.forEach((item) => {
                            const jobConfig = {
                                id: item.id,
                                distributionConfigId: item.distributionConfigId,
                                name: item.name,
                                distributionType: item.distributionType,
                                providerName: item.providerName,
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
                switch (response.status) {
                    case 401:
                    case 403:
                        this.props.logout();
                    default:
                        response.json().then((json) => {
                            this.setState({jobConfigTableMessage: json.message});
                        });
                }
            }
        }).catch((error) => {
            this.startAutoReloadIfConfigured();
            console.log(error);
        });
    }

    editButtonClicked(currentRowSelected) {
        this.setState({currentRowSelected});
    }

    editButtonClick(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.editButtonClicked} currentRowSelected={row}/>;
    }


    createCustomButtonGroup(buttons) {
        const classes = 'btn btn-sm btn-info react-bs-table-add-btn tableButton';
        const fontAwesomeIcon = 'fa fa-refresh fa-fw';
        const insertOnClick = buttons.insertBtn.props.onClick;
        const deleteOnClick = buttons.deleteBtn.props.onClick;
        const reloadEntries = () => this.reloadJobs();
        let refreshButton = null;
        if (!this.props.autoRefresh) {
            refreshButton = (
                <button type="button" tabIndex={0} className={classes} onClick={reloadEntries}>
                    <span className={fontAwesomeIcon} aria-hidden="true"/>Refresh
                </button>
            );
        }
        return (
            <div>
                <InsertButton className="addJobButton btn-sm" onClick={insertOnClick}>
                    <span className="fa fa-plus"/>
                    New
                </InsertButton>
                <DeleteButton className="deleteJobButton btn-sm" onClick={deleteOnClick}>
                    <span className="fa fa-trash"/>
                    Delete
                </DeleteButton>
                {refreshButton}
            </div>
        );
    }

    typeColumnDataFormat(cell) {
        const defaultValue = <div className="inline">{cell}</div>;
        if(this.props.descriptors) {
            const descriptorList = this.props.descriptors.items['CHANNEL_DISTRIBUTION_CONFIG'];
            if(descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === cell)
                if(filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix='distribution-channel-icon' descriptor={foundDescriptor}/>);
                } else {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    providerColumnDataFormat(cell) {
        const defaultValue = <div className="inline">{cell}</div>;
        if(this.props.descriptors) {
            const descriptorList = this.props.descriptors.items['PROVIDER_CONFIG'];
            if(descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === cell)
                if(filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix='distribution-provider-icon' descriptor={foundDescriptor}/>);
                } else {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
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
                    <TableHeaderColumn dataField="distributionType" dataSort columnClassName="tableCell" dataFormat={this.typeColumnDataFormat}>
                        Type
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="providerName" dataSort columnClassName="tableCell" dataFormat={this.providerColumnDataFormat}> Provider </TableHeaderColumn>
                    <TableHeaderColumn dataField="frequency" dataSort columnClassName="tableCell" dataFormat={frequencyColumnDataFormat}>
                        Digest Type
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="lastRan" dataSort columnTitle columnClassName="tableCell">
                        Last Run
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="status" dataSort columnTitle columnClassName={statusColumnClassNameFormat}>
                        Status
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="" width="48" columnClassName="tableCell" dataFormat={this.editButtonClick}/>
                </BootstrapTable>

                {this.state.inProgress &&
                <div className="progressIcon">
                    <span className="fa fa-spinner fa-pulse" aria-hidden="true"/>
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
                    <span className="fa fa-truck"/>
                    Distribution
                    <small className="pull-right">
                        <AutoRefresh startAutoReload={this.startAutoReload} cancelAutoReload={this.cancelAutoReload}/>
                    </small>
                </h1>
                {content}
            </div>
        );
    }
}

Index.propTypes = {
    autoRefresh: PropTypes.bool,
    csrfToken: PropTypes.string,
    descriptors: PropTypes.object
};

Index.defaultProps = {
    csrfToken: null,
    descriptor: {}
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    autoRefresh: state.refresh.autoRefresh,
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    logout: () => dispatch(logout())
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
