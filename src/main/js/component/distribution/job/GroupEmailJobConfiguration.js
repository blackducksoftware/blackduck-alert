'use strict';
import React from 'react';

import {fieldLabel, typeAheadField, fieldError, inline} from '../../../../css/field.css';

import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class GroupEmailJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
		this.handleGroupsChanged = this.handleGroupsChanged.bind(this);
	}

	handleGroupsChanged (optionsList) {
		super.handleStateValues('groupValue', optionsList);
	}

    initializeValues() {
        super.initializeValues();
        const { groups, selectedGroups } = this.props;
        let groupOptions= new Array();
        if (groups && groups.length > 0) {
			let rawGroups = groups;
			for (var index in rawGroups) {
				groupOptions.push({
					label: rawGroups[index].name
				});
			}
		} else {
            if(selectedGroups) {
                let rawGroups = selectedGroups;
                for (var index in rawGroups) {
    				groupOptions.push({
    					label: rawGroups[index]
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
            this.state.groupValue = groupValueArray;
        }
    }

	render() {
        const { groupOptions } = this.state;
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
						<Typeahead className={typeAheadField}
							onChange={this.handleGroupsChanged}
						    clearButton
						    options={options}
						    placeholder='Choose the Hub user group'
						    selected={this.state.groupValue}
						  />
						  {progressIndicator}
						  {errorDiv}
					</div>;
		var renderResult =  super.render(content);
		return renderResult;
	}
}
