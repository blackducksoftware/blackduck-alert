import React, { Component } from 'react';
import Select, { components } from 'react-select';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TextInput from 'field/input/TextInput';
import ProjectConfiguration from 'distribution/ProjectConfiguration';
import ConfigButtons from 'component/common/ConfigButtons';

import { getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributionConfigs';
import { getDistributionDescriptor } from 'store/actions/descriptors';
import DescriptorOption from 'component/common/DescriptorOption';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const { Option, SingleValue } = components;

const CustomProviderTypeOptionLabel = props => (
    <Option {...props}>
        <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
    </Option>
);

const CustomProviderTypeLabel = props => (
    <SingleValue {...props}>
        <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
    </SingleValue>
);


const KEY_NAME = 'channel.common.name';
const KEY_CHANNEL_NAME = 'channel.common.channel.name';
const KEY_PROVIDER_NAME = 'channel.common.provider.name';
const KEY_FREQUENCY = 'channel.common.frequency';
const KEY_NOTIFICATION_TYPES = 'provider.distribution.notification.types';
const KEY_FORMAT_TYPE = 'provider.distribution.format.type';

// blackduck common keys
const KEY_FILTER_BY_PROJECT = 'channel.common.filter.by.project';
const KEY_PROJECT_NAME_PATTERN = 'channel.common.project.name.pattern';
const KEY_CONFIGURED_PROJECT = 'channel.common.configured.project';

const fieldNames = [
    KEY_NAME,
    KEY_CHANNEL_NAME,
    KEY_PROVIDER_NAME,
    KEY_FREQUENCY,
    KEY_NOTIFICATION_TYPES,
    KEY_FORMAT_TYPE,
    KEY_FILTER_BY_PROJECT,
    KEY_PROJECT_NAME_PATTERN,
    KEY_CONFIGURED_PROJECT
];

const FIELD_MODEL_KEY = {
    COMMON: 'commonConfig',
    PROVIDER: 'providerConfig'
}

class BaseJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            success: false,
            fieldErrors: {},
            commonConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, this.props.alertChannelName),
            providerConfig: {},
            providerOptions: []
        };
        this.loading = false;
        this.saving = false;
        this.buildJsonBody = this.buildJsonBody.bind(this);
        this.createChangeHandler = this.createChangeHandler.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
        this.getSelectedSingleValue = this.getSelectedSingleValue.bind(this);
        this.getSelectedValues = this.getSelectedValues.bind(this);
        this.createFrequencyOptions = this.createFrequencyOptions.bind(this);
        this.createProviderOptions = this.createProviderOptions.bind(this);
        this.createNotificationTypeOptions = this.createNotificationTypeOptions.bind(this);
        this.createFormatTypeOptions = this.createFormatTypeOptions.bind(this);
        this.renderDistributionForm = this.renderDistributionForm.bind(this);
        this.createSingleSelectHandler = this.createSingleSelectHandler.bind(this);
        this.createMultiSelectHandler = this.createMultiSelectHandler.bind(this);
    }

    componentDidMount() {
        this.loading = true;
    }

    componentWillReceiveProps(nextProps) {
        if (this.saving) {
            this.saving = false;
            if (nextProps.success && nextProps.handleSaveBtnClick) {
                nextProps.handleSaveBtnClick(this.state);
                return;
            }
        }
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (nextProps.fieldErrors.message || nextProps.testingConfig) {
                // If there are errors, we only want to update the error messaging. We do not want to clear out the User's changes
                this.setState({
                    fieldErrors: nextProps.fieldErrors,
                    configurationMessage: nextProps.configurationMessage
                });
            } else if (this.loading) {
                this.loading = false;
                const stateValues = Object.assign({}, this.state, {
                    fetching: nextProps.fetching,
                    inProgress: nextProps.inProgress,
                    success: nextProps.success,
                    configurationMessage: nextProps.configurationMessage,
                    fieldErrors: nextProps.fieldErrors ? nextProps.fieldErrors : {}
                });

                if (nextProps.distributionConfigId) {
                    const jobConfig = nextProps.jobs[nextProps.distributionConfigId];
                    if (jobConfig) {

                    }
                    const newState = Object.assign({}, stateValues, {
                        id: jobConfig.id,
                        distributionConfigId: nextProps.distributionConfigId,
                        name: jobConfig.name,
                        providerName: jobConfig.providerName,
                        distributionType: jobConfig.distributionType,
                        frequency: jobConfig.frequency,
                        formatType: jobConfig.formatType,
                        includeAllProjects: jobConfig.filterByProject === 'false',
                        filterByProject: jobConfig.filterByProject,
                        notificationTypes: jobConfig.notificationTypes,
                        configuredProjects: jobConfig.configuredProjects,
                        projectNamePattern: jobConfig.projectNamePattern
                    });
                    this.setState(newState);
                } else {
                    if (this.state.includeAllProjects == null || undefined === this.state.includeAllProjects) {
                        this.setState({
                            includeAllProjects: true
                        });
                    }
                    this.setState(stateValues);
                }
            }
        }
    }

    onSubmit(event) {
        event.preventDefault();
        const { handleSaveBtnClick, handleCancel } = this.props;
        this.handleSubmit();
        if (handleCancel && !handleSaveBtnClick) {
            handleCancel();
        }
    }

    getSelectedSingleValue(options, fieldModel, fieldKey) {
        if (options) {
            if (options.length === 1) {
                const [selectedProviderOption] = options;
                return selectedProviderOption;
            }
            return options.find(option => option.value === FieldModelUtilities.getFieldModelSingleValue(fieldModel, fieldKey));
        }
        return null;
    }

    getSelectedValues(options, fieldModel, fieldKey) {
        if (options) {
            if (options.length === 1) {
                return options;
            }
            return options.filter(option => FieldModelUtilities.getFieldModelValues(fieldModel, fieldKey).indexOf(option.value) !== -1);
        }
        return [];
    }

    handleSubmit(event) {
        this.saving = true;
        this.setState({
            fieldErrors: {}
        });
        if (event) {
            event.preventDefault();
        }
        const jsonBody = this.buildJsonBody();
        if (this.state.commonConfig.id) {
            this.props.updateDistributionJob(jsonBody);
        } else {
            this.props.saveDistributionJob(jsonBody);
        }
    }

    buildJsonBody() {
        const channelSpecific = this.props.getParentConfiguration();
        const configuration = Object.assign({}, this.state.commonConfig, this.state.providerConfig, channelSpecific);
        return JSON.stringify(configuration);
    }

    handleTestSubmit(event) {
        this.setState({
            fieldErrors: {}
        });

        if (event) {
            event.preventDefault();
        }

        const jsonBody = this.buildJsonBody();
        this.props.testDistributionJob(jsonBody);
    }

    createChangeHandler(fieldModelKey) {
        return (event) => {
            const { target } = event;
            const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
            const fieldModel = this.state[fieldModelKey];
            const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, target.name, value);
            this.setState({
                [fieldModelKey]: newState
            });
        };
    }

    createSingleSelectHandler(fieldKey, fieldModelKey) {
        return (selectedValue) => {
            if (selectedValue) {
                const selected = selectedValue.value;
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state[fieldModelKey], fieldKey, selected);
                this.setState({
                    [fieldModelKey]: newState
                });
            } else {
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state[fieldModelKey], fieldKey, null);
                this.setState({
                    [fieldModelKey]: newState
                });
            }
        };
    }

    createMultiSelectHandler(fieldKey, fieldModelKey) {
        return (selectedValues) => {
            if (selectedValues && selectedValues.length > 0) {
                const selected = selectedValues.map(item => item.value);
                const newState = FieldModelUtilities.updateFieldModelValues(this.state[fieldModelKey], fieldKey, selected);
                this.setState({
                    [fieldModelKey]: newState
                });
            } else {
                const newState = FieldModelUtilities.updateFieldModelValues(this.state[fieldModelKey], fieldKey, []);
                this.setState({
                    [fieldModelKey]: newState
                });
            }
        };
    }

    createProviderOptions() {
        const providers = DescriptorUtilities.findDescriptorByTypeAndContext(this.props.descriptors.items, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        if (providers) {
            const optionList = providers.map(descriptor => ({
                label: descriptor.label,
                value: descriptor.name,
                icon: descriptor.fontAwesomeIcon
            }));
            return optionList;
        }
        return [];
    }

    createNotificationTypeOptions() {
        const selectedProvider = FieldModelUtilities.getFieldModelSingleValue(this.state.commonConfig, KEY_PROVIDER_NAME);
        if (selectedProvider) {
            const [descriptor] = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors.items, selectedProvider, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const options = DescriptorUtilities.findDescriptorFieldOptions(descriptor, KEY_NOTIFICATION_TYPES);
            if (options) {
                const optionList = options.map(option => Object.assign({}, { label: option, value: option }));
                return optionList;
            }
        }
        return [];
    }

    createFormatTypeOptions() {
        const selectedProvider = FieldModelUtilities.getFieldModelSingleValue(this.state.commonConfig, KEY_PROVIDER_NAME);
        if (selectedProvider) {
            const [descriptor] = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors.items, selectedProvider, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const options = DescriptorUtilities.findDescriptorFieldOptions(descriptor, KEY_FORMAT_TYPE);
            if (options) {
                return options.map(option => Object.assign({}, { label: option, value: option }));
            }
        }
        return [];
    }

    createFrequencyOptions() {
        const [descriptor] = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors.items, this.props.alertChannelName, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const options = DescriptorUtilities.findDescriptorFieldOptions(descriptor, KEY_FREQUENCY);
        if (options) {
            return options.map(option => Object.assign({}, { label: option, value: option }));
        }
        return [];
    }


    renderDistributionForm() {
        const fieldModel = this.state.commonConfig;
        const providerFieldModel = this.state.providerConfig;
        const formatOptions = this.createFormatTypeOptions();
        const notificationOptions = this.createNotificationTypeOptions();
        const selectedFormatType = this.getSelectedSingleValue(formatOptions, fieldModel, KEY_FORMAT_TYPE);
        const selectedNotifications = this.getSelectedValues(notificationOptions, fieldModel, KEY_NOTIFICATION_TYPES);
        //
        // if (!FieldModelUtilities.hasFieldModelValues(fieldModel, KEY_FORMAT_TYPE)) {
        //     const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, KEY_FORMAT_TYPE, selectedFormatType);
        //     this.setState({
        //         commonConfig: newState
        //     });
        // }
        //
        // if (!FieldModelUtilities.hasFieldModelValues(fieldModel, KEY_NOTIFICATION_TYPES)) {
        //     const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, KEY_NOTIFICATION_TYPES, selectedNotifications);
        //     this.setState({
        //         commonConfig: newState
        //     });
        // }

        return (
            <div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Format</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_FORMAT_TYPE}
                            className="typeAheadField"
                            onChange={this.createSingleSelectHandler(KEY_FORMAT_TYPE, FIELD_MODEL_KEY.COMMON)}
                            removeSelected
                            options={formatOptions}
                            placeholder="Choose the format for the job"
                            value={selectedFormatType}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Notification Types</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_NOTIFICATION_TYPES}
                            className="typeAheadField"
                            onChange={this.createMultiSelectHandler(KEY_NOTIFICATION_TYPES, FIELD_MODEL_KEY.COMMON)}
                            isSearchable
                            isMulti
                            removeSelected
                            options={notificationOptions}
                            placeholder="Choose the notification types"
                            value={selectedNotifications}
                        />
                    </div>
                </div>
                {this.props.childContent}
                <ProjectConfiguration
                    includeAllProjects={!FieldModelUtilities.getFieldModelBooleanValue(providerFieldModel, KEY_FILTER_BY_PROJECT)}
                    handleChange={this.createChangeHandler(FIELD_MODEL_KEY.PROVIDER)}
                    handleProjectChanged={this.createMultiSelectHandler(KEY_CONFIGURED_PROJECT, FIELD_MODEL_KEY.PROVIDER)}
                    projects={this.props.projects}
                    configuredProjects={FieldModelUtilities.getFieldModelValues(providerFieldModel, KEY_CONFIGURED_PROJECT)}
                    projectNamePattern={FieldModelUtilities.getFieldModelSingleValue(providerFieldModel, KEY_PROJECT_NAME_PATTERN)}
                    fieldErrors={this.props.fieldErrors}
                />
                <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel} />
                <p name="configurationMessage">{this.state.configurationMessage}</p>
            </div>
        );
    }

    render() {
        const providers = this.createProviderOptions();
        const frequencyOptions = this.createFrequencyOptions();
        const fieldModel = this.state.commonConfig;
        const selectedProviderOption = this.getSelectedSingleValue(providers, fieldModel, KEY_PROVIDER_NAME);
        const selectedFrequencyOption = this.getSelectedSingleValue(frequencyOptions, fieldModel, KEY_FREQUENCY);

        // if (!FieldModelUtilities.hasFieldModelValues(fieldModel, KEY_PROVIDER_NAME)) {
        //     const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, KEY_NOTIFICATION_TYPES, selectedProviderOption);
        //     this.setState({
        //         commonConfig: newState
        //     });
        // }
        //
        // if (!FieldModelUtilities.hasFieldModelValues(fieldModel, KEY_FREQUENCY)) {
        //     const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, KEY_NOTIFICATION_TYPES, selectedFrequencyOption);
        //     this.setState({
        //         commonConfig: newState
        //     });
        // }

        return (
            <form className="form-horizontal" onSubmit={this.onSubmit}>
                <TextInput
                    id={KEY_NAME}
                    label="Job Name"
                    name={KEY_NAME}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_NAME)}
                    onChange={this.createChangeHandler(FIELD_MODEL_KEY.COMMON)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_NAME)}
                    errorValue={this.props.fieldErrors[KEY_NAME]}
                />
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Frequency</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_FREQUENCY}
                            className="typeAheadField"
                            onChange={this.createSingleSelectHandler(KEY_FREQUENCY, FIELD_MODEL_KEY.COMMON)}
                            isSearchable
                            options={frequencyOptions}
                            placeholder="Choose the frequency"
                            value={selectedFrequencyOption}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Provider</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_PROVIDER_NAME}
                            className="typeAheadField"
                            onChange={this.createSingleSelectHandler(KEY_PROVIDER_NAME, FIELD_MODEL_KEY.COMMON)}
                            isSearchable
                            options={providers}
                            placeholder="Choose the provider"
                            value={selectedProviderOption}
                            components={{ Option: CustomProviderTypeOptionLabel, SingleValue: CustomProviderTypeLabel }}
                        />
                    </div>
                </div>
                {selectedProviderOption && this.renderDistributionForm(selectedProviderOption)}
            </form>
        );
    }
}

