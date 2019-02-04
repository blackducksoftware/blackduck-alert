import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import AutoRefresh from 'component/common/AutoRefresh';
import DescriptorLabel from 'component/common/DescriptorLabel';
import GroupEmailJobConfiguration from 'distribution/job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from 'distribution/job/HipChatJobConfiguration';
import SlackJobConfiguration from 'distribution/job/SlackJobConfiguration';
import EditTableCellFormatter from 'component/common/EditTableCellFormatter';
import JobAddModal from 'distribution/JobAddModal';
import { logout } from 'store/actions/session';
import { deleteDistributionJob, fetchDistributionJobs } from 'store/actions/distributions';

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

        this.state = { currentRowSelected: null };
    }

    componentDidMount() {
        this.reloadJobs();
    }

    componentWillUnmount() {
        this.cancelAutoReload();
    }

    getCurrentJobConfig(currentRowSelected) {
        if (currentRowSelected != null) {
            const { distributionConfigId, distributionType } = currentRowSelected;
            if (distributionType === DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL) {
                return (<GroupEmailJobConfiguration
                    alertChannelName={distributionType}
                    distributionConfigId={distributionConfigId}
                    handleCancel={this.cancelRowSelect}
                    handleSaveBtnClick={this.saveBtn}
                />);
            } else if (distributionType === DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_HIPCHAT) {
                return (<HipChatJobConfiguration
                    alertChannelName={distributionType}
                    distributionConfigId={distributionConfigId}
                    handleCancel={this.cancelRowSelect}
                    handleSaveBtnClick={this.saveBtn}
                />);
            } else if (distributionType === DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_SLACK) {
                return (<SlackJobConfiguration
                    alertChannelName={distributionType}
                    distributionConfigId={distributionConfigId}
                    handleCancel={this.cancelRowSelect}
                    handleSaveBtnClick={this.saveBtn}
                />);
            }
        }
        return null;
    }

    cancelRowSelect() {
        this.startAutoReloadIfConfigured();
        this.setState({
            currentRowSelected: null
        });
    }

    saveBtn() {
        this.startAutoReloadIfConfigured();
        this.cancelRowSelect();
        this.reloadJobs();
    }

    reloadJobs() {
        this.props.fetchDistributionJobs();
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
                projects={this.state.projects}
                includeAllProjects
                handleCancel={this.cancelRowSelect}
                onModalClose={() => {
                    this.props.fetchDistributionJobs();
                    this.startAutoReloadIfConfigured();
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
            const { jobs } = this.props;
            const matchingJobs = jobs.filter(job => dropRowKeys.includes(job.id));

            matchingJobs.forEach((job) => {
                this.props.deleteDistributionJob(job);
            });
            next();
        }
    }

    editButtonClicked(currentRowSelected) {
        this.cancelAutoReload();
        this.setState({ currentRowSelected });
    }

    editButtonClick(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.editButtonClicked} currentRowSelected={row} />;
    }


    createCustomButtonGroup(buttons) {
        const classes = 'btn btn-md btn-info react-bs-table-add-btn tableButton';
        const fontAwesomeIcon = 'fa fa-refresh fa-fw';
        const insertOnClick = buttons.insertBtn.props.onClick;
        const deleteOnClick = buttons.deleteBtn.props.onClick;
        const reloadEntries = () => this.reloadJobs();
        let refreshButton = null;
        if (!this.props.autoRefresh) {
            refreshButton = (
                <button type="button" tabIndex={0} className={classes} onClick={reloadEntries}>
                    <span className={fontAwesomeIcon} aria-hidden="true" />Refresh
                </button>
            );
        }
        return (
            <div>
                <InsertButton className="addJobButton btn-md" onClick={insertOnClick}>
                    <span className="fa fa-plus" />
                    New
                </InsertButton>
                <DeleteButton className="deleteJobButton btn-md" onClick={deleteOnClick}>
                    <span className="fa fa-trash" />
                    Delete
                </DeleteButton>
                {refreshButton}
            </div>
        );
    }

    typeColumnDataFormat(cell) {
        const defaultValue = <div className="inline">{cell}</div>;
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors.items, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === cell);
                if (filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix="distribution-channel-icon" descriptor={foundDescriptor} />);
                }
                return defaultValue;
            }
            return defaultValue;
        }
        return defaultValue;
    }

    providerColumnDataFormat(cell) {
        const defaultValue = <div className="inline">{cell}</div>;
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors.items, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === cell);
                if (filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix="distribution-provider-icon" descriptor={foundDescriptor} />);
                }
                return defaultValue;
            }
            return defaultValue;
        }
        return defaultValue;
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
                    version="4"
                    hover
                    condensed
                    data={this.props.jobs}
                    containerClass="table"
                    insertRow
                    deleteRow
                    selectRow={jobsSelectRowProp}
                    options={jobTableOptions}
                    search
                    trClassName="tableRow"
                    headerContainerClass="scrollable"
                    bodyContainerClass="tableScrollableBody"
                >
                    <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                    <TableHeaderColumn dataField="distributionConfigId" hidden>Distribution Id</TableHeaderColumn>
                    <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                    <TableHeaderColumn dataField="distributionType" dataSort columnClassName="tableCell" dataFormat={this.typeColumnDataFormat}>Type</TableHeaderColumn>
                    <TableHeaderColumn dataField="providerName" dataSort columnClassName="tableCell" dataFormat={this.providerColumnDataFormat}>Provider</TableHeaderColumn>
                    <TableHeaderColumn dataField="frequency" dataSort columnClassName="tableCell" dataFormat={frequencyColumnDataFormat}>Frequency Type</TableHeaderColumn>
                    <TableHeaderColumn dataField="lastRan" dataSort columnTitle columnClassName="tableCell">Last Run</TableHeaderColumn>
                    <TableHeaderColumn dataField="status" dataSort columnTitle columnClassName={statusColumnClassNameFormat}>Status</TableHeaderColumn>
                    <TableHeaderColumn dataField="" width="48" columnClassName="tableCell" dataFormat={this.editButtonClick} />
                </BootstrapTable>

                {this.props.inProgress &&
                <div className="progressIcon">
                    <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                </div>
                }

                <p name="jobConfigTableMessage">{this.props.jobConfigTableMessage}</p>
            </div>
        );

        const currentJobContent = this.getCurrentJobConfig(this.state.currentRowSelected);
        if (currentJobContent !== null) {
            content = currentJobContent;
        }
        return (
            <div>
                <h1>
                    <span className="fa fa-truck" />
                    Distribution
                    <small className="pull-right">
                        <AutoRefresh startAutoReload={this.startAutoReload} cancelAutoReload={this.cancelAutoReload} />
                    </small>
                </h1>
                {content}
            </div>
        );
    }
}

Index.propTypes = {
    deleteDistributionJob: PropTypes.func.isRequired,
    fetchDistributionJobs: PropTypes.func.isRequired,
    autoRefresh: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object),
    inProgress: PropTypes.bool.isRequired,
    jobs: PropTypes.arrayOf(PropTypes.object).isRequired,
    jobConfigTableMessage: PropTypes.string.isRequired
};

Index.defaultProps = {
    autoRefresh: true,
    descriptors: []
};

const mapStateToProps = state => ({
    autoRefresh: state.refresh.autoRefresh,
    descriptors: state.descriptors,
    inProgress: state.distributions.inProgress,
    jobs: state.distributions.jobs,
    jobConfigTableMessage: state.distributions.jobConfigTableMessage
});

const mapDispatchToProps = dispatch => ({
    deleteDistributionJob: job => dispatch(deleteDistributionJob(job)),
    fetchDistributionJobs: () => dispatch(fetchDistributionJobs())
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
