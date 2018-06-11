import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import Select from 'react-select-2';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';

import { jobTypes } from '../../../util/distribution-data';

function renderOption(option) {
    let fontAwesomeIcon;
    if (option.value === 'email_group_channel') {
        fontAwesomeIcon = 'fa fa-envelope fa-fw';
    } else if (option.value === 'hipchat_channel') {
        fontAwesomeIcon = 'fa fa-comments  fa-fw';
    } else if (option.value === 'slack_channel') {
        fontAwesomeIcon = 'fa fa-slack  fa-fw';
    }
    return (
        <div>
            <span key="icon" className={fontAwesomeIcon} aria-hidden="true" />
            <span key="name">{option.label}</span>
        </div>
    );
}

class JobAddModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            show: true,
            values: []
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleTypeChanged = this.handleTypeChanged.bind(this);
        this.getCurrentJobConfig = this.getCurrentJobConfig.bind(this);
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    getCurrentJobConfig() {
        switch (this.state.values.typeValue) {
            case 'email_group_channel':
                return (<GroupEmailJobConfiguration
                    csrfToken={this.props.csrfToken}
                    includeAllProjects={this.props.includeAllProjects}
                    waitingForGroups={this.props.waitingForGroups}
                    groups={this.props.groups}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                    groupError={this.props.groupError}
                    projectTableMessage={this.props.projectTableMessage}
                    updateJobsTable={this.props.updateJobsTable}
                />);
            case 'hipchat_channel':
                return (<HipChatJobConfiguration
                    csrfToken={this.props.csrfToken}
                    includeAllProjects={this.props.includeAllProjects}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                    projectTableMessage={this.props.projectTableMessage}
                    updateJobsTable={this.props.updateJobsTable}
                />);
            case 'slack_channel':
                return (<SlackJobConfiguration
                    csrfToken={this.props.csrfToken}
                    includeAllProjects={this.props.includeAllProjects}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                    projectTableMessage={this.props.projectTableMessage}
                    updateJobsTable={this.props.updateJobsTable}
                />);
            default:
                return null;
        }
    }

    handleSaveBtnClick(values) {
        const { onModalClose } = this.props;
        // You should call onSave function and give the new row
        //  onSave(values);
        onModalClose();
    }


    handleChange({ target }) {
        const { name, type, checked } = target;
        const value = type === 'checkbox' ? checked : target.value;

        const { values } = this.state;
        values[name] = value;
        this.setState({
            values
        });
    }

    handleTypeChanged(option) {
        const { values } = this.state;
        if (option) {
            values.typeValue = option.value;
            this.setState({
                values
            });
        }
    }


    handleClose() {
        this.setState({ show: false });
        this.props.onModalClose();
    }

    render() {
        return (
            <Modal show={this.state.show} onHide={this.handleClose}>

                <Modal.Header closeButton>
                    <Modal.Title>New Distribution Job</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <form className="form-horizontal">
                        <div className="form-group">
                            <label className="col-sm-3 control-label">Type</label>
                            <div className="col-sm-8">
                                <Select
                                    id="job-jobType"
                                    className="typeAheadField"
                                    onChange={this.handleTypeChanged}
                                    clearable={false}
                                    options={jobTypes}
                                    optionRenderer={renderOption}
                                    placeholder="Choose the Job Type"
                                    value={this.state.values.typeValue}
                                    valueRenderer={renderOption}
                                />
                            </div>
                        </div>
                    </form>
                    { this.getCurrentJobConfig() }
                </Modal.Body>

            </Modal>
        );
    }
}

JobAddModal.propTypes = {
    onModalClose: PropTypes.func.isRequired
};

export default JobAddModal;
