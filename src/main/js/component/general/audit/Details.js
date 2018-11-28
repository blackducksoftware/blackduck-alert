import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import DescriptorLabel from '../../common/DescriptorLabel';
import TextInput from '../../../field/input/TextInput';
import TextArea from '../../../field/input/TextArea';
import {BootstrapTable, ReactBsTable, TableHeaderColumn} from 'react-bootstrap-table';
import RefreshTableCellFormatter from "../../common/RefreshTableCellFormatter";
import {Modal} from "react-bootstrap";

class Details extends Component {
    constructor(props) {
        super(props);

        this.state = {
            message: ''
        };

        this.resendButton = this.resendButton.bind(this);
        this.onResendClick = this.onResendClick.bind(this);
        this.getEventType = this.getEventType.bind(this);
    }

    getEventType(eventType) {
        const defaultValue = <div className="inline">Unknown</div>;
        if (this.props.descriptors) {
            const descriptorList = this.props.descriptors.items['CHANNEL_DISTRIBUTION_CONFIG'];
            if (descriptorList) {
                const filteredList = descriptorList.filter(descriptor => descriptor.descriptorName === eventType)
                if (filteredList && filteredList.length > 0) {
                    const foundDescriptor = filteredList[0];
                    return (<DescriptorLabel keyPrefix='audit-detail-icon' descriptor={foundDescriptor}/>);
                }
            }
        }
        return (defaultValue);
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

    expandComponent(row) {
        let errorMessage = null;
        if (row.errorMessage) {
            errorMessage = <TextInput label="Error" readOnly name="errorMessage" value={row.errorMessage}/>;
        }
        let errorStackTrace = null;
        if (row.errorStackTrace) {
            errorStackTrace = <TextArea inputClass="stackTraceContainer" label="Stack Trace" readOnly name="errorStackTrace" value={row.errorStackTrace}/>;
        }

        return (<div className="inline">{errorMessage}{errorStackTrace}</div>);
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

    resendButton(cell, row) {
        if (row.content) {
            return <RefreshTableCellFormatter handleButtonClicked={this.onResendClick} currentRowSelected={row} buttonText="Re-send"/>;
        } else {
            return <div className="editJobButtonDisabled"><span className="fa fa-refresh"/></div>
        }
    }

    render() {
        const jobTableOptions = {
            defaultSortName: 'timeCreated',
            defaultSortOrder: 'desc',
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No events',
            clearSearch: true,
            expandBy: 'column',
            expandRowBgColor: '#e8e8e8',
        };
        let jsonContent = null;
        if (this.props.currentEntry.content) {
            jsonContent = JSON.parse(this.props.currentEntry.content);
        } else {
            jsonContent = Object.assign({}, {'warning': 'Content in an Unknown Format'});
        }
        const jsonPrettyPrintContent = JSON.stringify(jsonContent, null, 2);
        return (
            <Modal size="lg" show={this.props.show} onHide={this.props.handleClose}>
                <Modal.Header closeButton>
                </Modal.Header>
                <Modal.Body>
                    <div className="expandableContainer">
                        <div className="container-fluid">
                            <BootstrapTable
                                version="4"
                                trClassName={this.trClassFormat}
                                condensed
                                data={this.props.currentEntry.jobs}
                                expandableRow={() => true}
                                expandComponent={this.expandComponent}
                                containerClass="table"
                                options={jobTableOptions}
                                headerContainerClass="scrollable"
                                bodyContainerClass="auditDetailsTableBody"
                                pagination
                                search
                            >
                                <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                                <TableHeaderColumn dataField="eventType" dataSort columnClassName="tableCell" dataFormat={this.getEventType}>Event Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="timeLastSent" dataSort columnTitle columnClassName="tableCell">Time Last Sent</TableHeaderColumn>
                                <TableHeaderColumn dataField="status" dataSort columnClassName="tableCell" dataFormat={this.statusColumnDataFormat}>Status</TableHeaderColumn>
                                <TableHeaderColumn dataField="" width="48" expandable={false} columnClassName="tableCell" dataFormat={this.resendButton}/>
                                <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                            </BootstrapTable>
                        </div>
                        <div className="tableContainer">
                            <TextArea inputClass="stackTraceContainer" sizeClass='col-sm-12' label="Content" readOnly name="notificationContent" value={jsonPrettyPrintContent}/>
                        </div>
                    </div>
                </Modal.Body>

            </Modal>
        );
    }
}

Details.propTypes = {
    descriptors: PropTypes.object
};

Details.defaultProps = {
    descriptors: {}
};

const mapStateToProps = state => ({
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Details);
