import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BootstrapTable, ButtonGroup, TableHeaderColumn } from 'react-bootstrap-table';
import { getAuditData, resendNotification } from 'store/actions/audit';
import AutoRefresh from 'common/table/AutoRefresh';
import DescriptorLabel from 'common/DescriptorLabel';
import RefreshTableCellFormatter from 'common/table/RefreshTableCellFormatter';
import NotificationTypeLegend from 'page/audit/NotificationTypeLegend';
import AuditDetails from 'page/audit/Details';
import CheckboxInput from 'common/input/CheckboxInput';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import ConfigurationLabel from 'common/ConfigurationLabel';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import StatusMessage from 'common/StatusMessage';
import '../../../css/audit.scss';
import { AUDIT_INFO } from 'page/audit/AuditModel';
import { EXISTING_CHANNELS, EXISTING_PROVIDERS } from 'common/DescriptorInfo';
import { ProgressIcon } from 'common/table/ProgressIcon';

class AuditPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            entries: [],
            currentPage: 1,
            currentPageSize: 10,
            searchTerm: '',
            sortField: 'lastSent',
            sortOrder: 'desc',
            onlyShowSentNotifications: true,
            currentRowSelected: {},
            showDetailModal: false
        };
        this.setEntriesFromArray = this.setEntriesFromArray.bind(this);
        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.onStatusFailureClick = this.onStatusFailureClick.bind(this);
        this.statusColumnDataFormat = this.statusColumnDataFormat.bind(this);
        this.createCustomButtonGroup = this.createCustomButtonGroup.bind(this);
        this.refreshAuditEntries = this.refreshAuditEntries.bind(this);
        this.reloadAuditEntries = this.reloadAuditEntries.bind(this);
        this.onOnlyShowSentNotificationsChange = this.onOnlyShowSentNotificationsChange.bind(this);
        this.onSizePerPageListChange = this.onSizePerPageListChange.bind(this);
        this.onPageChange = this.onPageChange.bind(this);
        this.onSearchChange = this.onSearchChange.bind(this);
        this.providerColumnDataFormat = this.providerColumnDataFormat.bind(this);
        this.onSortChange = this.onSortChange.bind(this);
        this.handleCloseDetails = this.handleCloseDetails.bind(this);
        this.onRowClick = this.onRowClick.bind(this);
        this.isResendAllowed = this.isResendAllowed.bind(this);
    }

    componentDidMount() {
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm, this.state.sortField, this.state.sortOrder, this.state.onlyShowSentNotifications);
    }

    // FIXME componentWillReceiveProps is deprecated
    componentWillReceiveProps(nextProps) {
        if (nextProps.items !== this.props.items) {
            this.setEntriesFromArray(nextProps.items);
        }
    }

    onResendClick(currentRowSelected) {
        const currentEntry = currentRowSelected || this.state.currentRowSelected;
        this.props.resendNotification(currentEntry.id, null, this.state.currentPage, this.state.currentPageSize, this.state.searchTerm, this.state.sortField, this.state.sortOrder, this.state.onlyShowSentNotifications);
    }

    onStatusFailureClick(currentRowSelected) {
        this.setState({
            currentRowSelected
        });
    }

    onOnlyShowSentNotificationsChange({ target }) {
        const value = target.checked;
        this.setState({
            onlyShowSentNotifications: value
        });
        this.reloadAuditEntries(null, null, null, null, null, value);
    }

    onSizePerPageListChange(sizePerPage) {
        this.setState({
            currentPage: 1,
            currentPageSize: sizePerPage
        });

        this.reloadAuditEntries(1, sizePerPage);
    }

    onPageChange(page, sizePerPage) {
        this.setState({ currentPage: page });
        this.reloadAuditEntries(page, sizePerPage);
    }

    onSearchChange(searchText, colInfos, multiColumnSearch) {
        this.setState({
            currentPage: 1,
            searchTerm: searchText
        });
        this.reloadAuditEntries(null, null, searchText);
    }

    onSortChange(sortName, sortOrder) {
        this.setState({
            sortField: sortName,
            sortOrder
        });
        this.reloadAuditEntries(null, null, null, sortName, sortOrder);
    }

    onRowClick(row) {
        this.setState({
            currentRowSelected: row,
            showDetailModal: true
        });
    }

    setEntriesFromArray(jsonArray = []) {
        const entries = jsonArray.map((entry) => {
            const result = {
                id: entry.id,
                createdAt: entry.notification.createdAt,
                notificationType: entry.notification.notificationType,
                provider: entry.notification.provider,
                content: entry.notification.content,
                overallStatus: entry.overallStatus,
                lastSent: entry.lastSent
            };
            if (entry.jobs) {
                result.jobs = entry.jobs;
            }
            return result;
        });

        this.setState({
            entries
        });
    }

    statusColumnDataFormat(cell) {
        let statusClass = '';
        if (cell === 'Pending') {
            statusClass = 'statusPending';
        } else if (cell === 'Success') {
            statusClass = 'statusSuccess';
        } else if (cell === 'Failure') {
            statusClass = 'statusFailure';
        }
        return (<div className={statusClass} aria-hidden>{cell}</div>);
    }

    notificationTypeDataFormat(cell) {
        const types = Array.isArray(cell) ? cell : [cell];
        return (<NotificationTypeLegend notificationTypes={types} />);
    }

    trClassFormat(row, rowIndex) {
        // color the row correctly, since Striped does not work with expandable rows
        return rowIndex % 2 === 0 ? 'tableEvenRow' : 'tableRow';
    }

    refreshAuditEntries() {
        this.reloadAuditEntries(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm, this.state.sortField, this.state.sortOrder, this.state.onlyShowSentNotifications);
    }

    reloadAuditEntries(currentPage, sizePerPage, searchTerm, sortField, sortOrder, onlyShowSentNotifications) {
        let page = 1;
        if (currentPage) {
            page = currentPage;
        } else if (this.state.currentPage) {
            page = this.state.currentPage;
        }

        let size = 10;
        if (sizePerPage) {
            size = sizePerPage;
        } else if (this.state.currentPageSize) {
            size = this.state.currentPageSize;
        }

        let term = '';
        if (searchTerm) {
            term = searchTerm;
        } else if (this.state.searchTerm) {
            term = this.state.searchTerm;
        }

        let sortingField = '';
        if (sortField) {
            sortingField = sortField;
        } else if (this.state.sortField) {
            sortingField = this.state.sortField;
        }
        let sortingOrder = '';
        if (sortOrder) {
            sortingOrder = sortOrder;
        } else if (this.state.sortOrder) {
            sortingOrder = this.state.sortOrder;
        }

        let sentNotificationsOnly = false;
        // This seems to be the most correct way of checking if the variable is a boolean and not null or undefined
        if (onlyShowSentNotifications === true || onlyShowSentNotifications === false) {
            sentNotificationsOnly = onlyShowSentNotifications;
        } else if (this.state.onlyShowSentNotifications === true) {
            sentNotificationsOnly = this.state.onlyShowSentNotifications;
        }

        this.props.getAuditData(page, size, term, sortingField, sortingOrder, sentNotificationsOnly);
    }

    cancelRowSelect() {
        this.setState({
            currentRowSelected: null
        });
    }

    resendButton(cell, row) {
        if (this.isResendAllowed() && row.content) {
            return (
                <RefreshTableCellFormatter
                    id="audit-refresh-cell"
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
            if (descriptorList) {
                return descriptorList.some((descriptor) => DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.EXECUTE));
            }
        }

        return false;
    }

    createCustomButtonGroup(buttons) {
        return (
            <ButtonGroup>
                {!this.props.autoRefresh
                && (
                    <div
                        id="audit-refresh-button"
                        role="button"
                        tabIndex={0}
                        className="btn btn-info react-bs-table-add-btn tableButton"
                        onClick={this.refreshAuditEntries}
                    >
                        <span>
                            <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />
                            Refresh
                        </span>
                    </div>
                )}
            </ButtonGroup>
        );
    }

    providerColumnDataFormat(cell) {
        const descriptorOptions = {
            ...EXISTING_PROVIDERS,
            ...EXISTING_CHANNELS
        };
        const descriptor = descriptorOptions[cell];
        return (<DescriptorLabel keyPrefix="audit-provider-icon" descriptor={descriptor} />);
    }

    handleCloseDetails() {
        const { message, errorMessage } = this.props;
        if (message || errorMessage) {
            this.reloadAuditEntries();
        }
        this.setState({ showDetailModal: false });
    }

    render() {
        const {
            showDetailModal, onlyShowSentNotifications, sortField, searchTerm, currentRowSelected, entries, currentPage, currentPageSize, sortOrder
        } = this.state;
        const {
            errorMessage, resendNotification: resendNotificationAction, inProgress, message, totalPageCount, autoRefresh
        } = this.props;

        const auditTableOptions = {
            defaultSortName: 'lastSent',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            sizePerPage: currentPageSize,
            page: currentPage,
            // We need all of these onChange methods because the table is using the remote option
            onPageChange: this.onPageChange,
            onSizePerPageList: this.onSizePerPageListChange,
            onSearchChange: this.onSearchChange,
            onSortChange: this.onSortChange,
            onRowClick: this.onRowClick
        };

        const auditFetchInfo = {
            dataTotalSize: totalPageCount * currentPageSize
        };

        const shouldRefresh = !showDetailModal;

        return (
            <div>
                <ConfigurationLabel configurationName={AUDIT_INFO.label} description="Audit tracks all distribution events that have been produced by Alert and displays whether the event was successful or not. If an event fails, this page offers the ability to resend that event and see why it failed." />
                <div className="pull-right">
                    <AutoRefresh startAutoReload={this.reloadAuditEntries} autoRefresh={autoRefresh} isEnabled={shouldRefresh} />
                </div>
                <div className="pull-right">
                    <CheckboxInput
                        id="showSentNotificationsID"
                        label="Only show sent notifications"
                        name="onlyShowSentNotifications"
                        showDescriptionPlaceHolder={false}
                        labelClass="tableCheckbox"
                        isChecked={onlyShowSentNotifications}
                        onChange={this.onOnlyShowSentNotificationsChange}
                    />
                </div>
                <div>
                    <AuditDetails
                        handleClose={this.handleCloseDetails}
                        show={showDetailModal}
                        currentEntry={currentRowSelected}
                        resendNotification={resendNotificationAction}
                        providerNameFormat={this.providerColumnDataFormat}
                        notificationTypeFormat={this.notificationTypeDataFormat}
                        statusFormat={this.statusColumnDataFormat}
                        currentPage={currentPage}
                        currentPageSize={currentPageSize}
                        searchTerm={searchTerm}
                        sortField={sortField}
                        sortOrder={sortOrder}
                        actionMessage={message}
                        errorMessage={errorMessage}
                        onlyShowSentNotifications={onlyShowSentNotifications}
                    />
                    {!showDetailModal && !inProgress
                    && (
                        <StatusMessage
                            id="audit-status-message"
                            actionMessage={message}
                            errorMessage={errorMessage}
                        />
                    )}
                    <BootstrapTable
                        version="4"
                        trClassName={this.trClassFormat}
                        condensed
                        data={entries}
                        containerClass="table"
                        fetchInfo={auditFetchInfo}
                        options={auditTableOptions}
                        headerContainerClass="scrollable"
                        bodyContainerClass="auditTableScrollableBody"
                        remote
                        pagination
                        search
                    >
                        <TableHeaderColumn
                            dataField="provider"
                            dataSort
                            columnClassName="tableCell"
                            dataFormat={this.providerColumnDataFormat}
                        >
                            Provider
                        </TableHeaderColumn>
                        <TableHeaderColumn
                            dataField="notificationType"
                            dataSort
                            columnClassName="tableCell"
                            dataFormat={this.notificationTypeDataFormat}
                        >
                            Notification Types
                        </TableHeaderColumn>
                        <TableHeaderColumn
                            dataField="createdAt"
                            dataSort
                            columnTitle
                            columnClassName="tableCell"
                        >
                            Time Retrieved
                        </TableHeaderColumn>
                        <TableHeaderColumn
                            dataField="lastSent"
                            dataSort
                            columnTitle
                            columnClassName="tableCell"
                        >
                            Last Sent
                        </TableHeaderColumn>
                        <TableHeaderColumn
                            dataField="overallStatus"
                            dataSort
                            columnClassName="tableCell"
                            dataFormat={this.statusColumnDataFormat}
                        >
                            Status
                        </TableHeaderColumn>
                        <TableHeaderColumn width="48" columnClassName="tableCell" dataFormat={this.resendButton} />
                        <TableHeaderColumn dataField="id" isKey hidden>Notification Id</TableHeaderColumn>
                    </BootstrapTable>
                    <ProgressIcon inProgress={inProgress} />
                </div>
            </div>
        );
    }
}

