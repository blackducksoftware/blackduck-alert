import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'component/common/ConfigButtons';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { deleteDistributionJob } from 'store/actions/distributions';

class JobDeleteModal extends Component {
    constructor(props) {
        super(props);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);

    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.deleteSuccess !== prevProps.deleteSuccess && this.props.deleteSuccess === true) {
            this.props.onModalClose();
        }
    }

    onSubmit(event) {
        event.preventDefault();
        this.props.jobs.forEach((job) => {
            this.props.deleteDistributionJob(job);
        });
        this.props.onModalSubmit();
    }

    handleClose() {
        this.props.onModalClose();
    }

    render() {
        const tableData = this.props.createTableData(this.props.jobs);
        const jobTableOptions = {
            noDataText: 'No jobs configured'
        };
        return (
            <Modal size="lg" show={this.props.show} onHide={this.handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>Are you sure you want to delete these jobs?</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <form id="jobDeleteTable" className="form-horizontal" onSubmit={this.onSubmit}>
                        <div className="form-group">
                            <BootstrapTable
                                version="4"
                                hover
                                condensed
                                data={tableData}
                                options={jobTableOptions}
                                containerClass="table"
                                trClassName="tableRow"
                                headerContainerClass="scrollable"
                                bodyContainerClass="tableScrollableBody"
                            >
                                <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                                <TableHeaderColumn dataField="distributionConfigId" hidden>Distribution
                                    Id</TableHeaderColumn>
                                <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution
                                    Job</TableHeaderColumn>
                                <TableHeaderColumn dataField="distributionType" dataSort columnClassName="tableCell"
                                                   dataFormat={this.props.typeColumnDataFormat}>Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="providerName" dataSort columnClassName="tableCell"
                                                   dataFormat={this.props.providerColumnDataFormat}>Provider</TableHeaderColumn>
                                <TableHeaderColumn dataField="frequency" dataSort columnClassName="tableCell"
                                                   dataFormat={this.props.frequencyColumnDataFormat}>Frequency
                                    Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="lastRan" dataSort columnTitle columnClassName="tableCell">Last
                                    Run</TableHeaderColumn>
                                <TableHeaderColumn dataField="status" dataSort columnTitle
                                                   columnClassName={this.props.statusColumnClassNameFormat}>Status</TableHeaderColumn>
                            </BootstrapTable>
                        </div>
                        <p id="jobDeleteMessage" name="jobDeleteMessage">{this.props.jobDeleteMessage}</p>
                        <ConfigButtons performingAction={this.props.inProgress} cancelId="job-delete-cancel"
                                       submitId="job-delete-submit" submitLabel="Confirm" includeSave includeCancel
                                       onCancelClick={this.handleClose} isFixed={false} />
                    </form>
                </Modal.Body>
            </Modal>
        );
    }
}

JobDeleteModal.propTypes = {
    createTableData: PropTypes.func.isRequired,
    jobDeleteMessage: PropTypes.string.isRequired,
    onModalClose: PropTypes.func.isRequired,
    onModalSubmit: PropTypes.func.isRequired,
    deleteDistributionJob: PropTypes.func.isRequired,
    typeColumnDataFormat: PropTypes.func.isRequired,
    providerColumnDataFormat: PropTypes.func.isRequired,
    frequencyColumnDataFormat: PropTypes.func.isRequired,
    statusColumnClassNameFormat: PropTypes.func.isRequired,
    deleteSuccess: PropTypes.bool,
    inProgress: PropTypes.bool,
    jobs: PropTypes.arrayOf(PropTypes.object),
    show: PropTypes.bool
};

JobDeleteModal.defaultProps = {
    jobs: [],
    show: false,
    inProgress: false,
    deleteSuccess: false
};

const mapStateToProps = state => ({
    jobDeleteMessage: state.distributions.jobDeleteMessage,
    deleteSuccess: state.distributions.deleteSuccess,
    inProgress: state.distributions.inProgress
});

const mapDispatchToProps = dispatch => ({
    deleteDistributionJob: job => dispatch(deleteDistributionJob(job))
});

export default connect(mapStateToProps, mapDispatchToProps)(JobDeleteModal);
