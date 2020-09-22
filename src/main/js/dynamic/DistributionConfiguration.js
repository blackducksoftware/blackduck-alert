import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { OPERATIONS } from 'util/descriptorUtilities';
import FieldsPanel from 'field/FieldsPanel';
import {
    checkDescriptorForGlobalConfig, getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob, validateDistributionJob
} from 'store/actions/distributionConfigs';
import ConfigButtons from 'component/common/ConfigButtons';
import { Modal } from 'react-bootstrap';
import JobCustomMessageModal from 'dynamic/JobCustomMessageModal';

export const KEY_ENABLED = 'channel.common.enabled';
export const KEY_NAME = 'channel.common.name';
export const KEY_CHANNEL_NAME = 'channel.common.channel.name';
export const KEY_PROVIDER_NAME = 'channel.common.provider.name';
export const KEY_FREQUENCY = 'channel.common.frequency';
export const KEY_PROVIDER_CONFIG_ID = 'provider.common.config.id';

export const COMMON_KEYS = [KEY_ENABLED, KEY_NAME, KEY_CHANNEL_NAME, KEY_PROVIDER_NAME, KEY_FREQUENCY];

class DistributionConfiguration extends Component {
    constructor(props) {
        super(props);

        this.buildJsonBody = this.buildJsonBody.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.renderProviderConfigNameForm = this.renderProviderConfigNameForm.bind(this);
        this.renderProviderForm = this.renderProviderForm.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
        this.setSendMessageVisible = this.setSendMessageVisible.bind(this);

        const { descriptors, validateDescriptorForGlobalConfig } = props;
        const defaultDescriptor = descriptors.find((descriptor) => descriptor.type === DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const { fields, context, name } = defaultDescriptor;
        validateDescriptorForGlobalConfig(KEY_CHANNEL_NAME, name);
        const emptyFieldModel = FieldModelUtilities.createFieldModelWithDefaults(fields, context, name);
        const channelFieldModel = FieldModelUtilities.updateFieldModelSingleValue(emptyFieldModel, KEY_CHANNEL_NAME, name);
        this.state = {
            show: true,
            showSendMessage: false,
            channelConfig: channelFieldModel,
            providerConfig: {},
            currentChannel: defaultDescriptor,
            currentProvider: {}
        };
        this.loading = false;
    }

    componentDidMount() {
        const { jobId, getDistribution } = this.props;
        getDistribution(jobId);
        if (jobId) {
            this.loading = true;
        }
    }

    componentDidUpdate(prevProps) {
        const {
            success, onSave, onModalClose, fetching, inProgress, job, descriptors, validateDescriptorForGlobalConfig, status, isUpdatingJob, jobId, saveDistribution, updateDistribution
        } = this.props;
        if (prevProps.saving && success) {
            this.setState({ show: false });
            onSave(this.state);
            onModalClose();
        }

        if (!fetching && !inProgress) {
            if (this.loading) {
                this.loading = false;
                if (job && job.fieldModels) {
                    const channelModel = job.fieldModels.find((model) => FieldModelUtilities.hasKey(model, KEY_CHANNEL_NAME));
                    const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, KEY_PROVIDER_NAME);
                    const providerModel = job.fieldModels.find((model) => providerName === model.descriptorName);
                    const newChannel = descriptors.find((descriptor) => descriptor.name === channelModel.descriptorName && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
                    const newProvider = descriptors.find((descriptor) => descriptor.name === providerModel.descriptorName && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
                    validateDescriptorForGlobalConfig(KEY_CHANNEL_NAME, newChannel.name);
                    validateDescriptorForGlobalConfig(KEY_PROVIDER_NAME, newProvider.name);
                    this.setState({
                        fieldErrors: {},
                        channelConfig: channelModel,
                        providerConfig: providerModel,
                        currentChannel: newChannel,
                        currentProvider: newProvider
                    });
                }
            }
        }

        const { channelConfig, currentChannel, currentProvider } = this.state;

        const selectedChannelOption = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_CHANNEL_NAME);
        const prevChannelName = currentChannel ? currentChannel.name : '';

        if (selectedChannelOption && prevChannelName !== selectedChannelOption) {
            const newChannel = descriptors.find((descriptor) => descriptor.name === selectedChannelOption && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const emptyChannelConfig = FieldModelUtilities.createFieldModelWithDefaults(newChannel.fields, newChannel.context, newChannel.name);
            const updatedChannelConfig = { ...FieldModelUtilities.updateFieldModelSingleValue(emptyChannelConfig, KEY_CHANNEL_NAME, selectedChannelOption) };
            const name = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_NAME) || '';
            const frequency = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_FREQUENCY) || '';
            const provider = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_PROVIDER_NAME) || '';
            const keepName = Object.assign(updatedChannelConfig, FieldModelUtilities.updateFieldModelSingleValue(updatedChannelConfig, KEY_NAME, name));
            const keepFrequency = Object.assign(keepName, FieldModelUtilities.updateFieldModelSingleValue(updatedChannelConfig, KEY_FREQUENCY, frequency));
            const keepProvider = Object.assign(keepFrequency, FieldModelUtilities.updateFieldModelSingleValue(updatedChannelConfig, KEY_PROVIDER_NAME, provider));

            validateDescriptorForGlobalConfig(KEY_CHANNEL_NAME, newChannel.name);
            this.setState({
                channelConfig: keepProvider,
                currentChannel: newChannel
            });
        }
        const selectedProviderOption = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_PROVIDER_NAME);
        const prevProviderName = currentProvider ? currentProvider.name : '';

        if (selectedProviderOption && prevProviderName !== selectedProviderOption) {
            const newProvider = descriptors.find((descriptor) => descriptor.name === selectedProviderOption && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const emptyProviderConfig = FieldModelUtilities.createFieldModelWithDefaults(newProvider.fields, newProvider.context, newProvider.name);
            validateDescriptorForGlobalConfig(KEY_PROVIDER_NAME, newProvider.name);
            this.setState({
                providerConfig: emptyProviderConfig,
                currentProvider: newProvider
            });
        }

        if (prevProps.status === 'VALIDATING' && status === 'VALIDATED') {
            const jsonBody = this.buildJsonBody();
            if (isUpdatingJob) {
                updateDistribution(jsonBody);
            } else {
                saveDistribution(jsonBody);
            }
        }
    }

    setSendMessageVisible(visible) {
        this.setState({
            showSendMessage: visible
        });
    }

    handleTestSubmit(event) {
        event.preventDefault();
        const { testDistribution } = this.props;
        const jsonBody = this.buildJsonBody();
        testDistribution(jsonBody);
    }

    buildJsonBody() {
        const { channelConfig, providerConfig } = this.state;
        const { jobId } = this.props;
        return {
            jobId,
            fieldModels: [
                channelConfig,
                providerConfig
            ]
        };
    }

    handleClose() {
        const { onModalClose, handleCancel } = this.props;
        this.setState({ show: false });
        onModalClose();
        handleCancel();
    }

    handleSubmit(event) {
        event.preventDefault();
        const { validateDistribution } = this.props;

        const jsonBody = this.buildJsonBody();
        validateDistribution(jsonBody);
    }

    renderProviderConfigNameForm() {
        // TODO: Find a better way to order fields.
        // TODO: Perhaps have a config name key such as common, channel, and provider to create the config objects and some ordering attributes.
        const {
            providerConfig, channelConfig, currentProvider
        } = this.state;
        const { fieldErrors } = this.props;
        const configNameFields = currentProvider.fields.filter((field) => field.key === KEY_PROVIDER_CONFIG_ID);
        return (
            <div>
                <FieldsPanel
                    descriptorFields={configNameFields}
                    metadata={{ additionalFields: channelConfig.keyToValues }}
                    currentConfig={providerConfig}
                    fieldErrors={fieldErrors}
                    self={this}
                    stateName="providerConfig"
                />
            </div>
        );
    }

    renderProviderForm() {
        const {
            providerConfig, channelConfig, currentChannel, currentProvider, showSendMessage
        } = this.state;
        const { fieldErrors, testDistribution, configurationMessage } = this.props;
        const displayTest = !currentChannel.readOnly && DescriptorUtilities.isOperationAssigned(currentChannel, OPERATIONS.EXECUTE);
        const displaySave = !currentChannel.readOnly && DescriptorUtilities.isOneOperationAssigned(currentChannel, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
        const channelDescriptorName = channelConfig && channelConfig.descriptorName;
        const providerFields = currentProvider.fields.filter((field) => field.key !== KEY_PROVIDER_CONFIG_ID);
        return (
            <div>
                <FieldsPanel
                    descriptorFields={providerFields}
                    metadata={{ additionalFields: channelConfig.keyToValues }}
                    currentConfig={providerConfig}
                    fieldErrors={fieldErrors}
                    self={this}
                    stateName="providerConfig"
                />
                <ConfigButtons
                    cancelId="job-cancel"
                    submitId="job-submit"
                    includeTest={displayTest}
                    includeSave={displaySave}
                    includeCancel
                    onTestClick={() => this.setSendMessageVisible(true)}
                    onCancelClick={this.handleClose}
                    isFixed={false}
                />
                <JobCustomMessageModal
                    topicLabel="Topic"
                    messageLabel="Message"
                    showModal={showSendMessage}
                    jobFieldModelBuilder={this.buildJsonBody}
                    sendMessage={testDistribution}
                    handleCancel={() => this.setSendMessageVisible(false)}
                    channelDescriptorName={channelDescriptorName}
                />
                <p
                    id="distribution-configuration-message"
                    name="configurationMessage"
                >
                    {configurationMessage}
                </p>
            </div>
        );
    }

    render() {
        const {
            providerConfig, channelConfig, currentProvider, currentChannel, show
        } = this.state;
        const { job, isUpdatingJob, fieldErrors } = this.props;
        const selectedProvider = (currentProvider) ? currentProvider.name : null;

        let jobAction = 'New';
        if (job.jobId) {
            jobAction = isUpdatingJob ? 'Edit' : 'Copy';
        }
        const modalTitle = `${jobAction} Distribution Job`;

        const commonFields = currentChannel.fields.filter((field) => COMMON_KEYS.includes(field.key));
        const channelFields = currentChannel.fields.filter((field) => field.key !== KEY_PROVIDER_NAME && !COMMON_KEYS.includes(field.key));

        return (
            <div
                onKeyDown={(e) => e.stopPropagation()}
                onClick={(e) => e.stopPropagation()}
                onFocus={(e) => e.stopPropagation()}
                onMouseOver={(e) => e.stopPropagation()}
            >
                <Modal size="lg" show={show} onHide={this.handleClose}>
                    <Modal.Header closeButton>
                        <Modal.Title>{modalTitle}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate>
                            <FieldsPanel
                                descriptorFields={commonFields}
                                currentConfig={channelConfig}
                                fieldErrors={fieldErrors}
                                self={this}
                                stateName="channelConfig"
                            />
                            {currentChannel && selectedProvider && this.renderProviderConfigNameForm()}
                            {currentChannel && selectedProvider
                            && (
                                <FieldsPanel
                                    descriptorFields={channelFields}
                                    metadata={{ additionalFields: providerConfig.keyToValues }}
                                    currentConfig={channelConfig}
                                    fieldErrors={fieldErrors}
                                    self={this}
                                    stateName="channelConfig"
                                />
                            )}
                            {currentChannel && selectedProvider && this.renderProviderForm()}
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
    job: PropTypes.object,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool,
    saving: PropTypes.bool,
    success: PropTypes.bool,
    onSave: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    onModalClose: PropTypes.func.isRequired,
    getDistribution: PropTypes.func.isRequired,
    updateDistribution: PropTypes.func.isRequired,
    testDistribution: PropTypes.func.isRequired,
    saveDistribution: PropTypes.func.isRequired,
    validateDescriptorForGlobalConfig: PropTypes.func.isRequired,
    validateDistribution: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    isUpdatingJob: PropTypes.bool,
    status: PropTypes.string.isRequired
};

DistributionConfiguration.defaultProps = {
    projects: [],
    jobId: null,
    job: {},
    fetching: false,
    inProgress: false,
    saving: false,
    success: false,
    fieldErrors: {},
    configurationMessage: '',
    isUpdatingJob: false
};

const mapDispatchToProps = (dispatch) => ({
    getDistribution: (id) => dispatch(getDistributionJob(id)),
    saveDistribution: (config) => dispatch(saveDistributionJob(config)),
    updateDistribution: (config) => dispatch(updateDistributionJob(config)),
    testDistribution: (config) => dispatch(testDistributionJob(config)),
    validateDescriptorForGlobalConfig: (fieldErrorName, descriptorName) => dispatch(checkDescriptorForGlobalConfig(fieldErrorName, descriptorName)),
    validateDistribution: (config) => dispatch(validateDistributionJob(config))
});

const mapStateToProps = (state) => ({
    job: state.distributionConfigs.job,
    fieldErrors: state.distributionConfigs.error.fieldErrors,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress,
    descriptors: state.descriptors.items,
    saving: state.distributionConfigs.saving,
    success: state.distributionConfigs.success,
    testingConfig: state.distributionConfigs.testingConfig,
    configurationMessage: state.distributionConfigs.configurationMessage,
    status: state.distributionConfigs.status
});

export default connect(mapStateToProps, mapDispatchToProps)(DistributionConfiguration);
