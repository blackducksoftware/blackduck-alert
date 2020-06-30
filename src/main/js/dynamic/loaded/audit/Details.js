import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Modal, Tab, Tabs } from 'react-bootstrap';
import DescriptorLabel from 'component/common/DescriptorLabel';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import RefreshTableCellFormatter from 'component/common/RefreshTableCellFormatter';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class Details extends Component {
    constructor(props) {
        super(props);

        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.getEventType = this.getEventType.bind(this);
        this.flattenJobsForTable = this.flattenJobsForTable.bind(this);
    }

    onResendClick(currentRowSelected) {
        const currentEntry = currentRowSelected || this.state.currentRowSelected;
        this.props.resendNotification(this.props.currentEntry.id, currentEntry.configId, this.props.currentPage, this.props.currentPageSize, this.props.searchTerm, this.props.sortField, this.props.sortOrder, this.props.onlyShowSentNotifications);
    }

    getEventType(eventType) {
        const defaultValue = <div className="inline">Unknown</div>;
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.name === eventType);
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
            errorMessage = <TextInput id="auditDetailErrorMessage" label="Error" readOnly name="errorMessage"
                                      value={row.errorMessage} />;
        }
        let errorStackTrace = null;
        if (row.errorStackTrace) {
            errorStackTrace =
                <TextArea id="auditDetailStackTrace" inputClass="auditJobDetails" sizeClass="col-sm-12"
                          label="Stack Trace" readOnly
                          name="errorStackTrace"
                          value={row.errorStackTrace} cols={'auto'} />;
        }

        return (<div className="auditJobDetails">{errorMessage}{errorStackTrace}</div>);
    }

    resendButton(cell, row) {
        return (<RefreshTableCellFormatter
            id="audit-detail-refresh-cell"
            handleButtonClicked={this.onResendClick} currentRowSelected={row}
            buttonText="Re-send" />);
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
        const jobTableOptions = {
            defaultSortName: 'timeLastSent',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            expandBy: 'column',
            expandRowBgColor: '#e8e8e8'
        };
        let jsonContent = null;
        if (this.props.currentEntry.content) {
            jsonContent = JSON.parse(this.props.currentEntry.content);
        } else {
            jsonContent = Object.assign({}, { warning: 'Content in an Unknown Format' });
        }
        const jsonPrettyPrintContent = JSON.stringify(jsonContent, null, 2);

        let flatJobs = [];
        if (this.props.currentEntry.jobs) {
            flatJobs = this.flattenJobsForTable(this.props.currentEntry.jobs);
        }
        return (
            <Modal size="lg" show={this.props.show} onHide={this.props.handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        <div className="notificationTitlePiece">
                            {this.props.providerNameFormat(this.props.currentEntry.provider)}
                        </div>
                        <div className="notificationTitlePiece">
                            {this.props.notificationTypeFormat(this.props.currentEntry.notificationType)}
                        </div>
                        <div className="notificationTitlePiece">
                            {this.props.currentEntry.createdAt}
                        </div>
                        <div className="notificationTitlePiece">
                            {this.props.statusFormat(this.props.currentEntry.overallStatus)}
                        </div>
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
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
                                        <TableHeaderColumn dataField="name" dataSort columnTitle
                                                           columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                                        <TableHeaderColumn dataField="eventType" dataSort columnClassName="tableCell"
                                                           dataFormat={this.getEventType}>Event Type</TableHeaderColumn>
                                        <TableHeaderColumn dataField="timeLastSent" dataSort columnTitle
                                                           columnClassName="tableCell">Time Last
                                            Sent</TableHeaderColumn>
                                        <TableHeaderColumn dataField="status" dataSort columnClassName="tableCell"
                                                           dataFormat={this.props.statusFormat}>Status</TableHeaderColumn>
                                        <TableHeaderColumn dataField="" width="48" expandable={false}
                                                           columnClassName="tableCell" dataFormat={this.resendButton} />
                                        <TableHeaderColumn dataField="configId" hidden>Job Id</TableHeaderColumn>
                                        <TableHeaderColumn dataField="id" isKey hidden>Audit Id</TableHeaderColumn>
                                    </BootstrapTable>
                                </div>
                            </Tab>
                            <Tab eventKey={2} title="Notification Content">
                                <div className="tableContainer">
                                    <TextArea inputClass="auditContentTextArea" sizeClass="col-sm-12" label="" readOnly
                                              name="notificationContent" value={jsonPrettyPrintContent} />
                                </div>
                            </Tab>
                        </Tabs>
                        <p name="message">{this.props.errorMessage}</p>
                    </div>
                </Modal.Body>

            </Modal>
        );
    }
}

Details.propTypes = {
    show: PropTypes.bool,
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
    errorMessage: null,
    show: false,
    descriptors: []
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Details);
