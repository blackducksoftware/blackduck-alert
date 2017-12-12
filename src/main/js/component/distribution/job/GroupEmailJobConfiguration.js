'use strict';
import React from 'react';

import { missingHubData } from '../../../../css/main.css';
import {fieldLabel, typeAheadField, fieldError, inline} from '../../../../css/field.css';

import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class GroupEmailJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
		this.handleGroupsChanged = this.handleGroupsChanged.bind(this);
	}

	handleGroupsChanged (optionsList) {
		super.handleStateValues('groupValue', optionsList.value);
	}

    initializeValues() {
        super.initializeValues();
        const { groups, selectedGroups } = this.props;
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

            for(var index in selectedGroups) {
                let groupFound = groupOptions.find((group) => {
                    return group.name === selectedGroups[index];
                });

                if(!groupFound) {
                    groupOptions.push({
                        label: selectedGroups[index],
                        value: selectedGroups[index],
                        missing: true
                    });
                }
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
            if(selectedGroups) {
                let rawGroups = selectedGroups;
                for (var index in rawGroups) {
    				groupOptions.push({
    					label: rawGroups[index],
                        value: rawGroups[index],
                        missing: true
    				});
    			}
            }
        }
        this.state.groupOptions = groupOptions;

        let groupValueArray = groupOptions.filter((option) => {
            if(selectedGroups){
                let includes = selectedGroups.includes(option.label);
                return includes;
            } else {
                return false;
            }
        });

        if(groupValueArray) {
            super.handleStateValues('groupValue', groupValueArray[0].value);
        }
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
					</div>;
		var renderResult =  super.render(content);
		return renderResult;
	}
}
