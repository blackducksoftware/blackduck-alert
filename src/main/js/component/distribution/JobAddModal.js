import React, { Component } from 'react';

import TextInput from '../../field/input/TextInput';

import {modalContainer} from '../../../css/main.css';
import {fieldLabel, typeAheadField} from '../../../css/field.css';
import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead, Token} from 'react-bootstrap-typeahead';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';

import ConfigButtons from '../ConfigButtons'

export default class JobAddModal extends Component {
	constructor(props) {
		super(props);
		this.state = {
			values: [],
			errors: [],
            typeOptions: [
				{ label: 'Group Email', id: 'Group Email'},
				{ label: 'Slack', id: 'Slack' },
				{ label: 'HipChat', id: 'HipChat' }
			]
		}
		this.getFieldValue = this.getFieldValue.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.handleTypeChanged = this.handleTypeChanged.bind(this);
		this.getCurrentJobConfig = this.getCurrentJobConfig.bind(this);
	}

	handleSaveBtnClick() {
		// TODO persist to the backend
	    const { columns, onSave } = this.props;
	    const newRow = {};
	    columns.forEach((column, i) => {
	      newRow[column.field] = this.refs[column.field].value;
	    }, this);
	    // You should call onSave function and give the new row
	    onSave(newRow);
	 }

	getFieldValue() {
	    const newRow = {};
	    this.props.columns.forEach((column, i) => {
	    	var value = values[column.field];
	    	if (value == null || value == undefined) {
	    		newRow[column.field] ='';
	    	} else {
	    		newRow[column.field] = value;
	    	}
	    }, this);
    	return newRow;
  	}

	handleChange(event) {
		const target = event.target;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		const name = target.name;

		var values = this.state.values;
		values[name] = value;
		this.setState({
			values
		});
	}

	handleTypeChanged (optionsList) {
		var option = null;
		if (optionsList.length > 0) {
			option = optionsList[0];
		}
		var values = this.state.values;
		values['typeValues'] = optionsList;
		this.setState({
			values
		});
	}

	getCurrentJobConfig() {
		var currentJobConfig = null;
		let typeValues = this.state.values.typeValues;
		if (typeValues != null && typeValues.length > 0) {
			var type = typeValues[0];
			if (type.id === 'Group Email') {
				currentJobConfig = <GroupEmailJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForGroups={this.props.waitingForGroups} groups={this.props.groups}  waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} groupError={this.props.groupError} projectTableMessage={this.props.projectTableMessage} />;
			} else if (type.id === 'HipChat') {
				currentJobConfig = <HipChatJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} projectTableMessage={this.props.projectTableMessage} />;
			} else if (type.id === 'Slack') {
				currentJobConfig = <SlackJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} projectTableMessage={this.props.projectTableMessage} />;
			}
		}
		return currentJobConfig;
	}

	renderCustomJobTypeTokenDropDown(option, props, index) {
		var fontAwesomeIcon = "";
		if (option.id === 'Group Email') {
			fontAwesomeIcon = 'fa fa-envelope';
		} else if (option.id === 'HipChat') {
			fontAwesomeIcon = 'fa fa-comments';
		} else if (option.id === 'Slack') {
			fontAwesomeIcon = 'fa fa-slack';
		}
	    return (<div>
	    			<i key="icon" className={fontAwesomeIcon} aria-hidden='true'></i>
			    	<strong key="name"> {option.label} </strong>
		      	</div>
	    );
	  }

	render() {
		const containerClasses = `modal-content react-bs-table-insert-modal ${modalContainer}`;
		var content = <div>
						<ConfigButtons isFixed={false} includeCancel={true} onCancelClick={this.props.onModalClose} onClick={this.props.onModalClose} />
					</div>;
		
		var currentJobConfig = this.getCurrentJobConfig();
		if (currentJobConfig != null) {
			content = currentJobConfig;
		}
		return (
			<div className={containerClasses}>
				<div>
					<label className={fieldLabel}>Type</label>
					<Typeahead
							renderMenuItemChildren={this.renderCustomJobTypeTokenDropDown}
							className={typeAheadField}
							onChange={this.handleTypeChanged}
						    clearButton
						    options={this.state.typeOptions}
						    placeholder='Choose the Job Type'
						    selected={this.state.values.typeValues}
						  />
				</div>
				{content}
			</div>
		)
	}
}