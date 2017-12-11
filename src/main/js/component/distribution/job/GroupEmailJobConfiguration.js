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

	render() {		
		var groupOptions= new Array();

		if (this.props.groups != null && this.props.groups != undefined && this.props.groups.length > 0) {
			var rawGroups = this.props.groups;
			for (var index in rawGroups) {
				groupOptions.push({
					label: rawGroups[index].name,
					id: rawGroups[index].url
				});
			}
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
						    options={groupOptions}
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
