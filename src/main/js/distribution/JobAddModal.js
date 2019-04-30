import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';

import EmailJobConfiguration from 'distribution/job/EmailJobConfiguration';
import HipChatJobConfiguration from 'distribution/job/HipChatJobConfiguration';
import SlackJobConfiguration from 'distribution/job/SlackJobConfiguration';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import SelectInput from 'field/input/SelectInput';
import DistributionConfiguration from 'dynamic/DistributionConfiguration';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as FieldMapping from "../util/fieldMapping";

const KEY_CHANNEL_NAME = 'channel.common.channel.name';

class JobAddModal extends Component {
    constructor(props) {
        super(props);
        const defaultDescriptor = this.props.descriptors.find(descriptor => descriptor.type === DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const { fields, context, name } = defaultDescriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const emptyFieldModel = FieldModelUtilities.createEmptyFieldModel(fieldKeys, context, name);
        const updatedFieldModel = FieldModelUtilities.updateFieldModelSingleValue(emptyFieldModel, KEY_CHANNEL_NAME, name);
        this.state = {
            show: true,
            channelDescriptor: defaultDescriptor,
            channelConfig: updatedFieldModel
        };
        this.handleTypeChanged = this.handleTypeChanged.bind(this);
        this.handleTypeChanged = this.handleTypeChanged.bind(this);
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.createJobTypeOptions = this.createJobTypeOptions.bind(this);
    }

    handleSaveBtnClick(values) {
        const { onModalClose } = this.props;
        // You should call onSave function and give the new row
        //  onSave(values);
        onModalClose();
    }

    handleTypeChanged(option) {
        if (option) {
            const { value } = option;
            const foundDescriptor = this.props.descriptors.find(descriptor => descriptor.name === value);
            this.setState({
                channelDescriptor: foundDescriptor
            });
        }
    }


    handleClose() {
        this.setState({ show: false });
        this.props.onModalClose();
    }

    createJobTypeOptions() {
        const channelDescriptors = DescriptorUtilities.findDescriptorByTypeAndContext(this.props.descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        if (channelDescriptors) {
            const optionList = channelDescriptors.map(descriptor => ({
                label: descriptor.label,
                value: descriptor.name,
                icon: descriptor.fontAwesomeIcon
            }));
            return optionList;
        }
        return [];
    }

    render() {
        const jobTypeOptions = this.createJobTypeOptions();

        let jobTypeValue = null;
        if (jobTypeOptions) {
            jobTypeValue = jobTypeOptions.find(option => option.value === this.state.channelDescriptor.name);
        }

        // event propagation outside of the modal seems to cause a problem not being able to use the tab key to cycle through the form fields.
        // https://github.com/react-bootstrap/react-bootstrap/issues/3105
        // https://github.com/facebook/react/issues/11387
        return (
            <div
                onKeyDown={e => e.stopPropagation()}
                onClick={e => e.stopPropagation()}
                onFocus={e => e.stopPropagation()}
                onMouseOver={e => e.stopPropagation()}
            >
                <Modal size="lg" show={this.state.show} onHide={this.handleClose}>
                    <Modal.Header closeButton>
                        <Modal.Title>New Distribution Job</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <form className="form-horizontal">
                            <SelectInput
                                label="Type"
                                onChange={this.handleTypeChanged}
                                id="jobAddType"
                                inputClass="typeAheadField"
                                labelClass="col-sm-3"
                                selectSpacingClass="col-sm-8"
                                options={jobTypeOptions}
                                isSearchable={false}
                                placeholder="Choose the Job Type"
                                value={jobTypeValue}
                            />
                        </form>
                        <DistributionConfiguration channel={this.state.channelDescriptor} handleCancel={this.handleClose} channelConfig={this.state.channelConfig} />
                    </Modal.Body>

                </Modal>
            </div>
        );
    }
}

JobAddModal.propTypes = {
    onModalClose: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object),
    projects: PropTypes.arrayOf(PropTypes.object)
};

JobAddModal.defaultProps = {
    descriptors: [],
    projects: []
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(JobAddModal);
