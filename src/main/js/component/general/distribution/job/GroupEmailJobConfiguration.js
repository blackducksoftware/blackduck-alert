import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Select from 'react-select-2';
import TextInput from '../../../../field/input/TextInput';
import BaseJobConfiguration from './BaseJobConfiguration';

class GroupEmailJobConfiguration extends BaseJobConfiguration {
    constructor(props) {
        super(props);
        this.handleGroupsChanged = this.handleGroupsChanged.bind(this);
    }

    handleGroupsChanged(optionsList) {
        if (optionsList) {
            super.handleStateValues('groupName', optionsList.value);
        } else {
            super.handleStateValues('groupName', null);
        }
    }

    initializeValues(data) {
        super.initializeValues(data);
        const groupName = data.groupName || this.props.groupName;
        const emailSubjectLine = data.emailSubjectLine || this.props.emailSubjectLine;

        const groupOptions = this.props.groups.map(group => ({
            label: group.name,
            value: group.name,
            missing: false
        })).sort((group1, group2) => {
            if (group1.value < group2.value) {
                return -1;
            } else if (group1.value > group2.value) {
                return 1;
            }
            return 0;
        });

        const groupFound = groupOptions.find(group => group.label === groupName);
        if (groupName && (!groupFound || groupOptions.length === 0)) {
            groupOptions.push({
                label: groupName,
                value: groupName,
                missing: true
            });
        }

        this.state.groupOptions = groupOptions;
        super.handleStateValues('groupName', groupName);
        super.handleStateValues('emailSubjectLine', emailSubjectLine);
    }

    renderOption(option) {
        if (option.missing) {
            return (
                <span className="missingHubData"><span className="fa fa-exclamation-triangle fa-fw" aria-hidden="true" />{option.label} (Group not found on Hub)</span>
            );
        }

        return (
            <span>{option.label}</span>
        );
    }

    render() {
        const { groupOptions } = this.state;
        const { groupName } = this.state.values;
        const options = groupOptions || [];
        const content =
            (<div>
                <TextInput id="emailJob-subject" label="Subject Line" name="emailSubjectLine" value={this.state.values.emailSubjectLine} onChange={this.handleChange} errorName="emailSubjectLineError" errorValue={this.props.emailSubjectLineError} />

                <div className="form-group">
                    <label className="col-sm-3 control-label">Group</label>
                    <div className="col-sm-8">
                        <Select
                            id="emailJob-group"
                            className="typeAheadField"
                            onChange={this.handleGroupsChanged}
                            clearable
                            options={options}
                            optionRenderer={this.renderOption}
                            placeholder="Choose the Hub user group"
                            value={groupName}
                            valueRenderer={this.renderOption}
                            searchable
                        />

                        { this.state.errors.groupNameError && <label className="fieldError" name="groupError">
                            { this.state.errors.groupNameError }
                        </label> }
                    </div>
                </div>
                { this.props.waitingForGroups && <div className="inline">
                    <span className="fa fa-spinner fa-pulse fa-fw" aria-hidden />
                </div> }
            </div>);
        return super.render(content);
    }
}

GroupEmailJobConfiguration.defaultProps = {
    groups: []
};

GroupEmailJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    csrfToken: PropTypes.string,
    groups: PropTypes.arrayOf(PropTypes.object)
};

GroupEmailJobConfiguration.defaultProps = {
    baseUrl: '/alert/api/configuration/distribution/emailGroup',
    testUrl: '/alert/api/configuration/distribution/emailGroup/test',
    distributionType: 'email_group_channel'
};

export default GroupEmailJobConfiguration;
