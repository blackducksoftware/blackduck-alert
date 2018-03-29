import React, { Component } from 'react';
import { Modal } from 'react-bootstrap';
import Select from 'react-select-2';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';

import { jobTypes } from '../../../util/distribution-data';

export default class JobAddModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            show: true,
            values: [],
            errors: []
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleTypeChanged = this.handleTypeChanged.bind(this);
        this.getCurrentJobConfig = this.getCurrentJobConfig.bind(this);
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    handleSaveBtnClick(values) {
        const { columns, onSave, onModalClose } = this.props;
        // You should call onSave function and give the new row
        //  onSave(values);
        onModalClose();
    }


    handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;

        const values = this.state.values;
        values[name] = value;
        this.setState({
            values
        });
    }

    handleTypeChanged(option) {
        const values = this.state.values;
        if (option) {
            values.typeValue = option.value;
            this.setState({
                values
            });
        }
    }

    getCurrentJobConfig() {
        switch (this.state.values.typeValue) {
            case 'email_group_channel':
                return <GroupEmailJobConfiguration includeAllProjects={this.props.includeAllProjects} waitingForGroups={this.props.waitingForGroups} groups={this.props.groups} projects={this.props.projects} handleCancel={this.handleClose} handleSaveBtnClick={this.handleSaveBtnClick} groupError={this.props.groupError} projectTableMessage={this.props.projectTableMessage} updateJobsTable={this.props.updateJobsTable} />;
            case 'hipchat_channel':
                return <HipChatJobConfiguration includeAllProjects={this.props.includeAllProjects} projects={this.props.projects} handleCancel={this.handleClose} handleSaveBtnClick={this.handleSaveBtnClick} projectTableMessage={this.props.projectTableMessage} updateJobsTable={this.props.updateJobsTable} />;
            case 'slack_channel':
                return <SlackJobConfiguration includeAllProjects={this.props.includeAllProjects} projects={this.props.projects} handleCancel={this.handleClose} handleSaveBtnClick={this.handleSaveBtnClick} projectTableMessage={this.props.projectTableMessage} updateJobsTable={this.props.updateJobsTable} />;
            default:
                return null;
        }
    }

    renderOption(option) {
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
                                    className="typeAheadField"
                                    onChange={this.handleTypeChanged}
                                    clearable={false}
                                    options={jobTypes}
                                    optionRenderer={this.renderOption}
                                    placeholder="Choose the Job Type"
                                    value={this.state.values.typeValue}
                                    valueRenderer={this.renderOption}
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