BaseJobConfiguration.propTypes = {
    testDistributionJob: PropTypes.func.isRequired,
    saveDistributionJob: PropTypes.func.isRequired,
    updateDistributionJob: PropTypes.func.isRequired,
    getDistributionDescriptor: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object),
    jobs: PropTypes.arrayOf(PropTypes.object),
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool,
    success: PropTypes.bool,
    testingConfig: PropTypes.bool,
    configurationMessage: PropTypes.string,
    fieldErrors: PropTypes.object,
    distributionConfigId: PropTypes.string,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    getParentConfiguration: PropTypes.func.isRequired,
    childContent: PropTypes.object.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    currentDistributionComponents: PropTypes.object,
    projects: PropTypes.arrayOf(PropTypes.any),
    commonConfig: PropTypes.object
};

BaseJobConfiguration.defaultProps = {
    descriptors: [],
    jobs: [],
    fetching: false,
    inProgress: false,
    success: false,
    testingConfig: false,
    configurationMessage: '',
    fieldErrors: {},
    distributionConfigId: null,
    currentDistributionComponents: null,
    projects: [],
    commonConfig: {}
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: id => dispatch(getDistributionJob(id)),
    saveDistributionJob: config => dispatch(saveDistributionJob(config)),
    updateDistributionJob: config => dispatch(updateDistributionJob(config)),
    testDistributionJob: config => dispatch(testDistributionJob(config)),
    getDistributionDescriptor: (provider, channel) => dispatch(getDistributionDescriptor(provider, channel))
});

const mapStateToProps = state => ({
    descriptors: state.descriptors,
    jobs: state.distributions.jobs,
    fetching: state.distributions.fetching,
    inProgress: state.distributions.inProgress,
    success: state.distributions.success,
    testingConfig: state.distributions.testingConfig,
    configurationMessage: state.distributions.configurationMessage,
    fieldErrors: state.distributions.error,
    currentDistributionComponents: state.descriptors.currentDistributionComponents
});

export default connect(mapStateToProps, mapDispatchToProps)(BaseJobConfiguration);
