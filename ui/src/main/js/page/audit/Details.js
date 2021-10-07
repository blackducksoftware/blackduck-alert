import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Modal, Tab, Tabs } from 'react-bootstrap';
import DescriptorLabel from 'common/DescriptorLabel';
import TextInput from 'common/input/TextInput';
import TextArea from 'common/input/TextArea';
import RefreshTableCellFormatter from 'common/table/RefreshTableCellFormatter';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import StatusMessage from 'common/StatusMessage';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class Details extends Component {
    constructor(props) {
        super(props);

        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.getEventType = this.getEventType.bind(this);
        this.flattenJobsForTable = this.flattenJobsForTable.bind(this);
        this.isResendAllowed = this.isResendAllowed.bind(this);
    }

    onResendClick(currentRowSelected) {
        const currentEntry = currentRowSelected;
        const {
            currentEntry: currentSelectedEntry, currentPageSize, searchTerm, currentPage, resendNotification, sortField, onlyShowSentNotifications, sortOrder
        } = this.props;
        resendNotification(currentSelectedEntry.id, currentEntry.configId, currentPage, currentPageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications);
    }

    getEventType(eventType) {
        const defaultValue = <div className="inline">Unknown</div>;
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            if (descriptorList) {
                const filteredList = descriptorList.filter((descriptor) => descriptor.name === eventType);
                if (filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix="audit-detail-icon" descriptor={foundDescriptor} />);
                }
            }
        }
        return (defaultValue);
    }

    expandComponent(row) {
        let errorMessage = null;
        if (row.errorMessage) {
            errorMessage = (
                <TextInput
                    id="auditDetailErrorMessage"
                    label="Error"
                    readOnly
                    name="errorMessage"
                    value={row.errorMessage}
                />
            );
        }
        let errorStackTrace = null;
        if (row.errorStackTrace) {
            errorStackTrace = (
                <TextArea
                    id="auditDetailStackTrace"
                    inputClass="auditJobDetails"
                    sizeClass="col-sm-8"
                    label="Stack Trace"
                    readOnly
                    name="errorStackTrace"
                    value={row.errorStackTrace}
                    cols="auto"
                />
            );
        }

        return (
            <div className="auditJobDetails">
                {errorMessage}
                {errorStackTrace}
            </div>
        );
    }

    resendButton(cell, row) {
        if (this.isResendAllowed()) {
            return (
                <RefreshTableCellFormatter
                    id="audit-detail-refresh-cell"
                    handleButtonClicked={this.onResendClick}
                    currentRowSelected={row}
                    buttonText="Re-send"
                />
            );
        }
        return (<div className="jobIconButtonDisabled"><FontAwesomeIcon icon="sync" className="alert-icon" size="lg" /></div>);
    }

    isResendAllowed() {
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_AUDIT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
            return descriptorList && descriptorList.some((descriptor) => DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.EXECUTE));
        }

        return false;
    }

    flattenJobsForTable(jsonArray = []) {
        const jobs = jsonArray.map((entry) => {
            const result = {
                id: entry.id,
                configId: entry.configId,
                name: entry.name,
                eventType: entry.eventType,
                timeLastSent: entry.auditJobStatusModel.timeLastSent,
                status: entry.auditJobStatusModel.status,
                errorMessage: entry.errorMessage,
                errorStackTrace: entry.errorStackTrace
            };
            return result;
        });
        return jobs;
    }

    render() {
        const {
            actionMessage, errorMessage, currentEntry, show, handleClose, statusFormat, providerNameFormat, notificationTypeFormat
        } = this.props;

        const jobTableOptions = {
            defaultSortName: 'timeLastSent',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            expandBy: 'column',
            expandRowBgColor: '#e8e8e8'
        };
        let jsonContent;
        if (currentEntry.content) {
            jsonContent = JSON.parse(currentEntry.content);
        } else {
            jsonContent = { warning: 'Content in an Unknown Format' };
        }
        const jsonPrettyPrintContent = JSON.stringify(jsonContent, null, 2);

        let flatJobs = [];
        if (currentEntry.jobs) {
            flatJobs = this.flattenJobsForTable(currentEntry.jobs);
        }
        return (
            <Modal size="lg" show={show} onHide={handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        <div className="notificationTitlePiece">
                            {providerNameFormat(currentEntry.provider)}
                        </div>
                        <div className="notificationTitlePiece">
                            {notificationTypeFormat(currentEntry.notificationType)}
                        </div>
                        <div className="notificationTitlePiece">
                            {currentEntry.createdAt}
                        </div>
                        <div className="notificationTitlePiece">
                            {statusFormat(currentEntry.overallStatus)}
                        </div>
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <StatusMessage
                        actionMessage={actionMessage}
                        errorMessage={errorMessage}
                    />
                    <div className="expandableContainer">
                        <Tabs defaultActiveKey={1} id="audit-details-tabs">
                            <Tab eventKey={1} title="Distribution Jobs">
                                <div className="container-fluid">
                                    <BootstrapTable
                                        version="4"
                                        trClassName={this.trClassFormat}
                                        condensed
                                        data={flatJobs}
                                        expandableRow={() => true}
                                        expandComponent={this.expandComponent}
                                        containerClass="table"
                                        options={jobTableOptions}
                                        headerContainerClass="scrollable"
                                        bodyContainerClass="auditTableScrollableBody"
                                        pagination
                                        search
                                    >
                                        <TableHeaderColumn
                                            dataField="name"
                                            dataSort
                                            columnTitle
                                            columnClassName="tableCell"
                                        >
                                            Distribution Job
                                        </TableHeaderColumn>
                                        <TableHeaderColumn
                                            dataField="eventType"
                                            dataSort
                                            columnClassName="tableCell"
                                            dataFormat={this.getEventType}
                                        >
                                            Event Type
                                        </TableHeaderColumn>
                                        <TableHeaderColumn
                                            dataField="timeLastSent"
                                            dataSort
                                            columnTitle
                                            columnClassName="tableCell"
                                        >
                                            Time Last
                                            Sent
                                        </TableHeaderColumn>
                                        <TableHeaderColumn
                                            dataField="status"
                                            dataSort
                                            columnClassName="tableCell"
                                            dataFormat={statusFormat}
                                        >
                                            Status
                                        </TableHeaderColumn>
                                        <TableHeaderColumn
                                            dataField=""
                                            width="48"
                                            expandable={false}
                                            columnClassName="tableCell"
                                            dataFormat={this.resendButton}
                                        />
                                        <TableHeaderColumn dataField="configId" hidden>Job Id</TableHeaderColumn>
                                        <TableHeaderColumn dataField="id" isKey hidden>Audit Id</TableHeaderColumn>
                                    </BootstrapTable>
                                </div>
                            </Tab>
                            <Tab eventKey={2} title="Notification Content">
                                <div className="tableContainer">
                                    <TextArea
                                        inputClass="auditContentTextArea"
                                        sizeClass="col-sm-12"
                                        label=""
                                        readOnly
                                        name="notificationContent"
                                        value={jsonPrettyPrintContent}
                                    />
                                </div>
                            </Tab>
                        </Tabs>
                    </div>
                </Modal.Body>

            </Modal>
        );
    }
}

Details.propTypes = {
    show: PropTypes.bool,
    actionMessage: PropTypes.string,
    errorMessage: PropTypes.string,
    descriptors: PropTypes.arrayOf(PropTypes.object),
    currentEntry: PropTypes.object.isRequired,
    resendNotification: PropTypes.func.isRequired,
    handleClose: PropTypes.func.isRequired,
    providerNameFormat: PropTypes.func.isRequired,
    notificationTypeFormat: PropTypes.func.isRequired,
    statusFormat: PropTypes.func.isRequired,
    currentPage: PropTypes.number.isRequired,
    currentPageSize: PropTypes.number.isRequired,
    searchTerm: PropTypes.string.isRequired,
    sortField: PropTypes.string.isRequired,
    sortOrder: PropTypes.string.isRequired,
    onlyShowSentNotifications: PropTypes.bool.isRequired
};

Details.defaultProps = {
    actionMessage: null,
    errorMessage: null,
    show: false,
    descriptors: []
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items
});

const mapDispatchToProps = (dispatch) => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Details);
