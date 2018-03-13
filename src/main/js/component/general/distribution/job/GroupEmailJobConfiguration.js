import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select-2';
import TextInput from '../../../../field/input/TextInput';

import BaseJobConfiguration from './BaseJobConfiguration';

class GroupEmailJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
		this.handleGroupsChanged = this.handleGroupsChanged.bind(this);
	}

	handleGroupsChanged (optionsList) {
        if (optionsList) {
            super.handleStateValues('groupName', optionsList.value);
        } else {
            super.handleStateValues('groupName', null);
        }
	}

    initializeValues(data) {
        super.initializeValues(data);
        let groupName = data.groupName || this.props.groupName;
        let emailSubjectLine = data.emailSubjectLine || this.props.emailSubjectLine;
        const { groups } = this.props;
        let groupOptions= new Array();
        if (groups && groups.length > 0) {
			let rawGroups = groups;
			for (var index in rawGroups) {
				groupOptions.push({
					label: rawGroups[index].name,
                    value: rawGroups[index].name,
                    missing: false
				});
			}


            let groupFound = groupOptions.find((group) => {
                return group.name === groupName;
            });

            if(!groupFound) {
                groupOptions.push({
                    label: groupName,
                    value: groupName,
                    missing: true
                });
            }


            groupOptions.sort((group1, group2) => {
                if(group1.value < group2.value) {
                    return -1;
                } else if (group1.value > group2.value) {
                    return 1;
                } else {
                    return 0;
                }
            });
		} else {
            if(groupName) {
				groupOptions.push({
					label: groupName,
                    value: groupName,
                    missing: true
				});
            }
        }
        this.state.groupOptions = groupOptions;
        super.handleStateValues('groupName', groupName);
        super.handleStateValues('emailSubjectLine', emailSubjectLine);

    }

    renderOption(option) {
        if(option.missing) {
            return (
                <span className="missingHubData"><span className="fa fa-exclamation-triangle fa-fw" aria-hidden='true'></span>{option.label}</span>
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
					<div>
                        <TextInput label="Subject Line" name="emailSubjectLine" value={this.state.values.emailSubjectLine} onChange={this.handleChange} errorName="emailSubjectLineError" errorValue={this.props.emailSubjectLineError}></TextInput>

                        <div className="form-group">
                            <label className="col-sm-3 control-label">Group</label>
                            <div className="col-sm-8">
                                <Select className="typeAheadField"
                                    onChange={this.handleGroupsChanged}
                                    clearble={true}
                                    options={options}
                                    optionRenderer={this.renderOption}
                                    placeholder='Choose the Hub user group'
                                    value={groupName}
                                    valueRenderer={this.renderOption}
                                    searchable={true}
                                  />
                            </div>
                        </div>
						  { this.props.waitingForGroups && <div className="inline">
                              <span className="fa fa-spinner fa-pulse fa-fw" aria-hidden></span>
                          </div> }

						  { this.props.groupError && <p className="fieldError" name="groupError">
                              { this.props.groupError }
                          </p> }
					</div>;
		return super.render(content);
	}
};

GroupEmailJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
		csrfToken: PropTypes.string
};

GroupEmailJobConfiguration.defaultProps = {
    baseUrl: '/api/configuration/distribution/emailGroup',
    testUrl: '/api/configuration/distribution/emailGroup/test',
    distributionType: 'email_group_channel'
};

export default GroupEmailJobConfiguration;
