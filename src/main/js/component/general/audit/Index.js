import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {BootstrapTable, ButtonGroup, TableHeaderColumn} from 'react-bootstrap-table';
import {getAuditData} from '../../../store/actions/audit';
import AutoRefresh from '../../common/AutoRefresh';
import DescriptorLabel from '../../common/DescriptorLabel';
import RefreshTableCellFormatter from '../../common/RefreshTableCellFormatter';
import NotificationTypeLegend from '../../common/NotificationTypeLegend';

import '../../../../css/audit.scss';
import {logout} from '../../../store/actions/session';
import AuditDetails from "./Details";
import CheckboxInput from "../../../field/input/CheckboxInput";

class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            message: '',
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
        // this.addDefaultEntries = this.addDefaultEntries.bind(this);
        this.cancelAutoReload = this.cancelAutoReload.bind(this);
        this.startAutoReload = this.startAutoReload.bind(this);
        this.setEntriesFromArray = this.setEntriesFromArray.bind(this);
        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.resendNotification = this.resendNotification.bind(this);
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
    }

    componentDidMount() {
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm, this.state.sortField, this.state.sortOrder, this.state.onlyShowSentNotifications);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.items !== this.props.items) {
            this.setState({message: '', inProgress: false});
            this.setEntriesFromArray(nextProps.items);
        }

        if (!nextProps.fetching && this.props.autoRefresh) {
            this.startAutoReload();
        }
    }

    componentWillUnmount() {
        this.cancelAutoReload();
    }

    onResendClick(currentRowSelected) {
        const currentEntry = currentRowSelected || this.state.currentRowSelected;
        this.resendNotification(currentEntry.id);
    }

    resendNotification(notificationId, commonConfigId) {
        this.setState({
            message: 'Sending...',
            inProgress: true
        });

        let resendUrl = `/alert/api/audit/${notificationId}/resend`;
        if (commonConfigId) {
            resendUrl = resendUrl + `?commonConfigId=${commonConfigId}`;
        }

        const {csrfToken} = this.props;

        fetch(resendUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        }).then((response) => {
            this.setState({
                message: 'Completed',
                inProgress: false
            });
            if (!response.ok) {
                switch (response.status) {
                    case 401:
                    case 403:
                        this.props.logout();
                        return response.json().then((json) => {
                            this.setState({message: json.message});
                        });
                }
            }
            return response.json().then((json) => {
                setTimeout(function () {
                    this.setState({message: ''});
                }.bind(this), 3000);
                this.setEntriesFromArray(JSON.parse(json.message));
            });
        }).catch(console.error);
    }

    onStatusFailureClick(currentRowSelected) {
        this.setState({
            currentRowSelected
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
            }
        );

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
        let hasPolicyViolation = false;
        let hasPolicyViolationCleared = false;
        let hasPolicyViolationOverride = false;
        let hasHighVulnerability = false;
        let hasMediumVulnerability = false;
        let hasLowVulnerability = false;
        let hasVulnerability = false;


        if (cell === 'RULE_VIOLATION') {
            hasPolicyViolation = true;
        } else if (cell === 'RULE_VIOLATION_CLEARED') {
            hasPolicyViolationCleared = true;
        } else if (cell === 'POLICY_OVERRIDE') {
            hasPolicyViolationOverride = true;
        } else if (cell === 'VULNERABILITY') {
            hasVulnerability = true;
        }


        return (<NotificationTypeLegend
            hasPolicyViolation={hasPolicyViolation}
            hasPolicyViolationCleared={hasPolicyViolationCleared}
            hasPolicyViolationOverride={hasPolicyViolationOverride}
            hasHighVulnerability={hasHighVulnerability}
            hasMediumVulnerability={hasMediumVulnerability}
            hasLowVulnerability={hasLowVulnerability}
            hasVulnerability={hasVulnerability}
        />);
    }

    trClassFormat(row, rowIndex) {
        // color the row correctly, since Striped does not work with expandable rows
        return rowIndex % 2 === 0 ? 'tableEvenRow' : 'tableRow';
    }

    cancelAutoReload() {
        clearTimeout(this.timeout);
    }

    startAutoReload() {
        // run the reload now and then every 10 seconds
        this.cancelAutoReload();
        this.timeout = setTimeout(() => this.reloadAuditEntries(), 10000);
    }

    refreshAuditEntries() {
        this.reloadAuditEntries(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm, this.state.sortField, this.state.sortOrder, this.state.onlyShowSentNotifications);
    }


    reloadAuditEntries(currentPage, sizePerPage, searchTerm, sortField, sortOrder, onlyShowSentNotifications) {
        this.setState({
            message: 'Loading...',
            inProgress: true
        });
        var page = 1;
        if (currentPage) {
            page = currentPage;
        } else if (this.state.currentPage) {
            page = this.state.currentPage;
        }

        var size = 10;
        if (sizePerPage) {
            size = sizePerPage;
        } else if (this.state.currentPageSize) {
            size = this.state.currentPageSize;
        }

        var term = '';
        if (null != searchTerm || undefined != searchTerm) {
            term = searchTerm;
        } else if (this.state.searchTerm) {
            term = this.state.searchTerm;
        }

        var sortingField = '';
        if (null != sortField || undefined != sortField) {
            sortingField = sortField;
        } else if (this.state.sortField) {
            sortingField = this.state.sortField;
        }
        var sortingOrder = '';
        if (null != sortOrder || undefined != sortOrder) {
            sortingOrder = sortOrder;
        } else if (this.state.sortOrder) {
            sortingOrder = this.state.sortOrder;
        }

        var sentNotificationsOnly = false;
        if (null != onlyShowSentNotifications || undefined != onlyShowSentNotifications) {
            sentNotificationsOnly = onlyShowSentNotifications;
        } else if (this.state.onlyShowSentNotifications) {
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
        if (row.content) {
            return (<RefreshTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText="Re-send"/>);
        } else {
            return (<div className="editJobButtonDisabled"><span className="fa fa-refresh"/></div>);
        }
    }

    createCustomButtonGroup(buttons) {
        return (
            <ButtonGroup>
                {!this.props.autoRefresh && <div className="btn btn-info react-bs-table-add-btn tableButton" onClick={this.refreshAuditEntries}>
                    <span className="fa fa-refresh fa-fw" aria-hidden="true"/> Refresh
                </div>}
            </ButtonGroup>
        );
    }

    onOnlyShowSentNotificationsChange({target}) {
        const value = target.checked;
        this.setState({
            onlyShowSentNotifications: value
        });
        this.reloadAuditEntries(null, null, null, null, null, value);
    }

    onSizePerPageListChange(sizePerPage) {
        this.setState({currentPage: 1, currentPageSize: sizePerPage});

        this.reloadAuditEntries(1, sizePerPage);
    }

    onPageChange(page, sizePerPage) {
        this.setState({currentPage: page});
        this.reloadAuditEntries(page, sizePerPage);
    }

    onSearchChange(searchText, colInfos, multiColumnSearch) {
        this.setState({
            currentPage: 1,
            searchTerm: searchText
        });
        this.reloadAuditEntries(null, null, searchText)
    }

    providerColumnDataFormat(cell) {
        const defaultValue = <div className="inline" aria-hidden="true">{cell}</div>;
        if (this.props.descriptors) {
            const descriptorList = this.props.descriptors.items['PROVIDER_CONFIG'];
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === cell)
                if (filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix='audit-provider-icon' descriptor={foundDescriptor}/>);
                }
            }
        }
        return defaultValue;
    }

    onSortChange(sortName, sortOrder) {
        this.setState({sortField: sortName, sortOrder: sortOrder});
        this.reloadAuditEntries(null, null, null, sortName, sortOrder)
    }

    onRowClick(row) {
        this.setState({currentRowSelected: row, showDetailModal: true});
    }

    handleCloseDetails() {
        this.setState({showDetailModal: false});
    }

    render() {
        const auditTableOptions = {
            defaultSortName: 'lastSent',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            sizePerPage: this.state.currentPageSize,
            page: this.state.currentPage,
            // We need all of these onChange methods because the table is using the remote option
            onPageChange: this.onPageChange,
            onSizePerPageList: this.onSizePerPageListChange,
            onSearchChange: this.onSearchChange,
            onSortChange: this.onSortChange,
            onRowClick: this.onRowClick
        };

        const auditFetchInfo = {
            dataTotalSize: this.props.totalDataCount * this.state.currentPageSize
        }

        return (
            <div>
                <h1>
                    <span className="fa fa-history"/>
                    Audit
                    <small className="pull-right">
                        <AutoRefresh startAutoReload={this.startAutoReload} cancelAutoReload={this.cancelAutoReload}/>
                    </small>
                    <small className="pull-right">
                        <CheckboxInput
                            id="showSentNotificationsID"
                            label="Only show sent notifications"
                            name="onlyShowSentNotifications"
                            value={this.state.onlyShowSentNotifications}
                            onChange={this.onOnlyShowSentNotificationsChange}
                        />
                    </small>
                </h1>
                <div>
                    <AuditDetails handleClose={this.handleCloseDetails} show={this.state.showDetailModal} currentEntry={this.state.currentRowSelected} resendNotification={this.resendNotification}
                                  providerNameFormat={this.providerColumnDataFormat} notificationTypeFormat={this.notificationTypeDataFormat} statusFormat={this.statusColumnDataFormat}/>
                    <BootstrapTable
                        version="4"
                        trClassName={this.trClassFormat}
                        condensed
                        data={this.state.entries}
                        containerClass="table"
                        fetchInfo={auditFetchInfo}
                        options={auditTableOptions}
                        headerContainerClass="scrollable"
                        bodyContainerClass="auditTableScrollableBody"
                        remote
                        pagination
                        search
                    >
                        <TableHeaderColumn dataField="provider" dataSort columnClassName="tableCell" dataFormat={this.providerColumnDataFormat}>Provider</TableHeaderColumn>
                        <TableHeaderColumn dataField="notificationType" dataSort columnClassName="tableCell" dataFormat={this.notificationTypeDataFormat}>Notification Types</TableHeaderColumn>
                        <TableHeaderColumn dataField="createdAt" dataSort columnTitle columnClassName="tableCell">Time Retrieved</TableHeaderColumn>
                        <TableHeaderColumn dataField="lastSent" dataSort columnTitle columnClassName="tableCell">Last Sent</TableHeaderColumn>
                        <TableHeaderColumn dataField="overallStatus" dataSort columnClassName="tableCell" dataFormat={this.statusColumnDataFormat}>Status</TableHeaderColumn>
                        <TableHeaderColumn width="48" columnClassName="tableCell" dataFormat={this.resendButton}></TableHeaderColumn>
                        <TableHeaderColumn dataField="id" isKey hidden>Notification Id</TableHeaderColumn>
                    </BootstrapTable>

                    {this.state.inProgress && <div className="progressIcon">
                        <span className="fa fa-spinner fa-pulse fa-fw" aria-hidden="true"/>
                    </div>}

                    <p name="message">{this.state.message}</p>
                </div>
            </div>
        );
    }
}

Index.defaultProps = {
    csrfToken: null,
    descriptors: {},
    items: []
};

Index.propTypes = {
    autoRefresh: PropTypes.bool,
    csrfToken: PropTypes.string,
    fetching: PropTypes.bool,
    items: PropTypes.arrayOf(PropTypes.object),
    totalDataCount: PropTypes.number,
    getAuditData: PropTypes.func.isRequired,
    descriptors: PropTypes.object
};

const mapStateToProps = state => ({
    totalDataCount: state.audit.totalDataCount,
    items: state.audit.items,
    csrfToken: state.session.csrfToken,
    fetching: state.audit.fetching,
    autoRefresh: state.refresh.autoRefresh,
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    getAuditData: (pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) => dispatch(getAuditData(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications)),
    logout: () => dispatch(logout())
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
