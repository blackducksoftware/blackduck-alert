import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { ReactBsTable, BootstrapTable, TableHeaderColumn, ButtonGroup } from 'react-bootstrap-table';
import { getAuditData } from '../../../store/actions/audit';
import AutoRefresh from '../../common/AutoRefresh';
import RefreshTableCellFormatter from '../../common/RefreshTableCellFormatter';
import AuditDetails from './Details';
import NotificationTypeLegend from '../../common/NotificationTypeLegend';

import '../../../../css/audit.scss';

class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            autoRefresh: true,
            message: '',
            entries: [],
            currentPage: 1,
            currentPageSize: 10
        };
        // this.addDefaultEntries = this.addDefaultEntries.bind(this);
        this.cancelAutoReload = this.cancelAutoReload.bind(this);
        this.startAutoReload = this.startAutoReload.bind(this);
        this.handleAutoRefreshChange = this.handleAutoRefreshChange.bind(this);
        this.setEntriesFromArray = this.setEntriesFromArray.bind(this);
        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.onStatusFailureClick = this.onStatusFailureClick.bind(this);
        this.statusColumnDataFormat = this.statusColumnDataFormat.bind(this);
        this.createCustomButtonGroup = this.createCustomButtonGroup.bind(this);
        this.reloadAuditEntries = this.reloadAuditEntries.bind(this);
        this.onSizePerPageListChange = this.onSizePerPageListChange.bind(this);
        this.onPageChange = this.onPageChange.bind(this);
    }

    componentDidMount() {
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.items !== this.props.items) {
            this.setState({ message: '', inProgress: false });
            this.setEntriesFromArray(nextProps.items);
        }

        if (!nextProps.fetching) {
            this.startAutoReload();
        }
    }

    componentWillUnmount() {
        this.cancelAutoReload();
    }

    onResendClick(currentRowSelected) {
        const currentEntry = currentRowSelected || this.state.currentRowSelected;

        this.setState({
            message: 'Sending...',
            inProgress: true
        });

        const resendUrl = `/api/audit/${currentEntry.id}/resend`;
        const { csrfToken } = this.props;

        fetch(resendUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        }).then((response) => {
            this.setState({ inProgress: false });
            if (!response.ok) {
                return response.json().then((json) => {
                    this.setState({ message: json.message });
                });
            }
            return response.json().then((json) => {
                this.setState({ message: '' });
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
                jobName: entry.name,
                eventType: entry.eventType,
                timeCreated: entry.timeCreated,
                timeLastSent: entry.timeLastSent,
                status: entry.status,
                errorMessage: entry.errorMessage,
                errorStackTrace: entry.errorStackTrace
            };
            const { notification } = entry;
            if (notification) {
                result.notificationTypes = notification.notificationTypes;
                result.notificationProjectName = notification.projectName;
                result.notificationProjectVersion = notification.projectVersion;
                result.components = notification.components;
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
        return <div className={statusClass} aria-hidden>{cell}</div>;
    }

    notificationTypeDataFormat(cells) {
        if (cells && cells.length > 0) {
            let hasPolicyViolation = false;
            let hasPolicyViolationCleared = false;
            let hasPolicyViolationOverride = false;
            let hasHighVulnerability = false;
            let hasMediumVulnerability = false;
            let hasLowVulnerability = false;
            let hasVulnerability = false;

            cells.forEach((cell) => {
                if (cell === 'POLICY_VIOLATION') {
                    hasPolicyViolation = true;
                } else if (cell === 'POLICY_VIOLATION_CLEARED') {
                    hasPolicyViolationCleared = true;
                } else if (cell === 'POLICY_VIOLATION_OVERRIDE') {
                    hasPolicyViolationOverride = true;
                } else if (cell === 'HIGH_VULNERABILITY') {
                    hasHighVulnerability = true;
                } else if (cell === 'MEDIUM_VULNERABILITY') {
                    hasMediumVulnerability = true;
                } else if (cell === 'LOW_VULNERABILITY') {
                    hasLowVulnerability = true;
                } else if (cell === 'VULNERABILITY') {
                    hasVulnerability = true;
                }
            });

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
        return null;
    }

    expandComponent(row) {
        return <AuditDetails currentEntry={row} />;
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

    reloadAuditEntries() {
        this.setState({
            message: 'Loading...',
            inProgress: true
        });
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize);
    }

    handleAutoRefreshChange({ target }) {
        const { name, checked } = target;
        if (checked) {
            this.startAutoReload();
        } else {
            this.cancelAutoReload();
        }
        this.setState({
            [name]: checked
        });
    }

    cancelRowSelect() {
        this.setState({
            currentRowSelected: null
        });
    }

    resendButton(cell, row) {
        return <RefreshTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText="Re-send" />;
    }

    createCustomButtonGroup(buttons) {
        return (
            <ButtonGroup>
                {!this.state.autoRefresh && <div className="btn btn-info react-bs-table-add-btn tableButton" onClick={this.reloadAuditEntries}>
                    <span className="fa fa-refresh fa-fw" aria-hidden="true" /> Refresh
                </div>}
            </ButtonGroup>
        );
    }

    onSizePerPageListChange(sizePerPage) {
        this.setState({ currentPage: 1, currentPageSize: sizePerPage});
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize);
    }

    onPageChange(page, sizePerPage) {
        this.setState({currentPage: page});
        this.props.getAuditData(page,this.state.currentPageSize);
    }

    render() {
        const auditTableOptions = {
            defaultSortName: 'timeLastSent',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            expandBy: 'column',
            expandRowBgColor: '#e8e8e8',
            sizePerPage: this.state.currentPageSize,
            page: this.state.currentPage,
            onPageChange: this.onPageChange,
            onSizePerPageList: this.onSizePerPageListChange
        };

        const auditFetchInfo = {
            dataTotalSize: this.props.totalDataCount * this.state.currentPageSize
        }

        return (
            <div>
                <h1>
                    <span className="fa fa-history" />
                    Audit
                    <small className="pull-right">
                        <AutoRefresh autoRefresh={this.state.autoRefresh} handleAutoRefreshChange={this.handleAutoRefreshChange} />
                    </small>
                </h1>
                <div>
                    <BootstrapTable trClassName={this.trClassFormat} condensed data={this.state.entries} expandableRow={() => true} expandComponent={this.expandComponent} containerClass="table" search fetchInfo={auditFetchInfo}  options={auditTableOptions} headerContainerClass="scrollable" bodyContainerClass="tableScrollableBody" remote pagination>
                        <TableHeaderColumn dataField="jobName" dataSort columnTitle columnClassName="tableCell">Job Name</TableHeaderColumn>
                        <TableHeaderColumn dataField="notificationProjectName" dataSort columnTitle columnClassName="tableCell">Project Name</TableHeaderColumn>
                        <TableHeaderColumn dataField="notificationTypes" width="145" dataSort columnClassName="tableCell" dataFormat={this.notificationTypeDataFormat}>Notification Types</TableHeaderColumn>
                        <TableHeaderColumn dataField="timeCreated" width="160" dataSort columnTitle columnClassName="tableCell">Time Created</TableHeaderColumn>
                        <TableHeaderColumn dataField="timeLastSent" width="160" dataSort columnTitle columnClassName="tableCell">Time Last Sent</TableHeaderColumn>
                        <TableHeaderColumn dataField="status" width="75" dataSort columnClassName="tableCell" dataFormat={this.statusColumnDataFormat}>Status</TableHeaderColumn>
                        <TableHeaderColumn dataField="" width="48" expandable={false} columnClassName="tableCell" dataFormat={this.resendButton} />
                        <TableHeaderColumn dataField="id" isKey hidden>Audit Id</TableHeaderColumn>
                    </BootstrapTable>

                    { this.state.inProgress && <div className="progressIcon">
                        <span className="fa fa-spinner fa-pulse fa-fw" aria-hidden="true" />
                    </div>}

                    <p name="message">{this.state.message}</p>
                </div>
            </div>
        );
    }
}

Index.defaultProps = {
    csrfToken: null,
    items: []
};

Index.propTypes = {
    csrfToken: PropTypes.string,
    fetching: PropTypes.bool,
    items: PropTypes.arrayOf(PropTypes.object),
    totalDataCount: PropTypes.number,
    getAuditData: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    totalDataCount: state.audit.totalDataCount,
    items: state.audit.items,
    csrfToken: state.session.csrfToken,
    fetching: state.audit.fetching
});

const mapDispatchToProps = dispatch => ({
    getAuditData: (pageNumber, pageSize) => dispatch(getAuditData(pageNumber, pageSize))
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
