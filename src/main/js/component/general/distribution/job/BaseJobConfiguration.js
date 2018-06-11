import React, { Component } from 'react';
import { connect } from 'react-redux';
import Select from 'react-select-2';

import TextInput from '../../../../field/input/TextInput';
import ProjectConfiguration from '../ProjectConfiguration';
import ConfigButtons from '../../../common/ConfigButtons';

import { frequencyOptions, notificationOptions } from '../../../../util/distribution-data';

class BaseJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            success: false,
            values: {},
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
    }

    componentDidMount() {
        const { distributionConfigId } = this.props;
        this.readDistributionJobConfiguration(distributionConfigId);
    }

    async onSubmit(event) {
        event.preventDefault();
        const { handleSaveBtnClick, handleCancel } = this.props;
        await this.handleSubmit();
        if (handleSaveBtnClick && this.state.success) {
            handleSaveBtnClick(this.state.values);
        } else if (handleCancel && !handleSaveBtnClick) {
            handleCancel();
        }
    }

    initializeValues(data) {
        const {
            id, distributionConfigId, name, distributionType, frequency, notificationTypes, includeAllProjects, filterByProject, projects, configuredProjects
        } = data;

        const { values } = this.state;
        values.id = id;
        values.distributionConfigId = distributionConfigId;
        values.name = name;
        values.distributionType = distributionType;
        const frequencyFound = frequencyOptions.find(option => option.value === frequency);

        if (frequencyFound) {
            values.frequency = frequencyFound.value;
        }
        if (includeAllProjects) {
            values.includeAllProjects = includeAllProjects === true || includeAllProjects === 'true';
        } else if (filterByProject) {
            values.includeAllProjects = (filterByProject == 'false');
        }
        if (notificationTypes) {
            values.notificationTypes = notificationTypes;
        }

        values.configuredProjects = configuredProjects;

        this.setState({ values });
    }

    async handleSubmit(event) {
        this.setState({
            success: false,
            configurationMessage: 'Saving...',
            inProgress: true,
            errors: {}
        });
        if (event) {
            event.preventDefault();
        }

        const configuration = Object.assign({}, this.state.values);
        configuration.filterByProject = !configuration.includeAllProjects;
        configuration.includeAllProjects = null;
        if (configuration.notificationTypes && configuration.notificationTypes.length > 0) {
            configuration.notificationTypes = configuration.notificationTypes;
        } else {
            configuration.notificationTypes = null;
        }

        const self = this;
        const jsonBody = JSON.stringify(configuration);
        const method = this.state.values.id ? 'PUT' : 'POST';

        return fetch(this.props.baseUrl, {
            method,
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': this.props.csrfToken
            },
            body: jsonBody
        }).then((response) => {
            self.setState({
                inProgress: false
            });
            if (response.ok) {
                return response.json().then((json) => {
                    self.setState({
                        success: true,
                        configurationMessage: json.message
                    });
                });
            }
            return response.json().then((json) => {
                const jsonErrors = json.errors;
                if (jsonErrors) {
                    const errors = {};
                    for (const key in jsonErrors) {
                        if (jsonErrors.hasOwnProperty(key)) {
                            const name = key.concat('Error');
                            const value = jsonErrors[key];
                            errors[name] = value;
                        }
                    }
                    self.setState({
                        errors,
                        configurationMessage: json.message
                    });
                } else {
                    self.setState({
                        configurationMessage: json.error
                    });
                }
            });
        }).catch(console.error);
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

        const configuration = Object.assign({}, this.state.values);
        configuration.filterByProject = !configuration.includeAllProjects;
        configuration.includeAllProjects = null;
        if (configuration.notificationTypes && configuration.notificationTypes.length > 0) {
            configuration.notificationTypes = configuration.notificationTypes;
        } else {
            configuration.notificationTypes = null;
        }

        const jsonBody = JSON.stringify(configuration);

        fetch(this.props.testUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': this.props.csrfToken
            },
            body: jsonBody
        }).then((response) => {
            this.setState({
                inProgress: false
            });
            return response.json().then((json) => {
                const jsonErrors = json.errors;
                if (jsonErrors) {
                    const errors = {};
                    for (const key in jsonErrors) {
                        if (jsonErrors.hasOwnProperty(key)) {
                            const name = key.concat('Error');
                            const value = jsonErrors[key];
                            errors[name] = value;
                        }
                    }
                    this.setState({
                        errors
                    });
                }
                this.setState({
                    configurationMessage: json.message
                });
            });
        })
            .catch((error) => {
                console.log(error);
            });
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const { name } = target;
        this.handleStateValues(name, value);
    }

    handleStateValues(name, value) {
        const { values } = this.state;
        values[name] = value;
        this.setState({
            values
        });
    }

    handleErrorValues(name, value) {
        const { errors } = this.state;
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

    readDistributionJobConfiguration(distributionId) {
        if (distributionId) {
            const urlString = this.props.getUrl || this.props.baseUrl;
            const getUrl = `${urlString}?id=${distributionId}`;
            const self = this;
            fetch(getUrl, {
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then((response) => {
                if (response.ok) {
                    response.json().then((jsonArray) => {
                        if (jsonArray && jsonArray.length > 0) {
                            self.initializeValues(jsonArray[0]);
                        } else {
                            self.initializeValues(self.props);
                        }
                    });
                } else {
                    self.initializeValues(self.props);
                }
            }).catch(console.error);
        } else {
            this.initializeValues(this.props);
        }
    }

    render(content) {
        return (
            <form className="form-horizontal" onSubmit={this.onSubmit}>
                <TextInput id="job-name" label="Job Name" name="name" value={this.state.values.name} onChange={this.handleChange} errorName="nameError" errorValue={this.state.errors.nameError} />
                <div className="form-group">
                    <label className="col-sm-3 control-label">Frequency</label>
                    <div className="col-sm-8">
                        <Select
                            id="job-frequency"
                            className="typeAheadField"
                            onChange={this.handleFrequencyChanged}
                            searchable
                            options={frequencyOptions}
                            placeholder="Choose the frequency"
                            value={this.state.values.frequency}
                        />
                        { this.state.errors.frequencyError && <label className="fieldError" name="frequencyError">
                            { this.state.errors.frequencyError }
                        </label> }
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-sm-3 control-label">Notification Types</label>
                    <div className="col-sm-8">
                        <Select
                            id="job-notificationType"
                            className="typeAheadField"
                            onChange={this.handleNotificationChanged}
                            searchable
                            multi
                            removeSelected
                            options={notificationOptions}
                            placeholder="Choose the notification types"
                            value={this.state.values.notificationTypes}
                        />
                        { this.state.errors.notificationTypesError && <label className="fieldError" name="notificationTypesError">
                            { this.state.errors.notificationTypesError }
                        </label> }
                    </div>
                </div>
                {content}
                <ProjectConfiguration includeAllProjects={this.state.values.includeAllProjects} handleChange={this.handleChange} handleProjectChanged={this.handleProjectChanged} projects={this.props.projects} configuredProjects={this.state.values.configuredProjects} projectTableMessage={this.props.projectTableMessage} />
                <ConfigButtons cancelId="job-cancel" submitId="job-submit" includeTest includeCancel onTestClick={this.handleTestSubmit} onCancelClick={this.props.handleCancel} />
                <p name="configurationMessage">{this.state.configurationMessage}</p>
            </form>
        );
    }
}

export default BaseJobConfiguration;
