import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Select from 'react-select';
import TextInput from 'field/input/TextInput';
import CheckboxInput from 'field/input/CheckboxInput';
import { getDistributionJob } from 'store/actions/distributionConfigs';

import BaseJobConfiguration from 'distribution/job/BaseJobConfiguration';
import * as FielModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const KEY_ROOM_ID = 'channel.hipchat.room.id';
const KEY_NOTIFY = 'channel.hipchat.notify';
const KEY_COLOR = 'channel.hipchat.color';

const fieldNames = [
    KEY_ROOM_ID,
    KEY_NOTIFY,
    KEY_COLOR
];

class HipChatJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);
        this.createSingleSelectHandler = this.createSingleSelectHandler.bind(this);
        this.state = {
            currentConfig: FielModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_HIPCHAT)
        };
        this.loading = false;
    }

    componentDidMount() {
        const { distributionConfigId } = this.props;
        this.props.getDistributionJob(distributionConfigId);
        this.loading = true;
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (this.loading) {
                this.loading = false;
                const jobConfig = nextProps.jobs[nextProps.distributionConfigId];
                if (jobConfig) {
                    this.setState({
                        roomId: jobConfig.roomId,
                        notify: jobConfig.notify,
                        color: jobConfig.color,
                        colorOptions: nextProps.colorOptions
                    });
                }
            }
        }
    }

    getConfiguration() {
        return this.state.currentConfig;
    }

    handleStateValues(name, value) {
        this.setState({
            [name]: value
        });
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const { name } = target;
        this.handleStateValues(name, value);
    }

    createSingleSelectHandler(fieldKey) {
        return (selectedValue) => {
            if (selectedValue) {
                const selected = selectedValue.value;
                const newState = FielModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, fieldKey, selected);
                this.setState({
                    currentConfig: newState
                });
            } else {
                const newState = FielModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, fieldKey, null);
                this.setState({
                    currentConfig: newState
                });
            }
        };
    }


    render() {
        const { colorOptions } = this.state;
        const fieldModel = this.state.currentConfig;
        let selectedColorOption = null;
        if (colorOptions) {
            selectedColorOption = colorOptions.find(option => option.value === this.state.color);
        }
        const content = (
            <div>
                <TextInput
                    id={KEY_ROOM_ID}
                    label="Room Id"
                    name={KEY_ROOM_ID}
                    value={FielModelUtilities.getFieldModelSingleValue(fieldModel, KEY_ROOM_ID)}
                    onChange={this.handleChange}
                    errorName={FielModelUtilities.createFieldModelErrorKey(KEY_ROOM_ID)}
                    errorValue={this.props.fieldErrors[KEY_ROOM_ID]}
                />
                <CheckboxInput
                    id={KEY_NOTIFY}
                    label="Notify"
                    name={KEY_NOTIFY}
                    isChecked={FielModelUtilities.getFieldModelBooleanValue(fieldModel, KEY_NOTIFY)}
                    onChange={this.handleChange}
                    errorName={FielModelUtilities.createFieldModelErrorKey(KEY_NOTIFY)}
                    errorValue={this.props.fieldErrors[KEY_NOTIFY]}
                />
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Color</label>
                    <div className="d-inline-flex p-2 col-sm-9">
                        <Select
                            id={KEY_COLOR}
                            className="typeAheadField"
                            onChange={this.createSingleSelectHandler(KEY_COLOR)}
                            isSearchable
                            options={colorOptions}
                            placeholder="Choose the message color"
                            value={selectedColorOption}
                        />
                    </div>
                </div>
            </div>
        );
        return (<BaseJobConfiguration
            alertChannelName={this.props.alertChannelName}
            distributionConfigId={this.props.distributionConfigId}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}
        />);
    }
}

HipChatJobConfiguration.propTypes = {
    getDistributionJob: PropTypes.func.isRequired,
    jobs: PropTypes.arrayOf(PropTypes.object),
    distributionConfigId: PropTypes.string,
    colorOptions: PropTypes.arrayOf(PropTypes.object),
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

HipChatJobConfiguration.defaultProps = {
    jobs: [],
    distributionConfigId: null,
    colorOptions: [
        { label: 'Yellow', value: 'yellow' },
        { label: 'Green', value: 'green' },
        { label: 'Red', value: 'red' },
        { label: 'Purple', value: 'purple' },
        { label: 'Gray', value: 'gray' },
        { label: 'Random', value: 'random' }
    ],
    fieldErrors: {},
    fetching: false,
    inProgress: false
};

const mapStateToProps = state => ({
    jobs: state.distributionConfigs.jobs,
    fieldErrors: state.distributionConfigs.error,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress
});

const mapDispatchToProps = dispatch => ({
    getDistributionJob: id => dispatch(getDistributionJob(id))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatJobConfiguration);
