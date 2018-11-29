import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import DescriptorLabel from '../../common/DescriptorLabel';
import TextInput from '../../../field/input/TextInput';
import TextArea from '../../../field/input/TextArea';
import {BootstrapTable, ReactBsTable, TableHeaderColumn} from 'react-bootstrap-table';
import RefreshTableCellFormatter from "../../common/RefreshTableCellFormatter";
import {Modal, Tab, Tabs} from "react-bootstrap";

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
            errorStackTrace = <TextArea inputClass="textArea" label="Stack Trace" readOnly name="errorStackTrace" value={row.errorStackTrace}/>;
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
        const jobs = this.props.currentEntry.jobs;
        return (
            <Modal size="lg" show={this.props.show} onHide={this.props.handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>Notification</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="expandableContainer">
                        <Tabs defaultActiveKey={1} id="uncontrolled-tab-example">
                            <Tab eventKey={1} title="Distribution Jobs">
                                <div className="container-fluid">
                                    <BootstrapTable
                                        version="4"
                                        trClassName={this.trClassFormat}
                                        condensed
                                        data={jobs}
                                        expandableRow={() => true}
                                        expandComponent={this.expandComponent}
                                        containerClass="table"
                                        options={jobTableOptions}
                                        headerContainerClass="scrollable"
                                        bodyContainerClass="auditTableScrollableBody"
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
                            </Tab>
                            <Tab eventKey={2} title="Notification Content">
                                <div className="tableContainer">
                                    <TextArea inputClass="auditContentTextArea" sizeClass='col-sm-12' label="" readOnly name="notificationContent" value={jsonPrettyPrintContent}/>
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
