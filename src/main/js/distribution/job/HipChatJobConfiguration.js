import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TextInput from 'field/input/TextInput';
import CheckboxInput from 'field/input/CheckboxInput';
import { getDistributionJob } from 'store/actions/distributionConfigs';

import BaseJobConfiguration from 'distribution/job/BaseJobConfiguration';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import SelectInput from 'field/input/SelectInput';

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
        this.getConfiguration = this.getConfiguration.bind(this);
        this.createSingleSelectHandler = this.createSingleSelectHandler.bind(this);
        this.state = {
            currentConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_HIPCHAT)
        };
        this.loading = false;
    }

    componentDidMount() {
        const { jobId } = this.props;
        if (jobId) {
            this.props.getDistributionJob(jobId);
            this.loading = true;
        }
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (this.loading) {
                this.loading = false;
                const jobConfig = nextProps.job;
                if (jobConfig && jobConfig.fieldModels) {
                    const channelModel = jobConfig.fieldModels.find(model => model.descriptorName.startsWith('channel_'));
                    this.setState({
                        jobConfig,
                        currentConfig: channelModel,
                        colorOptions: nextProps.colorOptions
                    });
                }
            }
        }
    }

    getConfiguration() {
        return this.state.currentConfig;
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);
        this.setState({
            currentConfig: newState
        });
    }

    createSingleSelectHandler(fieldKey) {
        return (selectedValue) => {
            if (selectedValue) {
                const selected = selectedValue.value;
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, fieldKey, selected);
                this.setState({
                    currentConfig: newState
                });
            } else {
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, fieldKey, null);
                this.setState({
                    currentConfig: newState
                });
            }
        };
    }


    render() {
        const { colorOptions } = this.props;
        const fieldModel = this.state.currentConfig;
        let selectedColorOption = null;
        if (colorOptions) {
            selectedColorOption = colorOptions.find(option => option.value === FieldModelUtilities.getFieldModelSingleValue(this.state.currentConfig, KEY_COLOR));
        }
        const content = (
            <div>
                <TextInput
                    id={KEY_ROOM_ID}
                    label="Room Id"
                    description="The API ID of the room to receive Alerts."
                    name={KEY_ROOM_ID}
                    value={FieldModelUtilities.getFieldModelSingleValueOrDefault(fieldModel, KEY_ROOM_ID, '')}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_ROOM_ID)}
                    errorValue={this.props.fieldErrors[KEY_ROOM_ID]}
                />
                <CheckboxInput
                    id={KEY_NOTIFY}
                    label="Notify"
                    description="If true, this will add to the count of new messages in the HipChat room."
                    name={KEY_NOTIFY}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, KEY_NOTIFY)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_NOTIFY)}
                    errorValue={this.props.fieldErrors[KEY_NOTIFY]}
                />
                <SelectInput
                    label="Color"
                    onChange={this.createSingleSelectHandler(KEY_COLOR)}
                    id={KEY_COLOR}
                    className="typeAheadField"
                    labelSpacingClass="col-sm-3"
                    selectSpacingClass="col-sm-8"
                    description="The text color to display the Alert messages in."
                    options={colorOptions}
                    isSearchable
                    placeholder="Choose the message color"
                    value={selectedColorOption}
                />
            </div>
        );
        return (<BaseJobConfiguration
            alertChannelName={this.props.alertChannelName}
            job={this.state.jobConfig}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}
        />);
    }
}

HipChatJobConfiguration.propTypes = {
    getDistributionJob: PropTypes.func.isRequired,
    job: PropTypes.object,
    jobId: PropTypes.string,
    colorOptions: PropTypes.arrayOf(PropTypes.object),
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

HipChatJobConfiguration.defaultProps = {
    job: null,
    jobId: null,
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
    job: state.distributionConfigs.job,
    fieldErrors: state.distributionConfigs.error,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress
});

const mapDispatchToProps = dispatch => ({
    getDistributionJob: id => dispatch(getDistributionJob(id))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatJobConfiguration);
