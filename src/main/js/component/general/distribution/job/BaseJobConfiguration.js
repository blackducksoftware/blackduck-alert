import React, {Component} from 'react';
import Select from 'react-select-2';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import TextInput from '../../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';
import ConfigButtons from '../../../common/ConfigButtons';

import {frequencyOptions, notificationOptions} from '../../../../util/distribution-data';

import {getDistributionJob, saveDistributionJob, testDistributionJob, updateDistributionJob} from '../../../../store/actions/distributions';

class BaseJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            success: false,
            errors: {}
        };
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
        this.createProviderOptions = this.createProviderOptions.bind(this);
        this.buildJsonBody = this.buildJsonBody.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            const stateValues = {
                fetching: nextProps.fetching,
                inProgress: nextProps.inProgress,
                success: nextProps.success,
                configurationMessage: nextProps.configurationMessage,
                error: nextProps.error
            };

            if(nextProps.distributionConfigId) {

                const allProjectsSelected = (nextProps.jobs[nextProps.distributionConfigId].filterByProject == 'false');

                const newState = Object.assign({}, stateValues, {
                    id: nextProps.id,
                    distributionConfigId: nextProps.distributionConfigId,
                    name: nextProps.jobs[nextProps.distributionConfigId].name,
                    providerName: nextProps.jobs[nextProps.distributionConfigId].providerName,
                    distributionType: nextProps.jobs[nextProps.distributionConfigId].distributionType,
                    frequency: nextProps.jobs[nextProps.distributionConfigId].frequency,
                    includeAllProjects: {allProjectsSelected},
                    filterByProject: nextProps.jobs[nextProps.distributionConfigId].filterByProject,
                    notificationTypes: nextProps.jobs[nextProps.distributionConfigId].notificationTypes,
                    configuredProjects: nextProps.jobs[nextProps.distributionConfigId].configuredProjects
                });
                this.setState(newState);
            } else {
                    this.setState(stateValues);
            }
        }
    }

    onSubmit(event) {
        event.preventDefault();
        const {handleSaveBtnClick, handleCancel} = this.props;
        this.handleSubmit();
        if (handleSaveBtnClick && this.state.success) {
            handleSaveBtnClick(this.state);
        } else if (handleCancel && !handleSaveBtnClick) {
            handleCancel();
        }
    }

    handleSubmit(event) {
        this.setState({
            success: false,
            configurationMessage: 'Saving...',
            inProgress: true,
            errors: {}
        });
        if (event) {
            event.preventDefault();
        }
        const jsonBody = this.buildJsonBody();
        if(this.props.id) {
            this.props.updateDistributionJob(this.props.baseUrl, jsonBody);
        } else {
            this.props.saveDistributionJob(this.props.baseUrl, jsonBody);
        }
    }

    handleTestSubmit(event) {
        this.setState({
            configurationMessage: 'Testing...',
            inProgress: true,
            errors: {}
        });

        if (event) {
            event.preventDefault();
        }

        const jsonBody = this.buildJsonBody();
        this.props.testDistributionJob(this.props.testUrl, jsonBody);
    }

    buildJsonBody() {
        const configuration = Object.assign({}, this.state, this.props.getParentConfiguration());
        configuration.filterByProject = !configuration.includeAllProjects;
        configuration.includeAllProjects = null;
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

    handleErrorValues(name, value) {
        const {errors} = this.state;
        errors[name] = value;
        this.setState({
            errors
        });
    }

    handleSetState(name, value) {
        this.setState({
            [name]: value
        });
    }

    handleProviderChanged(option) {
        if(option) {
            this.handleStateValues('providerName',option.value);
        } else {
            this.handleStateValues('providerName', option);
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

    createProviderOptions() {
        const providers = this.props.descriptors.items['PROVIDER_CONFIG'];
        if(providers) {
            const optionList =  providers.map(descriptor => {
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

    render() {
        return (
            <form className="form-horizontal" onSubmit={this.onSubmit}>
                <TextInput id="jobName" label="Job Name" name="name" value={this.state.name} onChange={this.handleChange} errorName="nameError" errorValue={this.state.errors.nameError}/>
                <div className="form-group">
                    <label className="col-sm-3 control-label">Provider</label>
                    <div className="col-sm-8">
                    <Select
                    id="providerName"
                    className="typeAheadField"
                    onChange={this.handleProviderChanged}
                    searchable
                    options={this.createProviderOptions()}
                    placeholder="Choose the provider"
                    value={this.state.providerName}
                    />
                    {this.state.errors.providerNameError && <label className="fieldError" name="providerNameError">
                        {this.state.errors.providerNameError}
                    </label>}
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 control-label">Frequency</label>
                    <div className="col-sm-8">
                        <Select
                            id="jobFrequency"
                            className="typeAheadField"
                            onChange={this.handleFrequencyChanged}
                            searchable
                            options={frequencyOptions}
                            placeholder="Choose the frequency"
                            value={this.state.frequency}
                        />
                        {this.state.errors.frequencyError && <label className="fieldError" name="frequencyError">
                            {this.state.errors.frequencyError}
                        </label>}
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 control-label">Notification Types</label>
                    <div className="col-sm-8">
                        <Select
                            id="jobType"
                            className="typeAheadField"
                            onChange={this.handleNotificationChanged}
                            searchable
                            multi
                            removeSelected
                            options={notificationOptions}
                            placeholder="Choose the notification types"
                            value={this.state.notificationTypes}
                        />
                        {this.state.errors.notificationTypesError && <label className="fieldError" name="notificationTypesError">
                            {this.state.errors.notificationTypesError}
                        </label>}
                    </div>
                </div>
                {this.props.childContent}
                <ProjectConfiguration includeAllProjects={this.state.includeAllProjects} handleChange={this.handleChange} handleProjectChanged={this.handleProjectChanged} projects={this.props.projects}
                                      configuredProjects={this.state.configuredProjects}/>
                <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel}/>
                <p name="configurationMessage">{this.state.configurationMessage}</p>
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
    configurationMessage: PropTypes.string,
    error: PropTypes.object,
    distributionConfigId: PropTypes.string,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    getParentConfiguration: PropTypes.func.isRequired,
    childContent: PropTypes.object.isRequired
};

BaseJobConfiguration.defaultProps = {
    csrfToken: null,
    descriptor: {},
    jobs: {},
    baseUrl: '',
    testUrl: '',
    fetching: false,
    inProgress: false,
    success: false,
    configurationMessage: '',
    error: {},
    distributionConfigId: null
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url,id) => dispatch(getDistributionJob(url,id)),
    saveDistributionJob: (url,config) => dispatch(saveDistributionJob(url,config)),
    updateDistributionJob: (url,config) => dispatch(updateDistributionJob(url,config)),
    testDistributionJob: (url,config) => dispatch(testDistributionJob(url,config))
});

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    descriptors: state.descriptors,
    jobs: state.distributions.jobs,
    fetching: state.distributions.fetching,
    inProgress: state.distributions.inProgress,
    success: state.distributions.success,
    configurationMessage: state.distributions.configurationMessage,
    error: state.distributions.error,
});

export default connect(mapStateToProps, mapDispatchToProps)(BaseJobConfiguration);
