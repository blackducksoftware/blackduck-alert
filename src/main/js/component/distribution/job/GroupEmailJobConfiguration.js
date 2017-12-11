'use strict';
import React from 'react';

import {fieldLabel, typeAheadField, fieldError} from '../../../../css/field.css';

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
        if (groups != null && groups != undefined && groups.length > 0) {
			var rawGroups = groups;
			for (var index in rawGroups) {
				groupOptions.push({
					label: rawGroups[index].name,
					id: rawGroups[index].url
				});
			}
            
		}
        this.state.groupOptions = groupOptions;
        this.state.groupValue = groups;
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
						  {errorDiv}
					</div>;
		var renderResult =  super.render(content);
		return renderResult;
	}
}
