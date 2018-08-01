import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {BootstrapTable, ButtonGroup, TableHeaderColumn} from 'react-bootstrap-table';
import {getAuditData} from '../../../store/actions/audit';
import AutoRefresh from '../../common/AutoRefresh';
import RefreshTableCellFormatter from '../../common/RefreshTableCellFormatter';
import AuditDetails from './Details';
import NotificationTypeLegend from '../../common/NotificationTypeLegend';

import '../../../../css/audit.scss';
import {logout} from '../../../store/actions/session';

class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            message: '',
            entries: [],
            currentPage: 1,
            currentPageSize: 10,
            searchTerm: ''
        };
        // this.addDefaultEntries = this.addDefaultEntries.bind(this);
        this.cancelAutoReload = this.cancelAutoReload.bind(this);
        this.startAutoReload = this.startAutoReload.bind(this);
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
        this.onSearchChange = this.onSearchChange.bind(this);
    }

    componentDidMount() {
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.items !== this.props.items) {
            this.setState({message: '', inProgress: false});
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

        const resendUrl = `/alert/api/audit/${currentEntry.id}/resend`;
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
                jobName: entry.name,
                eventType: entry.eventType,
                timeCreated: entry.timeCreated,
                timeLastSent: entry.timeLastSent,
                status: entry.status,
                errorMessage: entry.errorMessage,
                errorStackTrace: entry.errorStackTrace
            };
            const {notification} = entry;
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
        return <AuditDetails currentEntry={row}/>;
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
        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm);
    }

    cancelRowSelect() {
        this.setState({
            currentRowSelected: null
        });
    }

    resendButton(cell, row) {
        return <RefreshTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText="Re-send"/>;
    }

    createCustomButtonGroup(buttons) {
        return (
            <ButtonGroup>
                {!this.props.autoRefresh && <div className="btn btn-info react-bs-table-add-btn tableButton" onClick={this.reloadAuditEntries}>
                    <span className="fa fa-refresh fa-fw" aria-hidden="true"/> Refresh
                </div>}
            </ButtonGroup>
        );
    }

    onSizePerPageListChange(sizePerPage) {
        this.setState({currentPage: 1, currentPageSize: sizePerPage});

        this.props.getAuditData(this.state.currentPage, this.state.currentPageSize, this.state.searchTerm);
    }

    onPageChange(page, sizePerPage) {
        this.setState({currentPage: page});
        this.props.getAuditData(page, this.state.currentPageSize, this.state.searchTerm);
    }

    onSearchChange(searchText, colInfos, multiColumnSearch) {
        this.setState({
            searchTerm: searchText
        });
        this.reloadAuditEntries()
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
            onSizePerPageList: this.onSizePerPageListChange,
            onSearchChange: this.onSearchChange
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
                </h1>
                <div>
                    <BootstrapTable
                        trClassName={this.trClassFormat}
                        condensed
                        data={this.state.entries}
                        expandableRow={() => true}
                        expandComponent={this.expandComponent}
                        containerClass="table"
                        fetchInfo={auditFetchInfo}
                        options={auditTableOptions}
                        headerContainerClass="scrollable"
                        bodyContainerClass="tableScrollableBody"
                        remote
                        pagination
                        search
                    >
                        <TableHeaderColumn dataField="jobName" dataSort columnTitle columnClassName="tableCell">Job Name</TableHeaderColumn>
                        <TableHeaderColumn dataField="notificationProjectName" dataSort columnTitle columnClassName="tableCell">Project Name</TableHeaderColumn>
                        <TableHeaderColumn dataField="notificationTypes" dataSort width="145" columnClassName="tableCell" dataFormat={this.notificationTypeDataFormat}>Notification Types</TableHeaderColumn>
                        <TableHeaderColumn dataField="timeCreated" dataSort width="160" columnTitle columnClassName="tableCell">Time Created</TableHeaderColumn>
                        <TableHeaderColumn dataField="timeLastSent" dataSort width="160" columnTitle columnClassName="tableCell">Time Last Sent</TableHeaderColumn>
                        <TableHeaderColumn dataField="status" width="75" dataSort columnClassName="tableCell" dataFormat={this.statusColumnDataFormat}>Status</TableHeaderColumn>
                        <TableHeaderColumn dataField="" width="48" expandable={false} columnClassName="tableCell" dataFormat={this.resendButton}/>
                        <TableHeaderColumn dataField="id" isKey hidden>Audit Id</TableHeaderColumn>
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
    items: []
};

Index.propTypes = {
    autoRefresh: PropTypes.bool,
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
    fetching: state.audit.fetching,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = dispatch => ({
    getAuditData: (pageNumber, pageSize, searchTerm) => dispatch(getAuditData(pageNumber, pageSize, searchTerm)),
    logout: () => dispatch(logout())
});

export default connect(mapStateToProps, mapDispatchToProps)(Index);
