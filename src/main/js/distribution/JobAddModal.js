import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import Select, { components } from 'react-select';

import EmailJobConfiguration from 'distribution/job/EmailJobConfiguration';
import HipChatJobConfiguration from 'distribution/job/HipChatJobConfiguration';
import SlackJobConfiguration from 'distribution/job/SlackJobConfiguration';
import DescriptorOption from 'component/common/DescriptorOption';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const { Option, SingleValue } = components;

const CustomJobTypeOptionLabel = props => (
    <Option {...props}>
        <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
    </Option>
);

const CustomJobTypeLabel = props => (
    <SingleValue {...props}>
        <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
    </SingleValue>
);

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
        this.createJobTypeOptions = this.createJobTypeOptions.bind(this);
    }

    getCurrentJobConfig() {
        switch (this.state.values.typeValue) {
            case DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL:
                return (<EmailJobConfiguration
                    alertChannelName={this.state.values.typeValue}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                />);
            case DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_HIPCHAT:
                return (<HipChatJobConfiguration
                    alertChannelName={this.state.values.typeValue}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                />);
            case DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_SLACK:
                return (<SlackJobConfiguration
                    alertChannelName={this.state.values.typeValue}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
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
        return (
            <Modal size="lg" show={this.state.show} onHide={this.handleClose}>

                <Modal.Header closeButton>
                    <Modal.Title>New Distribution Job</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <form className="form-horizontal">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right">Type</label>
                            <div className="d-inline-flex p-2 col-sm-9">
                                <Select
                                    id="jobAddType"
                                    className="typeAheadField"
                                    onChange={this.handleTypeChanged}
                                    isClearable={false}
                                    options={jobTypeOptions}
                                    placeholder="Choose the Job Type"
                                    value={jobTypeOptions.find(option => option.value === this.state.values.typeValue)}
                                    components={{ Option: CustomJobTypeOptionLabel, SingleValue: CustomJobTypeLabel }}
                                />
                            </div>
                        </div>
                    </form>
                    {this.getCurrentJobConfig()}
                </Modal.Body>

            </Modal>
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
