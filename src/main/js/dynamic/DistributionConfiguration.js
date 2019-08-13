import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { OPERATIONS } from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import FieldsPanel from 'field/FieldsPanel';
import { getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributionConfigs';
import ProjectConfiguration from 'distribution/ProjectConfiguration';
import ConfigButtons from 'component/common/ConfigButtons';
import { Modal } from 'react-bootstrap';

export const KEY_NAME = 'channel.common.name';
export const KEY_CHANNEL_NAME = 'channel.common.channel.name';
export const KEY_PROVIDER_NAME = 'channel.common.provider.name';
export const KEY_FREQUENCY = 'channel.common.frequency';

export const KEY_FILTER_BY_PROJECT = 'channel.common.filter.by.project';
export const KEY_PROJECT_NAME_PATTERN = 'channel.common.project.name.pattern';
export const KEY_CONFIGURED_PROJECT = 'channel.common.configured.project';

class DistributionConfiguration extends Component {
    constructor(props) {
        super(props);

        this.buildJsonBody = this.buildJsonBody.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.renderProviderForm = this.renderProviderForm.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
        this.createMultiSelectHandler = this.createMultiSelectHandler.bind(this);

        const defaultDescriptor = this.props.descriptors.find(descriptor => descriptor.type === DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const { fields, context, name } = defaultDescriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const emptyFieldModel = FieldModelUtilities.createEmptyFieldModel(fieldKeys, context, name);
        const channelFieldModel = FieldModelUtilities.updateFieldModelSingleValue(emptyFieldModel, KEY_CHANNEL_NAME, name);
        this.state = {
            show: true,
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

    componentWillReceiveProps(nextProps) {
        if (this.props.saving && nextProps.success) {
            this.setState({ show: false });
            this.props.onSave(this.state);
            this.props.onModalClose();
        }

        if (!nextProps.fetching && !nextProps.inProgress) {
            if (this.loading) {
                this.loading = false;
                const { job } = nextProps;
                if (job && job.fieldModels) {
                    const channelModel = nextProps.job.fieldModels.find(model => FieldModelUtilities.hasKey(model, KEY_NAME));
                    const providerModel = nextProps.job.fieldModels.find(model => !FieldModelUtilities.hasKey(model, KEY_NAME));
                    const newChannel = this.props.descriptors.find(descriptor => descriptor.name === channelModel.descriptorName && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
                    const newProvider = this.props.descriptors.find(descriptor => descriptor.name === providerModel.descriptorName && descriptor.context === DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);

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
    }

    componentDidUpdate() {
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

    // FIXME this function is probably not necessary
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
        const {
            providerConfig, currentProvider, channelConfig, currentChannel
        } = this.state;
        const updatedProviderFields = Object.assign({}, currentProvider);
        const filterByProject = FieldModelUtilities.getFieldModelBooleanValue(providerConfig, KEY_FILTER_BY_PROJECT);
        const removeProject = updatedProviderFields.fields.filter(field => field.key !== KEY_CONFIGURED_PROJECT);
        const displayTest = !currentChannel.readOnly && DescriptorUtilities.isOperationAssigned(currentChannel, OPERATIONS.EXECUTE);
        const displaySave = !currentChannel.readOnly && DescriptorUtilities.isOneOperationAssigned(currentChannel, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
        const isReadOnly = currentChannel.readOnly;

        let removedFields = Object.assign(updatedProviderFields, { fields: removeProject });
        if (!filterByProject) {
            const removePattern = updatedProviderFields.fields.filter(field => field.key !== KEY_PROJECT_NAME_PATTERN);
            removedFields = Object.assign(removedFields, { fields: removePattern });
        }

        return (
            <div>
                <FieldsPanel
                    descriptorFields={removedFields.fields}
                    currentConfig={providerConfig}
                    fieldErrors={this.props.fieldErrors}
                    self={this}
                    stateName="providerConfig"
                />
                <ProjectConfiguration
                    providerName={FieldModelUtilities.getFieldModelSingleValue(channelConfig, KEY_PROVIDER_NAME)}
                    includeAllProjects={!filterByProject}
                    handleProjectChanged={this.createMultiSelectHandler(KEY_CONFIGURED_PROJECT)}
                    projects={this.props.projects}
                    configuredProjects={FieldModelUtilities.getFieldModelValues(providerConfig, KEY_CONFIGURED_PROJECT)}
                    projectNamePattern={FieldModelUtilities.getFieldModelSingleValueOrDefault(providerConfig, KEY_PROJECT_NAME_PATTERN, '')}
                    fieldErrors={this.props.fieldErrors}
                    readOnly={isReadOnly}
                />
                <ConfigButtons
                    cancelId="job-cancel"
                    submitId="job-submit"
                    includeTest={displayTest}
                    includeSave={displaySave}
                    includeCancel
                    onTestClick={this.handleTestSubmit}
                    onCancelClick={this.handleClose}
                    isFixed={false}
                />
                <p name="configurationMessage">{this.props.configurationMessage}</p>
            </div>
        );
    }

    render() {
        const { channelConfig, currentProvider, currentChannel } = this.state;
        const selectedProvider = (currentProvider) ? currentProvider.name : null;

        let jobAction = 'New';
        if (this.props.job.jobId) {
            jobAction = this.props.isUpdatingJob ? 'Edit' : 'Copy';
        }
        const modalTitle = `${jobAction} Distribution Job`;

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
                                descriptorFields={currentChannel.fields}
                                currentConfig={channelConfig}
                                fieldErrors={this.props.fieldErrors}
                                self={this}
                                stateName="channelConfig"
                            />
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
    testDistributionJob: config => dispatch(testDistributionJob(config))
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
