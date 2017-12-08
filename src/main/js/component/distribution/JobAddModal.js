import React, { Component } from 'react';

import TextInput from '../../field/input/TextInput';

import {modalContainer} from '../../../css/main.css';
import {fieldLabel, typeAheadField} from '../../../css/field.css';
import 'react-bootstrap-typeahead/css/Typeahead.css';
import {Typeahead} from 'react-bootstrap-typeahead';

import ConfigButtons from '../ConfigButtons'

export default class JobAddModal extends Component {
	constructor(props) {
		super(props);
		this.state = {
			values: [],
            typeOptions: [
				{ label: 'Group Email', id: 'GROUP_EMAIL'},
				{ label: 'Slack', id: 'SLACK' },
				{ label: 'HipCHat', id: 'HIPCAHT' }
			]
		}
		this.getFieldValue = this.getFieldValue.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.handleTypeChanged = this.handleTypeChanged.bind(this);
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
		values['typeValue'] = option;
		this.setState({
			values
		});
	}

	render() {
		const containerClasses = `modal-content react-bs-table-insert-modal ${modalContainer}`;
		return (
			<div className={containerClasses}>
				<TextInput label="Job Name" name="jobName" value={this.state.values.jobName} onChange={this.handleChange} errorName="jobNameError" errorValue={this.state.values.jobName}></TextInput>
				<div>
					<label className={fieldLabel}>Type</label>
					<Typeahead className={typeAheadField}
							onChange={this.handleTypeChanged}
						    clearButton
						    options={this.state.typeOptions}
						    placeholder='Choose the Job Type'
						    selected={this.state.values.typeValue}
						  />
					<ConfigButtons isFixed="false" includeCancel='true' onCancelClick={this.props.onModalClose} onClick={this.props.onModalClose} />
				</div>
			</div>
		)
	}
}