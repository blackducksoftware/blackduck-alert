import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'distribution/job/BaseJobConfiguration';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';

class JobDeleteModal extends Component {
    constructor(props) {
        super(props);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }


    onSubmit(event) {
        event.preventDefault();
        this.props.jobs.forEach((job) => {
            this.props.deleteDistributionJob(job);
        });
        this.props.onModalSubmit();
        this.props.onModalClose();
    }


    handleClose() {
        this.props.onModalClose();
    }

    render() {
        const jobTableOptions = {
            noDataText: 'No jobs configured'
        };
        return (
            <Modal size="lg" show={this.props.show} onHide={this.handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>Do you want to delete these jobs?</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <form className="form-horizontal" onSubmit={this.onSubmit}>
                        <div className="form-group">
                            <BootstrapTable
                                version="4"
                                hover
                                condensed
                                data={this.props.jobs}
                                options={jobTableOptions}
                                containerClass="table"
                                trClassName="tableRow"
                                headerContainerClass="scrollable"
                                bodyContainerClass="tableScrollableBody"
                            >
                                <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                                <TableHeaderColumn dataField="distributionConfigId" hidden>Distribution Id</TableHeaderColumn>
                                <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                                <TableHeaderColumn dataField="distributionType" dataSort columnClassName="tableCell" dataFormat={this.props.typeColumnDataFormat}>Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="providerName" dataSort columnClassName="tableCell" dataFormat={this.props.providerColumnDataFormat}>Provider</TableHeaderColumn>
                                <TableHeaderColumn dataField="frequency" dataSort columnClassName="tableCell" dataFormat={this.props.frequencyColumnDataFormat}>Frequency Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="lastRan" dataSort columnTitle columnClassName="tableCell">Last Run</TableHeaderColumn>
                                <TableHeaderColumn dataField="status" dataSort columnTitle columnClassName={this.props.statusColumnClassNameFormat}>Status</TableHeaderColumn>
                            </BootstrapTable>
                        </div>
                        <ConfigButtons cancelId="job-cancel" submitId="job-submit" submitLabel="Confirm" includeSave includeCancel onCancelClick={this.handleClose} />
                    </form>
                </Modal.Body>

            </Modal>
        );
    }
}

JobDeleteModal.propTypes = {
    onModalClose: PropTypes.func.isRequired,
    onModalSubmit: PropTypes.func.isRequired,
    deleteDistributionJob: PropTypes.func.isRequired,
    typeColumnDataFormat: PropTypes.func.isRequired,
    providerColumnDataFormat: PropTypes.func.isRequired,
    frequencyColumnDataFormat: PropTypes.func.isRequired,
    statusColumnClassNameFormat: PropTypes.func.isRequired,
    jobs: PropTypes.arrayOf(PropTypes.object),
    show: PropTypes.bool
};

JobDeleteModal.defaultProps = {
    jobs: [],
    show: false
};

const mapStateToProps = state => ({});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(JobDeleteModal);
