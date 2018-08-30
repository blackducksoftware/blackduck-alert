import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {Modal} from 'react-bootstrap';
import Select from 'react-select-2';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';
import DescriptorOption from "../../common/DescriptorOption";
import {resetDistributionDescriptor} from '../../../store/actions/descriptors';

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

    componentDidMount() {
        this.props.resetDistributionDescriptor();
    }

    getCurrentJobConfig() {
        switch (this.state.values.typeValue) {
            case 'channel_email':
                return (<GroupEmailJobConfiguration
                    alertChannelName={this.state.values.typeValue}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                    groupError={this.props.groupError}
                />);
            case 'channel_hipchat':
                return (<HipChatJobConfiguration
                    alertChannelName={this.state.values.typeValue}
                    projects={this.props.projects}
                    handleCancel={this.handleClose}
                    handleSaveBtnClick={this.handleSaveBtnClick}
                />);
            case 'channel_slack':
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
        const {onModalClose} = this.props;
        // You should call onSave function and give the new row
        //  onSave(values);
        onModalClose();
    }


    handleChange({target}) {
        const {name, type, checked} = target;
        const value = type === 'checkbox' ? checked : target.value;

        const {values} = this.state;
        values[name] = value;
        this.setState({
            values
        });
    }

    handleTypeChanged(option) {
        const {values} = this.state;
        if (option) {
            values.typeValue = option.value;
            this.setState({
                values
            });
        }
    }


    handleClose() {
        this.setState({show: false});
        this.props.onModalClose();
    }

    createJobTypeOptions() {
        const channelDescriptors = this.props.descriptors.items['CHANNEL_DISTRIBUTION_CONFIG'];
        if (channelDescriptors) {
            const optionList = channelDescriptors.map(descriptor => {
                return {
                    label: descriptor.label,
                    value: descriptor.descriptorName,
                    icon: descriptor.fontAwesomeIcon
                }
            });
            return optionList;
        } else {
            return [];
        }
    }

    renderOption(option) {
        return (<DescriptorOption icon={option.icon} label={option.label} value={option.value}/>);
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
                                    id="jobAddType"
                                    className="typeAheadField"
                                    onChange={this.handleTypeChanged}
                                    clearable={false}
                                    options={this.createJobTypeOptions()}
                                    optionRenderer={this.renderOption}
                                    placeholder="Choose the Job Type"
                                    value={this.state.values.typeValue}
                                    valueRenderer={this.renderOption}
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
    csrfToken: PropTypes.string,
    descriptors: PropTypes.object,
    projects: PropTypes.arrayOf(PropTypes.object)
};

JobAddModal.defaultProps = {
    csrfToken: null,
    descriptor: {},
    projects: []
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    resetDistributionDescriptor: () => dispatch(resetDistributionDescriptor())
});

export default connect(mapStateToProps, mapDispatchToProps)(JobAddModal);