AuditPage.defaultProps = {
    inProgress: false,
    message: '',
    errorMessage: null,
    autoRefresh: true,
    fetching: false,
    totalPageCount: 0,
    descriptors: [],
    items: [],
    description: '',
    label: ''
};

AuditPage.propTypes = {
    inProgress: PropTypes.bool,
    message: PropTypes.string,
    errorMessage: PropTypes.string,
    autoRefresh: PropTypes.bool,
    fetching: PropTypes.bool,
    items: PropTypes.arrayOf(PropTypes.object),
    totalPageCount: PropTypes.number,
    getAuditData: PropTypes.func.isRequired,
    resendNotification: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

const mapStateToProps = (state) => ({
    message: state.audit.message,
    errorMessage: state.audit.error.message,
    inProgress: state.audit.inProgress,
    totalPageCount: state.audit.totalPageCount,
    items: state.audit.items,
    fetching: state.audit.fetching,
    autoRefresh: state.refresh.autoRefresh,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = (dispatch) => ({
    getAuditData: (totalPageCount, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) => dispatch(getAuditData(totalPageCount, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications)),
    resendNotification: (notificationId, commonConfigId, totalPageCount, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) => dispatch(resendNotification(notificationId, commonConfigId, totalPageCount, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications))
});

export default connect(mapStateToProps, mapDispatchToProps)(AuditPage);
