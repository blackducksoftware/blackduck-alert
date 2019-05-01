import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import FieldsPanel from 'field/FieldsPanel';
import { saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributionConfigs';
import ProjectConfiguration from 'distribution/ProjectConfiguration';
import ConfigButtons from 'component/common/ConfigButtons';
import { Modal } from 'react-bootstrap';

const KEY_CHANNEL_NAME = 'channel.common.channel.name';
const KEY_PROVIDER_NAME = 'channel.common.provider.name';
const KEY_FILTER_BY_PROJECT = 'channel.common.filter.by.project';
const KEY_PROJECT_NAME_PATTERN = 'channel.common.project.name.pattern';
const KEY_CONFIGURED_PROJECT = 'channel.common.configured.project';

class DistributionConfiguration extends Component {
    constructor(props) {
        super(props);

        this.buildJsonBody = this.buildJsonBody.bind(this);
        this.handleChannelChange = this.handleChannelChange.bind(this);
        this.handleProviderChange = this.handleProviderChange.bind(this);
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.renderProviderForm = this.renderProviderForm.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
        this.createChangeHandler = this.createChangeHandler.bind(this);
        this.createMultiSelectHandler = this.createMultiSelectHandler.bind(this);

        const defaultDescriptor = this.props.descriptors.find(descriptor => descriptor.type === DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const { fields, context, name } = defaultDescriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const emptyFieldModel = FieldModelUtilities.createEmptyFieldModel(fieldKeys, context, name);
        const channelFieldModel = FieldModelUtilities.updateFieldModelSingleValue(emptyFieldModel, KEY_CHANNEL_NAME, name);
        const providerFieldModel = FieldModelUtilities.createEmptyFieldModel([], DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, null);
        this.state = {
            show: true,
            channelConfig: channelFieldModel,
            providerConfig: providerFieldModel,
            currentChannel: defaultDescriptor,
            currentProvider: {}
        };
    }

    componentDidUpdate() {
        const { channelConfig, currentChannel, currentProvider } = this.state;
        const selectedChannelOption = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_CHANNEL_NAME);
        const selectedProviderOption = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_PROVIDER_NAME);
        const prevChannelName = currentChannel ? currentChannel.name : '';
        const prevProviderName = currentProvider ? currentProvider.name : '';
        if (prevChannelName !== selectedChannelOption || prevProviderName !== selectedProviderOption) {
            const newChannel = this.props.descriptors.find(descriptor => descriptor.name === selectedChannelOption && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION) || {};
            const newProvider = this.props.descriptors.find(descriptor => descriptor.name === selectedProviderOption && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION) || {};
            const emptyProviderConfig = FieldModelUtilities.createEmptyFieldModel(newProvider.fields, newProvider.context, newProvider.name);
            const updatedProviderConfig = FieldModelUtilities.combineFieldModels(emptyProviderConfig, this.state.providerConfig);
            this.setState({
                providerConfig: updatedProviderConfig,
                currentChannel: newChannel,
                currentProvider: newProvider
            });
        }
    }

    handleSaveBtnClick() {
        this.props.onModalClose();
    }

    handleClose() {
        this.setState({ show: false });
        this.props.onModalClose();
        this.props.handleCancel();
    }

    handleChannelChange(event) {
        const { target } = event;
        if (target) {
            const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.channelConfig, target.name, value);

            this.setState({
                channelConfig: newState
            });
        } else {
            const { value, name } = event;
            if (value && name) {
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.channelConfig, name, value);
                this.setState({
                    channelConfig: newState
                });
            }
        }
    }

    handleProviderChange(event) {
        const { target } = event;
        if (target) {
            const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.providerConfig, target.name, value);

            this.setState({
                providerConfig: newState
            });
        } else {
            const { value, name } = event;
            if (value && name) {
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.providerConfig, name, value);
                this.setState({
                    providerConfig: newState
                });
            }
        }
    }

    buildJsonBody() {
        const { channelConfig, providerConfig } = this.state;
        const { jobId } = this.props;
        const configuration = Object.assign({}, {
            fieldModels: [
                channelConfig,
                providerConfig
            ]
        });

        if (jobId) {
            Object.assign(configuration, { jobId });
        }
        return configuration;
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

    createChangeHandler(negateCheckboxValue) {
        return (event) => {
            const { target } = event;
            let targetValue = target.value;
            if (target.type === 'checkbox') {
                const { checked } = target;
                if (negateCheckboxValue) {
                    targetValue = (!checked).toString();
                } else {
                    targetValue = checked.toString();
                }
            }
            const fieldModel = this.state.providerConfig;
            const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, target.name, targetValue);
            this.setState({
                providerConfig: newState
            });
        };
    }

    createMultiSelectHandler(fieldKey) {
        return (selectedValues) => {
            if (selectedValues && selectedValues.length > 0) {
                const selected = selectedValues.map(item => item.value);
                const newState = FieldModelUtilities.updateFieldModelValues(this.state.providerConfig, fieldKey, selected);
                this.setState({
                    providerConfig: newState
                });
            } else {
                const newState = FieldModelUtilities.updateFieldModelValues(this.state.providerConfig, fieldKey, []);
                this.setState({
                    providerConfig: newState
                });
            }
        };
    }

    renderProviderForm() {
        const { providerConfig, currentProvider, channelConfig } = this.state;
        const updatedProviderFields = currentProvider.fields.filter(field => field.key !== KEY_CONFIGURED_PROJECT);
        Object.assign(currentProvider, { fields: updatedProviderFields });
        return (
            <div>
                <FieldsPanel descriptorFields={currentProvider.fields} currentConfig={providerConfig} fieldErrors={this.props.fieldErrors} handleChange={this.handleProviderChange} />
                <ProjectConfiguration
                    providerName={FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_PROVIDER_NAME)}
                    includeAllProjects={!FieldModelUtilities.getFieldModelBooleanValue(providerConfig, KEY_FILTER_BY_PROJECT)}
                    handleChange={this.createChangeHandler(true)}
                    handleProjectChanged={this.createMultiSelectHandler(KEY_CONFIGURED_PROJECT)}
                    projects={this.props.projects}
                    configuredProjects={FieldModelUtilities.getFieldModelValues(providerConfig, KEY_CONFIGURED_PROJECT)}
                    projectNamePattern={FieldModelUtilities.getFieldModelSingleValueOrDefault(providerConfig, KEY_PROJECT_NAME_PATTERN, '')}
                    fieldErrors={this.props.fieldErrors}
                />
                <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.handleClose} isFixed={false} />
                <p name="configurationMessage">{this.state.configurationMessage}</p>
            </div>
        );
    }

    render() {
        const { channelConfig, currentProvider, currentChannel } = this.state;
        const selectedProvider = currentProvider && currentProvider.name;

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
                        <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate>
                            <FieldsPanel descriptorFields={currentChannel.fields} currentConfig={channelConfig} fieldErrors={this.props.fieldErrors} handleChange={this.handleChannelChange} />
                            {selectedProvider && this.renderProviderForm()}
                        </form>
                    </Modal.Body>
                </Modal>
            </div>

        );
    }
}

DistributionConfiguration.propTypes = {
    projects: PropTypes.arrayOf(PropTypes.object),
    fieldErrors: PropTypes.object,
    configurationMessage: PropTypes.string,
    jobId: PropTypes.string,
    handleCancel: PropTypes.func.isRequired,
    onModalClose: PropTypes.func.isRequired,
    updateDistributionJob: PropTypes.func.isRequired,
    testDistributionJob: PropTypes.func.isRequired,
    saveDistributionJob: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired
};

DistributionConfiguration.defaultProps = {
    projects: []
};

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
