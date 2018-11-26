import React, {Component} from 'react';
import Select, {components} from 'react-select';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import TextInput from '../../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';
import ConfigButtons from '../../../common/ConfigButtons';

import {frequencyOptions} from '../../../../util/distribution-data';

import {getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob} from '../../../../store/actions/distributions';
import {getDistributionDescriptor} from '../../../../store/actions/descriptors';
import DescriptorOption from "../../../common/DescriptorOption";

const {Option, SingleValue} = components;

const CustomProviderTypeOptionLabel = (props) => (
    <Option {...props}>
        <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value}/>
    </Option>
);

const CustomProviderTypeLabel = (props) => (
    <SingleValue {...props}>
        <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value}/>
    </SingleValue>
);

class BaseJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            saving: false,
            success: false,
            error: {}
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
            if (nextProps.error.message || nextProps.testingConfig) {
                // If there are errors, we only want to update the error messaging. We do not want to clear out the User's changes
                this.setState({
                    error: nextProps.error,
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
                    error: nextProps.error ? nextProps.error : {},
                    providerOptions: providerOptions
                });

                if (nextProps.distributionConfigId) {
                    const jobConfig = nextProps.jobs[nextProps.distributionConfigId];
                    if (jobConfig) {
                        const readDescriptorDistribution = !this.state.providerName && jobConfig.providerName
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
                        includeAllProjects: jobConfig.filterByProject == 'false',
                        filterByProject: jobConfig.filterByProject,
                        notificationTypes: jobConfig.notificationTypes,
                        configuredProjects: jobConfig.configuredProjects,
                        projectNamePattern: jobConfig.projectNamePattern
                    });
                    this.setState(newState);
                } else {
                    if (null == this.state.includeAllProjects || undefined == this.state.includeAllProjects) {
                        this.setState({
                            includeAllProjects: true
                        });
                    }
                    if (!this.state.providerName && providerOptions.length == 1) {
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
        const {handleSaveBtnClick, handleCancel} = this.props;
        this.handleSubmit();
        if (handleCancel && !handleSaveBtnClick) {
            handleCancel();
        }
    }

    handleSubmit(event) {
        this.setState({
            saving: true,
            error: {}
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
            error: {}
        });

        if (event) {
            event.preventDefault();
        }

        const jsonBody = this.buildJsonBody();
        this.props.testDistributionJob(this.props.testUrl, jsonBody);
    }

    buildJsonBody() {
        const configuration = Object.assign({}, {
            id: this.state.id,
            distributionConfigId: this.state.distributionConfigId,
            name: this.state.name,
            providerName: this.state.providerName,
            distributionType: this.state.distributionType,
            frequency: this.state.frequency,
            formatType: this.state.formatType,
            filterByProject: !this.state.includeAllProjects,
            notificationTypes: this.state.notificationTypes,
            configuredProjects: this.state.configuredProjects,
            projectNamePattern: this.state.projectNamePattern
        }, this.props.getParentConfiguration());
        configuration.includeAllProjects = !configuration.filterByProject;
        if (configuration.notificationTypes && configuration.notificationTypes.length > 0) {
            configuration.notificationTypes = configuration.notificationTypes;
        } else {
            configuration.notificationTypes = null;
        }

        const jsonBody = JSON.stringify(configuration);
        return jsonBody;
    }

    handleChange({target}) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const {name} = target;
        this.handleStateValues(name, value);
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
            if (this.state.providerName != option.value) {
                this.handleStateValues('providerName', option.value);
                this.props.getDistributionDescriptor(option.value, this.props.alertChannelName);
            }
        } else {
            if (this.state.providerOptions.length > 1) {
                this.handleStateValues('providerName', option);
                this.props.getDistributionDescriptor('', this.props.alertChannelName);
            }
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
        const providers = this.props.descriptors.items['PROVIDER_CONFIG'];
        if (providers) {
            const optionList = providers.map(descriptor => {
                return {
                    label: descriptor.label,
                    value: descriptor.descriptorName,
                    icon: descriptor.fontAwesomeIcon
                }
            });
            return optionList;
        } else {
            return [];
        }
    }

    createNotificationTypeOptions() {
        const {fields} = this.props.currentDistributionComponents;
        if (fields) {
            const notificationTypeField = fields.filter(field => field.key === 'notificationTypes');
            const {options} = notificationTypeField[0];

            const optionList = options.map(option => Object.assign({}, {label: option, value: option}));
            return optionList;
        } else {
            return [];
        }
    }

    createFormatTypeOptions() {
        const {fields} = this.props.currentDistributionComponents;
        if (fields) {
            const formatTypeField = fields.filter(field => field.key === 'formatType');
            const {options} = formatTypeField[0];

            const optionList = options.map(option => Object.assign({}, {label: option, value: option}));
            return optionList;
        } else {
            return [];
        }
    }

    renderDistributionForm() {
        if (!this.props.currentDistributionComponents) {
            return null;
        } else {
            const formatOptions = this.createFormatTypeOptions();
            const notificationOptions = this.createNotificationTypeOptions();
            var configuredNotificationOptions = null;
            if (this.state.notificationTypes) {
                configuredNotificationOptions = notificationOptions.filter(option => this.state.notificationTypes.indexOf(option.value) !== -1);
            }
            return (
                <div>
                    <div className="form-group">
                        <label className="col-sm-3 col-form-label text-right">Format</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <Select
                                id="formatType"
                                className="typeAheadField"
                                onChange={this.handleFormatChanged}
                                removeSelected
                                options={formatOptions}
                                placeholder="Choose the format for the job"
                                value={formatOptions.find(option => option.value === this.state.formatType)}
                            />
                            {this.state.error.formatTypeError && <label className="fieldError" name="formatTypeError">
                                {this.state.error.formatTypeError}
                            </label>}
                        </div>
                    </div>
                    <div className="form-group">
                        <label className="col-sm-3 col-form-label text-right">Notification Types</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <Select
                                id="jobType"
                                className="typeAheadField"
                                onChange={this.handleNotificationChanged}
                                isSearchable={true}
                                isMulti={true}
                                removeSelected
                                options={notificationOptions}
                                placeholder="Choose the notification types"
                                value={configuredNotificationOptions}
                            />
                            {this.state.error.notificationTypesError && <label className="fieldError" name="notificationTypesError">
                                {this.state.error.notificationTypesError}
                            </label>}
                        </div>
                    </div>
                    {this.props.childContent}
                    <ProjectConfiguration includeAllProjects={this.state.includeAllProjects} handleChange={this.handleChange} handleProjectChanged={this.handleProjectChanged} projects={this.props.projects}
                                          configuredProjects={this.state.configuredProjects} projectNamePattern={this.state.projectNamePattern}/>
                    <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel}/>
                    <p name="configurationMessage">{this.state.configurationMessage}</p>
                </div>
            );
        }
    }

    render() {
        const providerOptions = this.state.providerOptions;
        var selectedProviderOption = null
        if (providerOptions) {
            selectedProviderOption = providerOptions.find(option => option.value === this.state.providerName)
        }
        return (
            <form className="form-horizontal" onSubmit={this.onSubmit}>
                <TextInput id="name" label="Job Name" name="name" value={this.state.name} onChange={this.handleChange} errorName="nameError" errorValue={this.state.error.nameError}/>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Frequency</label>
                    <div className="d-inline-flex p-2 col-sm-9">
                        <Select
                            id="jobFrequency"
                            className="typeAheadField"
                            onChange={this.handleFrequencyChanged}
                            isSearchable={true}
                            options={frequencyOptions}
                            placeholder="Choose the frequency"
                            value={frequencyOptions.find(option => option.value === this.state.frequency)}
                        />
                        {this.state.error.frequencyError && <label className="fieldError" name="frequencyError">
                            {this.state.error.frequencyError}
                        </label>}
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Provider</label>
                    <div className="d-inline-flex p-2 col-sm-9">
                        <Select
                            id="providerName"
                            className="typeAheadField"
                            onChange={this.handleProviderChanged}
                            isSearchable={true}
                            options={providerOptions}
                            placeholder="Choose the provider"
                            value={selectedProviderOption}
                            components={{Option: CustomProviderTypeOptionLabel, SingleValue: CustomProviderTypeLabel}}
                        />
                        {this.state.error.providerNameError && <label className="fieldError" name="providerNameError">
                            {this.state.error.providerNameError}
                        </label>}
                    </div>
                </div>
                {this.renderDistributionForm()}
            </form>
        );
    }
}

