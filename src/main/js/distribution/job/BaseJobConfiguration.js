import React, { Component } from 'react';
import { components } from 'react-select';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TextInput from 'field/input/TextInput';
import ProjectConfiguration from 'distribution/ProjectConfiguration';
import ConfigButtons from 'component/common/ConfigButtons';

import { saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributionConfigs';
import DescriptorOption from 'component/common/DescriptorOption';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import SelectInput from 'field/input/SelectInput';

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

// provider configuration
const KEY_NOTIFICATION_TYPES = 'provider.distribution.notification.types';
const KEY_FORMAT_TYPE = 'provider.distribution.format.type';
const KEY_FILTER_BY_PROJECT = 'channel.common.filter.by.project';
const KEY_PROJECT_NAME_PATTERN = 'channel.common.project.name.pattern';
const KEY_CONFIGURED_PROJECT = 'channel.common.configured.project';

const fieldDescriptions = {
    [KEY_FORMAT_TYPE]: 'Select the format of the message that will be created.',
    [KEY_NOTIFICATION_TYPES]: 'Select one or more of the notification types. Only these notification types will be included for this distribution job.',
    [KEY_NAME]: 'The name of the distribution job. Must be unique.',
    [KEY_FREQUENCY]: 'Select how frequently this job should check for notifications to send.',
    [KEY_PROVIDER_NAME]: 'Select the provider. Only notifications for that provider will be processed in this distribution job.'
};

const fieldNames = [
    KEY_NAME,
    KEY_CHANNEL_NAME,
    KEY_PROVIDER_NAME,
    KEY_FREQUENCY
];

const providerFieldNames = [
    KEY_NOTIFICATION_TYPES,
    KEY_FORMAT_TYPE,
    KEY_FILTER_BY_PROJECT,
    KEY_PROJECT_NAME_PATTERN,
    KEY_CONFIGURED_PROJECT
];

const FIELD_MODEL_KEY = {
    COMMON: 'commonConfig',
    PROVIDER: 'providerConfig'
};

class BaseJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            fieldErrors: {},
            commonConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, this.props.alertChannelName),
            providerConfig: FieldModelUtilities.createEmptyFieldModel(providerFieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, null)
        };
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
        this.updateProviderModel = this.updateProviderModel.bind(this);

        const channelModel = FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, this.props.alertChannelName);

        let providerModel = FieldModelUtilities.createEmptyFieldModel(providerFieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, null);
        providerModel = this.updateProviderModel(FieldModelUtilities.getFieldModelSingleValue(channelModel, KEY_PROVIDER_NAME), providerModel);

        this.state = {
            fieldErrors: {},
            commonConfig: channelModel,
            providerConfig: providerModel
        };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.saving && nextProps.success) {
            this.props.handleSaveBtnClick(this.state);
            return;
        }
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (nextProps.fieldErrors.message || nextProps.testingConfig) {
                // If there are errors, we only want to update the error messaging. We do not want to clear out the User's changes
                this.setState({
                    fieldErrors: nextProps.fieldErrors,
                    configurationMessage: nextProps.configurationMessage
                });
            }
            let channelModel = this.state.commonConfig;
            let providerModel = this.state.providerConfig;
            if (!channelModel) {
                channelModel = FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, this.props.alertChannelName);
            }
            if (!providerModel) {
                providerModel = FieldModelUtilities.createEmptyFieldModel(providerFieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, null);
            }
            const { job } = nextProps;
            if (job && job.fieldModels) {
                channelModel = nextProps.job.fieldModels.find(model => model.descriptorName.startsWith('channel_'));
                providerModel = nextProps.job.fieldModels.find(model => model.descriptorName.startsWith('provider_'));
            }

            providerModel = this.updateProviderModel(FieldModelUtilities.getFieldModelSingleValue(channelModel, KEY_PROVIDER_NAME), providerModel);

            this.setState({
                fieldErrors: {},
                jobId: nextProps.job.jobId,
                commonConfig: channelModel,
                providerConfig: providerModel
            });
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
        return null;
    }

    updateProviderModel(selectedProvider, currentProviderModel) {
        let providerModel = currentProviderModel;
        const formatOptions = this.createFormatTypeOptions(selectedProvider);
        const notificationOptions = this.createNotificationTypeOptions(selectedProvider);
        const selectedFormatType = this.getSelectedSingleValue(formatOptions, providerModel, KEY_FORMAT_TYPE);
        const selectedNotifications = this.getSelectedValues(notificationOptions, providerModel, KEY_NOTIFICATION_TYPES);
        const filterByProject = FieldModelUtilities.getFieldModelBooleanValue(providerModel, KEY_FILTER_BY_PROJECT);

        if (!FieldModelUtilities.hasFieldModelValues(providerModel, KEY_FORMAT_TYPE) && selectedFormatType) {
            providerModel = FieldModelUtilities.updateFieldModelSingleValue(providerModel, KEY_FORMAT_TYPE, selectedFormatType.value);
        }

        if (!FieldModelUtilities.hasFieldModelValues(providerModel, KEY_NOTIFICATION_TYPES)) {
            providerModel = FieldModelUtilities.updateFieldModelValues(providerModel, KEY_NOTIFICATION_TYPES, selectedNotifications);
        }

        if (!FieldModelUtilities.hasFieldModelValues(providerModel, KEY_FILTER_BY_PROJECT) && filterByProject) {
            providerModel = FieldModelUtilities.updateFieldModelSingleValue(providerModel, KEY_FILTER_BY_PROJECT, filterByProject.toString());
        }
        return providerModel;
    }

    handleSubmit(event) {
        this.setState({
            fieldErrors: {}
        });
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
        const channelSpecific = this.props.getParentConfiguration();
        const channelName = this.state.commonConfig.descriptorName;
        const providerName = FieldModelUtilities.getFieldModelSingleValue(this.state.commonConfig, KEY_PROVIDER_NAME);
        const emptyProviderModel = FieldModelUtilities.createEmptyFieldModel(providerFieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, providerName);
        const updatedProviderFieldModel = FieldModelUtilities.combineFieldModels(emptyProviderModel, this.state.providerConfig);
        const commonFieldModel = FieldModelUtilities.updateFieldModelSingleValue(this.state.commonConfig, KEY_CHANNEL_NAME, channelName);
        const updatedChannelFieldModel = FieldModelUtilities.combineFieldModels(commonFieldModel, channelSpecific);
        const configuration = Object.assign({}, {
            jobId: this.state.jobId,
            fieldModels: [
                updatedChannelFieldModel,
                updatedProviderFieldModel
            ]
        });
        return configuration;
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

    createChangeHandler(fieldModelKey, negateCheckboxValue) {
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
            const fieldModel = this.state[fieldModelKey];
            const newState = FieldModelUtilities.updateFieldModelSingleValue(fieldModel, target.name, targetValue);
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
        const providers = DescriptorUtilities.findDescriptorByTypeAndContext(this.props.descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
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

    createNotificationTypeOptions(selectedProvider) {
        if (selectedProvider) {
            const [descriptor] = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, selectedProvider, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const options = DescriptorUtilities.findDescriptorFieldOptions(descriptor, KEY_NOTIFICATION_TYPES);
            if (options) {
                const optionList = options.map(option => Object.assign({}, { label: option, value: option }));
                return optionList;
            }
        }
        return [];
    }

    createFormatTypeOptions(selectedProvider) {
        if (selectedProvider) {
            const [descriptor] = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, selectedProvider, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            const options = DescriptorUtilities.findDescriptorFieldOptions(descriptor, KEY_FORMAT_TYPE);
            if (options) {
                return options.map(option => Object.assign({}, { label: option, value: option }));
            }
        }
        return [];
    }

    createFrequencyOptions() {
        const [descriptor] = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, this.props.alertChannelName, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const options = DescriptorUtilities.findDescriptorFieldOptions(descriptor, KEY_FREQUENCY);
        if (options) {
            return options.map((option) => {
                if (option === 'DAILY') {
                    return Object.assign({}, { label: 'Daily', value: option });
                } else if (option === 'REAL_TIME') {
                    return Object.assign({}, { label: 'Real Time', value: option });
                }
                return Object.assign({}, { label: option, value: option });
            });
        }
        return [];
    }


    renderDistributionForm(selectedProvider) {
        const providerFieldModel = this.state.providerConfig;
        const formatOptions = this.createFormatTypeOptions(selectedProvider);
        const notificationOptions = this.createNotificationTypeOptions(selectedProvider);
        const selectedFormatType = this.getSelectedSingleValue(formatOptions, providerFieldModel, KEY_FORMAT_TYPE);
        const selectedNotifications = this.getSelectedValues(notificationOptions, providerFieldModel, KEY_NOTIFICATION_TYPES);
        const filterByProject = FieldModelUtilities.getFieldModelBooleanValue(providerFieldModel, KEY_FILTER_BY_PROJECT);
        const includeAllProjects = !filterByProject;
        return (
            <div>
                <SelectInput
                    label="Format"
                    onChange={this.createSingleSelectHandler(KEY_FORMAT_TYPE, FIELD_MODEL_KEY.PROVIDER)}
                    id={KEY_FORMAT_TYPE}
                    inputClass="typeAheadField"
                    labelClass="col-sm-3"
                    selectSpacingClass="col-sm-8"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_FORMAT_TYPE)}
                    options={formatOptions}
                    isSearchable={false}
                    removeSelected
                    placeholder="Choose the format for the job"
                    value={selectedFormatType}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_FORMAT_TYPE)}
                    errorValue={this.props.fieldErrors[KEY_FORMAT_TYPE]}
                />

                <SelectInput
                    label="Notification Types"
                    onChange={this.createMultiSelectHandler(KEY_NOTIFICATION_TYPES, FIELD_MODEL_KEY.PROVIDER)}
                    id={KEY_NOTIFICATION_TYPES}
                    inputClass="typeAheadField"
                    labelClass="col-sm-3"
                    selectSpacingClass="col-sm-8"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_NOTIFICATION_TYPES)}
                    options={notificationOptions}
                    isSearchable
                    removeSelected
                    hasMultipleValues
                    placeholder="Choose the notification types"
                    value={selectedNotifications}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_NOTIFICATION_TYPES)}
                    errorValue={this.props.fieldErrors[KEY_NOTIFICATION_TYPES]}
                />
                {this.props.childContent}
                <ProjectConfiguration
                    providerName={FieldModelUtilities.getFieldModelSingleValue(this.state.commonConfig, KEY_PROVIDER_NAME)}
                    includeAllProjects={includeAllProjects}
                    handleChange={this.createChangeHandler(FIELD_MODEL_KEY.PROVIDER, true)}
                    handleProjectChanged={this.createMultiSelectHandler(KEY_CONFIGURED_PROJECT, FIELD_MODEL_KEY.PROVIDER)}
                    projects={this.props.projects}
                    configuredProjects={FieldModelUtilities.getFieldModelValues(providerFieldModel, KEY_CONFIGURED_PROJECT)}
                    projectNamePattern={FieldModelUtilities.getFieldModelSingleValueOrDefault(providerFieldModel, KEY_PROJECT_NAME_PATTERN, '')}
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
        return (
            <form className="form-horizontal" onSubmit={this.onSubmit} noValidate={true}>
                <TextInput
                    id={KEY_NAME}
                    label="Job Name"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_NAME)}
                    name={KEY_NAME}
                    value={FieldModelUtilities.getFieldModelSingleValueOrDefault(fieldModel, KEY_NAME, '')}
                    onChange={this.createChangeHandler(FIELD_MODEL_KEY.COMMON)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_NAME)}
                    errorValue={this.props.fieldErrors[KEY_NAME]}
                />
                <SelectInput
                    label="Frequency"
                    onChange={this.createSingleSelectHandler(KEY_FREQUENCY, FIELD_MODEL_KEY.COMMON)}
                    id={KEY_FREQUENCY}
                    inputClass="typeAheadField"
                    labelClass="col-sm-3"
                    selectSpacingClass="col-sm-8"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_FREQUENCY)}
                    options={frequencyOptions}
                    isSearchable
                    placeholder="Choose the frequency"
                    value={selectedFrequencyOption}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_FREQUENCY)}
                    errorValue={this.props.fieldErrors[KEY_FREQUENCY]}
                />

                <SelectInput
                    label="Provider"
                    onChange={this.createSingleSelectHandler(KEY_PROVIDER_NAME, FIELD_MODEL_KEY.COMMON)}
                    id={KEY_PROVIDER_NAME}
                    inputClass="typeAheadField"
                    labelClass="col-sm-3"
                    selectSpacingClass="col-sm-8"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PROVIDER_NAME)}
                    options={providers}
                    isSearchable
                    placeholder="Choose the provider"
                    value={selectedProviderOption}
                    components={{ Option: CustomProviderTypeOptionLabel, SingleValue: CustomProviderTypeLabel }}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROVIDER_NAME)}
                    errorValue={this.props.fieldErrors[KEY_PROVIDER_NAME]}
                />
                {selectedProviderOption && this.renderDistributionForm(selectedProviderOption.value)}
            </form>
        );
    }
}

BaseJobConfiguration.propTypes = {
    testDistributionJob: PropTypes.func.isRequired,
    saveDistributionJob: PropTypes.func.isRequired,
    updateDistributionJob: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    job: PropTypes.object.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool,
    saving: PropTypes.bool,
    success: PropTypes.bool,
    testingConfig: PropTypes.bool,
    configurationMessage: PropTypes.string,
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    getParentConfiguration: PropTypes.func.isRequired,
    childContent: PropTypes.object.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    projects: PropTypes.arrayOf(PropTypes.any)
};

BaseJobConfiguration.defaultProps = {
    job: {},
    fetching: false,
    inProgress: false,
    saving: false,
    success: false,
    testingConfig: false,
    configurationMessage: '',
    fieldErrors: {},
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

export default connect(mapStateToProps, mapDispatchToProps)(BaseJobConfiguration);
