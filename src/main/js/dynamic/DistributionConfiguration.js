import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { OPERATIONS } from 'util/descriptorUtilities';
import FieldsPanel from 'field/FieldsPanel';
import { checkDescriptorForGlobalConfig, getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributionConfigs';
import ConfigButtons from 'component/common/ConfigButtons';
import { Modal } from 'react-bootstrap';
import JobCustomMessageModal from 'dynamic/JobCustomMessageModal';

export const KEY_ENABLED = 'channel.common.enabled';
export const KEY_NAME = 'channel.common.name';
export const KEY_CHANNEL_NAME = 'channel.common.channel.name';
export const KEY_PROVIDER_NAME = 'channel.common.provider.name';
export const KEY_FREQUENCY = 'channel.common.frequency';

export const COMMON_KEYS = [KEY_ENABLED, KEY_NAME, KEY_CHANNEL_NAME, KEY_PROVIDER_NAME, KEY_FREQUENCY];


class DistributionConfiguration extends Component {
    constructor(props) {
        super(props);

        this.buildJsonBody = this.buildJsonBody.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.renderProviderForm = this.renderProviderForm.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
        this.setSendMessageVisible = this.setSendMessageVisible.bind(this);

        const defaultDescriptor = this.props.descriptors.find(descriptor => descriptor.type === DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const { fields, context, name } = defaultDescriptor;
        this.props.checkDescriptorForGlobalConfig(KEY_CHANNEL_NAME, name);
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
        const { jobId } = this.props;
        this.props.getDistributionJob(jobId);
        if (jobId) {
            this.loading = true;
        }
    }

    componentDidUpdate(prevProps) {
        if (prevProps.saving && this.props.success) {
            this.setState({ show: false });
            this.props.onSave(this.state);
            this.props.onModalClose();
        }

        if (!this.props.fetching && !this.props.inProgress) {
            if (this.loading) {
                this.loading = false;
                const { job } = this.props;
                if (job && job.fieldModels) {
                    const channelModel = this.props.job.fieldModels.find(model => FieldModelUtilities.hasKey(model, KEY_CHANNEL_NAME));
                    const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, KEY_PROVIDER_NAME);
                    const providerModel = this.props.job.fieldModels.find(model => providerName === model.descriptorName);
                    const newChannel = this.props.descriptors.find(descriptor => descriptor.name === channelModel.descriptorName && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
                    const newProvider = this.props.descriptors.find(descriptor => descriptor.name === providerModel.descriptorName && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
                    this.props.checkDescriptorForGlobalConfig(KEY_CHANNEL_NAME, newChannel.name);
                    this.props.checkDescriptorForGlobalConfig(KEY_PROVIDER_NAME, newProvider.name);
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
            const newChannel = this.props.descriptors.find(descriptor => descriptor.name === selectedChannelOption && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const emptyChannelConfig = FieldModelUtilities.createFieldModelWithDefaults(newChannel.fields, newChannel.context, newChannel.name);
            const updatedChannelConfig = Object.assign({}, FieldModelUtilities.updateFieldModelSingleValue(emptyChannelConfig, KEY_CHANNEL_NAME, selectedChannelOption));
            const name = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_NAME) || '';
            const frequency = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_FREQUENCY) || '';
            const provider = FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_PROVIDER_NAME) || '';
            const keepName = Object.assign(updatedChannelConfig, FieldModelUtilities.updateFieldModelSingleValue(updatedChannelConfig, KEY_NAME, name));
            const keepFrequency = Object.assign(keepName, FieldModelUtilities.updateFieldModelSingleValue(updatedChannelConfig, KEY_FREQUENCY, frequency));
            const keepProvider = Object.assign(keepFrequency, FieldModelUtilities.updateFieldModelSingleValue(updatedChannelConfig, KEY_PROVIDER_NAME, provider));

            this.props.checkDescriptorForGlobalConfig(KEY_CHANNEL_NAME, newChannel.name);
            this.setState({
                channelConfig: keepProvider,
                currentChannel: newChannel
            });
        }
        const selectedProviderOption = FieldModelUtilities.getFieldModelSingleValue(this.state.channelConfig, KEY_PROVIDER_NAME);
        const prevProviderName = currentProvider ? currentProvider.name : '';

        if (selectedProviderOption && prevProviderName !== selectedProviderOption) {
            const newProvider = this.props.descriptors.find(descriptor => descriptor.name === selectedProviderOption && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const emptyProviderConfig = FieldModelUtilities.createFieldModelWithDefaults(newProvider.fields, newProvider.context, newProvider.name);
            this.props.checkDescriptorForGlobalConfig(KEY_PROVIDER_NAME, newProvider.name);
            this.setState({
                providerConfig: emptyProviderConfig,
                currentProvider: newProvider
            });
        }
    }

    handleClose() {
        this.setState({ show: false });
        this.props.onModalClose();
        this.props.handleCancel();
    }

    buildJsonBody() {
        const { channelConfig, providerConfig } = this.state;
        return Object.assign({}, {
            fieldModels: [
                channelConfig,
                providerConfig
            ]
        });
    }

    handleTestSubmit(event) {
        event.preventDefault();

        const jsonBody = this.buildJsonBody();
        this.props.testDistributionJob(jsonBody);
    }

    setSendMessageVisible(visible) {
        this.setState({
            showSendMessage: visible
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const { jobId, isUpdatingJob } = this.props;

        const jsonBody = this.buildJsonBody();
        if (isUpdatingJob) {
            const withId = Object.assign(jsonBody, { jobId });
            this.props.updateDistributionJob(withId);
        } else {
            this.props.saveDistributionJob(jsonBody);
        }
    }

    renderProviderForm() {
        const {
            providerConfig, channelConfig, currentChannel, currentProvider
        } = this.state;
        const displayTest = !currentChannel.readOnly && DescriptorUtilities.isOperationAssigned(currentChannel, OPERATIONS.EXECUTE);
        const displaySave = !currentChannel.readOnly && DescriptorUtilities.isOneOperationAssigned(currentChannel, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
        const channelDescriptorName = channelConfig && channelConfig.descriptorName;

        return (
            <div>
                <FieldsPanel
                    descriptorFields={currentProvider.fields}
                    metadata={{ additionalFields: channelConfig.keyToValues }}
                    currentConfig={providerConfig}
                    fieldErrors={this.props.fieldErrors}
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
                    showModal={this.state.showSendMessage}
                    jobFieldModelBuilder={this.buildJsonBody}
                    sendMessage={this.props.testDistributionJob}
                    handleCancel={() => this.setSendMessageVisible(false)}
                    channelDescriptorName={channelDescriptorName}
                />
                <p name="configurationMessage">{this.props.configurationMessage}</p>
            </div>
        );
    }

    render() {
        const { providerConfig, channelConfig, currentProvider, currentChannel } = this.state;
        const selectedProvider = (currentProvider) ? currentProvider.name : null;

        let jobAction = 'New';
        if (this.props.job.jobId) {
            jobAction = this.props.isUpdatingJob ? 'Edit' : 'Copy';
        }
        const modalTitle = `${jobAction} Distribution Job`;


        const commonFields = currentChannel.fields.filter(field => COMMON_KEYS.includes(field.key));
        const channelFields = currentChannel.fields.filter(field => !COMMON_KEYS.includes(field.key));

        return (
            <div
                onKeyDown={e => e.stopPropagation()}
                onClick={e => e.stopPropagation()}
                onFocus={e => e.stopPropagation()}
                onMouseOver={e => e.stopPropagation()}
            >
                <Modal size="lg" show={this.state.show} onHide={this.handleClose}>
                    <Modal.Header closeButton>
                        <Modal.Title>{modalTitle}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate>
                            <FieldsPanel
                                descriptorFields={commonFields}
                                currentConfig={channelConfig}
                                fieldErrors={this.props.fieldErrors}
                                self={this}
                                stateName="channelConfig"
                            />
                            {currentChannel && selectedProvider &&
                            <FieldsPanel
                                descriptorFields={channelFields}
                                metadata={{ additionalFields: providerConfig.keyToValues }}
                                currentConfig={channelConfig}
                                fieldErrors={this.props.fieldErrors}
                                self={this}
                                stateName="channelConfig"
                            />
                            }
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
    getDistributionJob: PropTypes.func.isRequired,
    updateDistributionJob: PropTypes.func.isRequired,
    testDistributionJob: PropTypes.func.isRequired,
    saveDistributionJob: PropTypes.func.isRequired,
    checkDescriptorForGlobalConfig: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    isUpdatingJob: PropTypes.bool
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

const mapDispatchToProps = dispatch => ({
    getDistributionJob: id => dispatch(getDistributionJob(id)),
    saveDistributionJob: config => dispatch(saveDistributionJob(config)),
    updateDistributionJob: config => dispatch(updateDistributionJob(config)),
    testDistributionJob: config => dispatch(testDistributionJob(config)),
    checkDescriptorForGlobalConfig: (fieldErrorName, descriptorName) => dispatch(checkDescriptorForGlobalConfig(fieldErrorName, descriptorName))
});

const mapStateToProps = state => ({
    job: state.distributionConfigs.job,
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