BaseJobConfiguration.propTypes = {
    csrfToken: PropTypes.string,
    descriptors: PropTypes.object,
    jobs: PropTypes.object,
    baseUrl: PropTypes.string.isRequired,
    testUrl: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool,
    success: PropTypes.bool,
    testingConfig: PropTypes.bool,
    configurationMessage: PropTypes.string,
    error: PropTypes.object,
    distributionConfigId: PropTypes.string,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    getParentConfiguration: PropTypes.func.isRequired,
    childContent: PropTypes.object.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    currentDistributionComponents: PropTypes.object
};

BaseJobConfiguration.defaultProps = {
    csrfToken: null,
    descriptors: {},
    jobs: {},
    baseUrl: '',
    testUrl: '',
    fetching: false,
    inProgress: false,
    success: false,
    testingConfig: false,
    configurationMessage: '',
    error: {},
    distributionConfigId: null,
    currentDistributionComponents: null
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id)),
    saveDistributionJob: (url, config) => dispatch(saveDistributionJob(url, config)),
    updateDistributionJob: (url, config) => dispatch(updateDistributionJob(url, config)),
    testDistributionJob: (url, config) => dispatch(testDistributionJob(url, config)),
    getDistributionDescriptor: (provider, channel) => dispatch(getDistributionDescriptor(provider, channel))
});

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    descriptors: state.descriptors,
    jobs: state.distributions.jobs,
    fetching: state.distributions.fetching,
    inProgress: state.distributions.inProgress,
    success: state.distributions.success,
    testingConfig: state.distributions.testingConfig,
    configurationMessage: state.distributions.configurationMessage,
    error: state.distributions.error,
    currentDistributionComponents: state.descriptors.currentDistributionComponents
});

export default connect(mapStateToProps, mapDispatchToProps)(BaseJobConfiguration);
