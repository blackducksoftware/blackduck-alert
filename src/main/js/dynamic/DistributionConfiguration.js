import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import TextInput from 'field/input/TextInput';
import SelectInput from 'field/input/SelectInput';
import FieldsPanel from 'field/FieldsPanel';
import { saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributionConfigs';
import ProjectConfiguration from 'distribution/ProjectConfiguration';
import ConfigButtons from 'component/common/ConfigButtons';

const KEY_CHANNEL_NAME = 'channel.common.channel.name';
const KEY_PROVIDER_NAME = 'channel.common.provider.name';

class DistributionConfiguration extends Component {
    constructor(props) {
        super(props);

        this.handleChannelChange = this.handleChange.bind(this);
        this.handleProviderChange = this.handleProviderChange.bind(this);
        this.handleChange = this.handleChange.bind(this);

        const { fields, name } = this.props.channel;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const channelFieldModel = FieldModelUtilities.createEmptyFieldModelFromFieldObject(fieldKeys, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, name);
        const providerFieldModel = FieldModelUtilities.createEmptyFieldModel([], DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, null);
        this.state = {
            channelConfig: channelFieldModel,
            providerConfig: providerFieldModel,
            currentChannel: this.props.channel,
            currentKeys: fieldKeys
        };
    }

    handleChannelChange(event) {
        this.handleChange(event, (newState) => {
            this.setState({
                channelConfig: newState
            });
        });
    }

    handleProviderChange(event) {
        this.handleChange(event, (newState) => {
            this.setState({
                providerConfig: newState
            });
        });
    }

    handleChange(event, stateFunction) {
        const { target } = event;
        if (target) {
            const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);

            stateFunction(newState);
        } else {
            const { value, name } = event;
            if (value && name) {
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, name, value);
                stateFunction(newState);
            }
        }
    }

    handleTestSubmit(event) {
        if (event) {
            event.preventDefault();
        }

        const jsonBody = this.buildJsonBody();
        this.props.testDistributionJob(jsonBody);
    }

    handleSubmit(event) {
        if (event) {
            event.preventDefault();
        }
        const jsonBody = this.buildJsonBody();
        if (this.state.jobId) {
            this.props.updateDistributionJob(jsonBody);
        } else {
            this.props.saveDistributionJob(jsonBody);
        }
    }

    buildJsonBody() {
        const channelName = this.state.commonConfig.descriptorName;
        const providerName = FieldModelUtilities.getFieldModelSingleValue(this.state.channelConfig, KEY_PROVIDER_NAME);
        const currentProvider = this.props.descriptors.find(descriptor => descriptor.name === providerName);
        const emptyProviderModel = FieldModelUtilities.createEmptyFieldModel(currentProvider.fields, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, providerName);
        const updatedProviderFieldModel = FieldModelUtilities.combineFieldModels(emptyProviderModel, this.state.providerConfig);
        const commonFieldModel = FieldModelUtilities.updateFieldModelSingleValue(this.state.commonConfig, KEY_CHANNEL_NAME, channelName);
        const updatedChannelFieldModel = FieldModelUtilities.combineFieldModels(commonFieldModel, this.state.channelConfig);
        const configuration = Object.assign({}, {
            jobId: this.state.jobId,
            fieldModels: [
                updatedChannelFieldModel,
                updatedProviderFieldModel
            ]
        });
        return configuration;
    }

    renderProviderForm(selectedProvider) {
        const { providerConfig } = this.state;
        const currentProvider = this.props.descriptors.find(descriptor => descriptor.name === selectedProvider);
        return (
            <div>
                <FieldsPanel descriptorFields={currentProvider.fields} currentConfig={providerConfig} fieldErrors={this.props.fieldErrors} handleChange={this.handleProviderChange} />
                <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel} isFixed={false} />
                <p name="configurationMessage">{this.state.configurationMessage}</p>
            </div>
        );
    }

    render() {
        const fieldModel = this.state.channelConfig;
        const selectedProviderOption = FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROVIDER_NAME);
        const { fields } = this.state.currentChannel;
        return (
            <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate>
                <FieldsPanel descriptorFields={fields} currentConfig={fieldModel} fieldErrors={this.props.fieldErrors} handleChange={this.hanhandleChannelChangedleChange} />
                {selectedProviderOption && this.renderProviderForm(selectedProviderOption.value)}
            </form>
        );
    }
}

DistributionConfiguration.propTypes = {
    channel: PropTypes.object.isRequired,
    channelConfig: PropTypes.object,
    fieldErrors: PropTypes.object,
    configurationMessage: PropTypes.string,
    updateDistributionJob: PropTypes.func.isRequired,
    testDistributionJob: PropTypes.func.isRequired,
    saveDistributionJob: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired
};

DistributionConfiguration.defaultProps = {};

const mapDispatchToProps = dispatch => ({
    saveDistributionJob: config => dispatch(saveDistributionJob(config)),
    updateDistributionJob: config => dispatch(updateDistributionJob(config)),
    testDistributionJob: config => dispatch(testDistributionJob(config))
});

const mapStateToProps = state => ({
    fieldErrors: state.distributionConfigs.error,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress,
    descriptors: state.descriptors.items,
    saving: state.distributionConfigs.saving,
    success: state.distributionConfigs.success,
    testingConfig: state.distributionConfigs.testingConfig,
    configurationMessage: state.distributionConfigs.configurationMessage
});

export default connect(mapStateToProps, mapDispatchToProps)(DistributionConfiguration);
