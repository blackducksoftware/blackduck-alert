import React, { Component } from 'react';
import Select, { components } from 'react-select';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TextInput from 'field/input/TextInput';
import ProjectConfiguration from 'distribution/ProjectConfiguration';
import ConfigButtons from 'component/common/ConfigButtons';

import { frequencyOptions } from 'util/distribution-data';

import { getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob } from 'store/actions/distributions';
import { getDistributionDescriptor } from 'store/actions/descriptors';
import DescriptorOption from 'component/common/DescriptorOption';
import * as FieldModelUtil from 'util/fieldModelUtilities';
import * as DescriptorUtil from 'util/descriptorUtilities';

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


const KEY_NAME = "channel.common.name";
const KEY_CHANNEL_NAME = "channel.common.channel.name";
const KEY_PROVIDER_NAME = "channel.common.provider.name";
const KEY_FREQUENCY = "channel.common.frequency";
const KEY_NOTIFICATION_TYPES = "provider.distribution.notification.types";
const KEY_FORMAT_TYPE = "provider.distribution.format.type";

const fieldNames = [
    KEY_NAME,
    KEY_CHANNEL_NAME,
    KEY_PROVIDER_NAME,
    KEY_FREQUENCY,
    KEY_NOTIFICATION_TYPES,
    KEY_FORMAT_TYPE
];

class BaseJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            saving: false,
            success: false,
            fieldErrors: {},
            currentConfig: {}
        };
        this.loading = false;
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.handleFrequencyChanged = this.handleFrequencyChanged.bind(this);
        this.handleNotificationChanged = this.handleNotificationChanged.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleProjectChanged = this.handleProjectChanged.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTestSubmit = this.handleTestSubmit.bind(this);
        this.handleProviderChanged = this.handleProviderChanged.bind(this);
        this.handleFormatChanged = this.handleFormatChanged.bind(this);
        this.createProviderOptions = this.createProviderOptions.bind(this);
        this.createNotificationTypeOptions = this.createNotificationTypeOptions.bind(this);
        this.createFormatTypeOptions = this.createFormatTypeOptions.bind(this);
        this.buildJsonBody = this.buildJsonBody.bind(this);
        this.renderDistributionForm = this.renderDistributionForm.bind(this);
    }

    componentDidMount() {
        this.loading = true;
    }

    componentWillReceiveProps(nextProps) {
        if (this.state.saving) {
            this.setState({
                saving: false
            });
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
                const providerOptions = this.createProviderOptions();
                const stateValues = Object.assign({}, this.state, {
                    fetching: nextProps.fetching,
                    inProgress: nextProps.inProgress,
                    success: nextProps.success,
                    configurationMessage: nextProps.configurationMessage,
                    fieldErrors: nextProps.fieldErrors ? nextProps.fieldErrors : {},
                    providerOptions
                });

                if (nextProps.distributionConfigId) {
                    const jobConfig = nextProps.jobs[nextProps.distributionConfigId];
                    if (jobConfig) {
                        const readDescriptorDistribution = !this.state.providerName && jobConfig.providerName;
                        if (readDescriptorDistribution) {
                            nextProps.getDistributionDescriptor(jobConfig.providerName, nextProps.alertChannelName);
                        }
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
                    if (!this.state.providerName && providerOptions.length === 1) {
                        const providerSelection = providerOptions[0].value;
                        nextProps.getDistributionDescriptor(providerSelection, nextProps.alertChannelName);
                        this.setState({
                            providerName: providerSelection
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

    handleSubmit(event) {
        this.setState({
            saving: true,
            fieldErrors: {}
        });
        if (event) {
            event.preventDefault();
        }
        const jsonBody = this.buildJsonBody();
        if (this.state.id) {
            this.props.updateDistributionJob(this.props.baseUrl, jsonBody);
        } else {
            this.props.saveDistributionJob(this.props.baseUrl, jsonBody);
        }
    }

    handleTestSubmit(event) {
        this.setState({
            fieldErrors: {}
        });

        if (event) {
            event.preventDefault();
        }

        const jsonBody = this.buildJsonBody();
        this.props.testDistributionJob(this.props.testUrl, jsonBody);
    }

    buildJsonBody() {
        // const configuration = Object.assign({}, {
        //     id: this.state.id,
        //     distributionConfigId: this.state.distributionConfigId,
        //     name: this.state.name,
        //     providerName: this.state.providerName,
        //     distributionType: this.state.distributionType,
        //     frequency: this.state.frequency,
        //     formatType: this.state.formatType,
        //     filterByProject: !this.state.includeAllProjects,
        //     notificationTypes: this.state.notificationTypes,
        //     configuredProjects: this.state.configuredProjects,
        //     projectNamePattern: this.state.projectNamePattern
        // }, this.props.getParentConfiguration());
        // configuration.includeAllProjects = !configuration.filterByProject;
        // if (configuration.notificationTypes && configuration.notificationTypes.length > 0) {
        //     configuration.notificationTypes = configuration.notificationTypes;
        // } else {
        //     configuration.notificationTypes = null;
        // }

        const jsonBody = JSON.stringify(this.state.currentConfig);
        return jsonBody;
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtil.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);
        this.setState({
            currentConfig: newState
        });
    }

    handleStateValues(name, value) {
        this.setState({
            [name]: value
        });
    }

    handleSetState(name, value) {
        this.setState({
            [name]: value
        });
    }

    handleProviderChanged(option) {
        if (option) {
            if (this.state.providerName !== option.value) {
                this.handleStateValues('providerName', option.value);
                this.props.getDistributionDescriptor(option.value, this.props.alertChannelName);
            }
        } else if (this.state.providerOptions.length > 1) {
            this.handleStateValues('providerName', option);
            this.props.getDistributionDescriptor('', this.props.alertChannelName);
        }
    }

    handleFrequencyChanged(option) {
        if (option) {
            this.handleStateValues('frequency', option.value);
        } else {
            this.handleStateValues('frequency', option);
        }
    }

    handleNotificationChanged(selectedValues) {
        if (selectedValues && selectedValues.length > 0) {
            const selected = selectedValues.map(item => item.value);
            this.handleStateValues('notificationTypes', selected);
        } else {
            this.handleStateValues('notificationTypes', []);
        }
    }

    handleProjectChanged(selectedValues) {
        if (selectedValues && selectedValues.length > 0) {
            this.handleStateValues('configuredProjects', selectedValues);
        } else {
            this.handleStateValues('configuredProjects', []);
        }
    }

    handleFormatChanged(option) {
        if (option) {
            this.handleStateValues('formatType', option.value);
        } else {
            this.handleStateValues('formatType', option);
        }
    }

    createProviderOptions() {
        const providers = DescriptorUtil.findDescriptorByTypeAndContext(this.props.descriptors.items, DescriptorUtil.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtil.CONTEXT_TYPE.GLOBAL);
        if (providers) {
            const optionList = providers.map(descriptor => ({
                label: descriptor.label,
                value: descriptor.descriptorName,
                icon: descriptor.fontAwesomeIcon
            }));
            return optionList;
        }
        return [];
    }

    createNotificationTypeOptions() {
        const { fields } = this.props.currentDistributionComponents;
        if (fields) {
            const notificationTypeField = fields.filter(field => field.key === 'notificationTypes');
            const { options } = notificationTypeField[0];

            const optionList = options.map(option => Object.assign({}, { label: option, value: option }));
            return optionList;
        }
        return [];
    }

    createFormatTypeOptions() {
        const { fields } = this.props.currentDistributionComponents;
        if (fields) {
            const formatTypeField = fields.filter(field => field.key === 'formatType');
            const { options } = formatTypeField[0];

            const optionList = options.map(option => Object.assign({}, { label: option, value: option }));
            return optionList;
        }
        return [];
    }

    renderDistributionForm() {
        if (!this.props.currentDistributionComponents) {
            return null;
        }
        const formatOptions = this.createFormatTypeOptions();
        const notificationOptions = this.createNotificationTypeOptions();
        let configuredNotificationOptions = null;
        if (this.state.notificationTypes) {
            configuredNotificationOptions = notificationOptions.filter(option => this.state.notificationTypes.indexOf(option.value) !== -1);
        }
        return (
            <div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Format</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_FORMAT_TYPE}
                            className="typeAheadField"
                            onChange={this.handleFormatChanged}
                            removeSelected
                            options={formatOptions}
                            placeholder="Choose the format for the job"
                            value={formatOptions.find((option) => {
                                const selectedOption = FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_FORMAT_TYPE);
                                return option.value === selectedOption;
                            })}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Notification Types</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_NOTIFICATION_TYPES}
                            className="typeAheadField"
                            onChange={this.handleNotificationChanged()}
                            isSearchable
                            isMulti
                            removeSelected
                            options={notificationOptions}
                            placeholder="Choose the notification types"
                            value={configuredNotificationOptions}
                        />
                    </div>
                </div>
                {this.props.childContent}
                <ProjectConfiguration
                    includeAllProjects={this.state.includeAllProjects}
                    handleChange={this.handleChange}
                    handleProjectChanged={this.handleProjectChanged}
                    projects={this.props.projects}
                    configuredProjects={this.state.configuredProjects}
                    projectNamePattern={this.state.projectNamePattern}
                />
                <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel} />
                <p name="configurationMessage">{this.state.configurationMessage}</p>
            </div>
        );
    }

    render() {
        const [providerOptions] = this.state;
        const fieldModel = this.state.currentConfig;
        let selectedProviderOption = null;
        if (providerOptions) {
            selectedProviderOption = providerOptions.find(option => option.value === this.state.providerName);
        }
        return (
            <form className="form-horizontal" onSubmit={this.onSubmit}>
                <TextInput
                    id={KEY_NAME}
                    label="Job Name"
                    name={KEY_NAME}
                    value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_NAME)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtil.createFieldModelErrorKey(KEY_NAME)}
                    errorValue={this.props.fieldErrors[KEY_NAME]}
                />
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Frequency</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_FREQUENCY}
                            className="typeAheadField"
                            onChange={this.handleFrequencyChanged}
                            isSearchable
                            options={frequencyOptions}
                            placeholder="Choose the frequency"
                            value={frequencyOptions.find((option) => {
                                const selectedOption = FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_FREQUENCY);
                                return option.value === selectedOption;
                            })}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Provider</label>
                    <div className="d-inline-flex flex-column p-2 col-sm-9">
                        <Select
                            id={KEY_PROVIDER_NAME}
                            className="typeAheadField"
                            onChange={this.handleProviderChanged()}
                            isSearchable
                            options={providerOptions}
                            placeholder="Choose the provider"
                            value={selectedProviderOption}
                            components={{ Option: CustomProviderTypeOptionLabel, SingleValue: CustomProviderTypeLabel }}
                        />
                    </div>
                </div>
                {this.renderDistributionForm()}
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
    jobs: PropTypes.object,
    baseUrl: PropTypes.string.isRequired,
    testUrl: PropTypes.string.isRequired,
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
    currentConfig: PropTypes.object
};

BaseJobConfiguration.defaultProps = {
    descriptors: [],
    jobs: {},
    baseUrl: '',
    testUrl: '',
    fetching: false,
    inProgress: false,
    success: false,
    testingConfig: false,
    configurationMessage: '',
    fieldErrors: {},
    distributionConfigId: null,
    currentDistributionComponents: null,
    projects: [],
    currentConfig: {}
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id)),
    saveDistributionJob: (url, config) => dispatch(saveDistributionJob(url, config)),
    updateDistributionJob: (url, config) => dispatch(updateDistributionJob(url, config)),
    testDistributionJob: (url, config) => dispatch(testDistributionJob(url, config)),
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
