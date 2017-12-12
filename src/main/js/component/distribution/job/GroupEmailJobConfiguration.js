'use strict';
import React from 'react';

import { missingHubData } from '../../../../css/main.css';
import {fieldLabel, typeAheadField, fieldError, inline} from '../../../../css/field.css';
import TextInput from '../../../field/input/TextInput';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class GroupEmailJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
		this.handleGroupsChanged = this.handleGroupsChanged.bind(this);
	}

	handleGroupsChanged (optionsList) {
        if (optionsList) {
            super.handleStateValues('groupValue', optionsList.value);
        } else {
            super.handleStateValues('groupValue', null);
        }
	}

    initializeValues() {
        super.initializeValues();
        const { groups, selectedGroup } = this.props;
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
                return group.name === selectedGroup;
            });

            if(!groupFound) {
                groupOptions.push({
                    label: selectedGroup,
                    value: selectedGroup,
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
            if(selectedGroup) {
				groupOptions.push({
					label: selectedGroup,
                    value: selectedGroup,
                    missing: true
				});
            }
        }
        this.state.groupOptions = groupOptions;
        super.handleStateValues('groupValue', selectedGroup);
    
    }

    renderOption(option) {
        let classAttribute;
        if(option.missing) {
            return (
                <span className={missingHubData}>{option.label}</span>
            );
        } else {
            return (
                <span>{option.label}</span>
            );
        }
    }

	render() {
        const { groupOptions } = this.state;
        const groupValue = this.state.values['groupValue'];
        let options;
        if(groupOptions) {
            options = groupOptions;
        } else {
            options = new Array();
        }

		let errorDiv = null;
		if (this.props.groupError) {
			errorDiv = <p className={fieldError} name="groupError">{this.props.groupError}</p>;
		}

		var progressIndicator = null;
		if (this.props.waitingForGroups) {
        	const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
			progressIndicator = <div className={inline}>
									<i className={fontAwesomeIcon} aria-hidden='true'></i>
								</div>;
		}
		let content =
					<div>
						<label className={fieldLabel}>Group</label>
						<Select className={typeAheadField}
							onChange={this.handleGroupsChanged}
						    clearble={true}
						    options={options}
                            optionRenderer={this.renderOption}
						    placeholder='Choose the Hub user group'
						    value={groupValue}
                            valueRenderer={this.renderOption}
                            searchable={true}
						  />
						  {progressIndicator}
						  {errorDiv}
						  <TextInput label="Test Email Address" name="testEmailAddress" value={this.props.testEmailAddress} onChange={this.handleChange} errorName="testEmailAddressError" errorValue={this.props.testEmailAddressError}></TextInput>
					</div>;
		var renderResult =  super.render(content);
		return renderResult;
	}
}
