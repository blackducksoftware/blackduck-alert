import React, { Component } from 'react';

import TextInput from '../../field/input/TextInput';

import {modalContainer} from '../../../css/main.css';
import {fieldLabel, typeAheadField} from '../../../css/field.css';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

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
				{ label: 'Group Email', value: 'Group Email'},
				{ label: 'Slack', value: 'Slack' },
				{ label: 'HipChat', value: 'HipChat' }
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

	handleTypeChanged (option) {
		var values = this.state.values;
        if(option) {
    		values['typeValues'] = option.value;
    		this.setState({
    			values
    		});
        }
	}

	getCurrentJobConfig() {
		var currentJobConfig = null;
		let typeValues = this.state.values.typeValues;
		if (typeValues != null && typeValues.length > 0) {
			var type = typeValues[0];
			if (type.value === 'Group Email') {
				currentJobConfig = <GroupEmailJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForGroups={this.props.waitingForGroups} groups={this.props.groups}  waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} groupError={this.props.groupError} projectTableMessage={this.props.projectTableMessage} />;
			} else if (type.value === 'HipChat') {
				currentJobConfig = <HipChatJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} projectTableMessage={this.props.projectTableMessage} />;
			} else if (type.value === 'Slack') {
				currentJobConfig = <SlackJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} projectTableMessage={this.props.projectTableMessage} />;
			}
		}
		return currentJobConfig;
	}

	renderOption(option) {
		var fontAwesomeIcon = "";
		if (option.value === 'Group Email') {
			fontAwesomeIcon = 'fa fa-envelope';
		} else if (option.value === 'HipChat') {
			fontAwesomeIcon = 'fa fa-comments';
		} else if (option.value === 'Slack') {
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
					<Select
							className={typeAheadField}
							onChange={this.handleTypeChanged}
						    clearble={true}
						    options={this.state.typeOptions}
                            optionRenderer={this.renderOption}
						    placeholder='Choose the Job Type'
						    value={this.state.values.typeValues}
                            valueRenderer={this.renderOptions}
						  />
				</div>
				{content}
			</div>
		)
	}
}
