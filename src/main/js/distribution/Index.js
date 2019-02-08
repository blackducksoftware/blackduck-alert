import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import AutoRefresh from 'component/common/AutoRefresh';
import DescriptorLabel from 'component/common/DescriptorLabel';
import EmailJobConfiguration from 'distribution/job/EmailJobConfiguration';
import HipChatJobConfiguration from 'distribution/job/HipChatJobConfiguration';
import SlackJobConfiguration from 'distribution/job/SlackJobConfiguration';
import EditTableCellFormatter from 'component/common/EditTableCellFormatter';
import JobAddModal from 'distribution/JobAddModal';
import { fetchDistributionJobs } from 'store/actions/distributions';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import JobDeleteModal from 'distribution/JobDeleteModal';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

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
        this.onJobDeleteClose = this.onJobDeleteClose.bind(this);
        this.onJobDeleteSubmit = this.onJobDeleteSubmit.bind(this);

        this.state = {
            currentRowSelected: null,
            jobsToDelete: [],
            showDeleteModal: false,
            nextDelete: null
        };
    }

    componentDidMount() {
        this.reloadJobs();
    }

    componentWillUnmount() {
        this.cancelAutoReload();
    }

    onJobDeleteSubmit() {
        this.state.nextDelete();
    }

    onJobDeleteClose() {
        this.setState({
            showDeleteModal: false,
            nextDelete: null,
            jobsToDelete: []
        });
    }

    getCurrentJobConfig(currentRowSelected) {
        if (currentRowSelected != null) {
            const { distributionConfigId, distributionType } = currentRowSelected;
            if (distributionType === DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL) {
                return (<EmailJobConfiguration
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
        const { jobs } = this.props;
        const matchingJobs = jobs.filter(job => dropRowKeys.includes(job.jobId));
        this.setState({
            showDeleteModal: true,
            nextDelete: next,
            jobsToDelete: matchingJobs
        });
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
            const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.name === cell);
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
            const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.name === cell);
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

    createTableData(jobs) {
        const tableData = [];
        if (jobs) {
            jobs.forEach((job) => {
                const channelModel = job.fieldModels
                    .find(fieldModel => fieldModel.descriptorName.startsWith('channel_'));
                const providerModel = job.fieldModels
                    .find(fieldModel => fieldModel.descriptorName.startsWith('provider_'));
                const id = job.jobId;
                const name = FieldModelUtilities.getFieldModelSingleValue(channelModel, 'channel.common.name');
                const distributionType = channelModel.descriptorName;
                const providerName = providerModel.descriptorName;
                const frequency = FieldModelUtilities.getFieldModelSingleValue(channelModel, 'channel.common.frequency');
                const lastRan = FieldModelUtilities.getFieldModelSingleValue(job, 'lastRan');
                const status = FieldModelUtilities.getFieldModelSingleValue(job, 'status');
                const entry = Object.assign({}, {
                    id,
                    name,
                    distributionType,
                    providerName,
                    frequency,
                    lastRan,
                    status
                });
                tableData.push(entry);
            });
        }
        return tableData;
    }

    render() {
        const tableData = this.createTableData(this.props.jobs);
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
                    data={tableData}
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
                <JobDeleteModal
                    createTableData={this.createTableData}
                    onModalSubmit={this.onJobDeleteSubmit}
                    onModalClose={this.onJobDeleteClose}
                    typeColumnDataFormat={this.typeColumnDataFormat}
                    providerColumnDataFormat={this.providerColumnDataFormat}
                    frequencyColumnDataFormat={frequencyColumnDataFormat}
                    statusColumnClassNameFormat={statusColumnClassNameFormat}
                    jobs={this.state.jobsToDelete}
                    show={this.state.showDeleteModal}
                />
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
    fetchDistributionJobs: PropTypes.func.isRequired,
    autoRefresh: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object),
    inProgress: PropTypes.bool.isRequired,
    jobs: PropTypes.arrayOf(PropTypes.object).isRequired,
    jobConfigTableMessage: PropTypes.string
};

Index.defaultProps = {
    autoRefresh: true,
    descriptors: [],
    jobConfigTableMessage: ''
};

const mapStateToProps = state => ({
    autoRefresh: state.refresh.autoRefresh,
    descriptors: state.descriptors.items,
    inProgress: state.distributions.inProgress,
    jobs: state.distributions.jobs,
    jobConfigTableMessage: state.distributions.jobConfigTableMessage
});

const mapDispatchToProps = dispatch => ({
    fetchDistributionJobs: () => dispatch(fetchDistributionJobs())
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
