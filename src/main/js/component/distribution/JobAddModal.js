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
				{ label: 'Group Email', value: 'email_group_channel'},
				{ label: 'Slack', value: 'slack_channel' },
				{ label: 'HipChat', value: 'hipchat_channel' }
			]
		}
		this.handleChange = this.handleChange.bind(this);
		this.handleTypeChanged = this.handleTypeChanged.bind(this);
		this.getCurrentJobConfig = this.getCurrentJobConfig.bind(this);
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
	}

	handleSaveBtnClick(values) {
		const { columns, onSave } = this.props;
	    // You should call onSave function and give the new row
	    onSave(values);
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
    		values['typeValue'] = option.value;
    		this.setState({
    			values
    		});
        }
	}

	getCurrentJobConfig() {
		var currentJobConfig = null;
		let typeValue = this.state.values.typeValue;
		if (typeValue) {
			if (typeValue === 'email_group_channel') {
				currentJobConfig = <GroupEmailJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForGroups={this.props.waitingForGroups} groups={this.props.groups}  waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} handleSaveBtnClick={this.handleSaveBtnClick} groupError={this.props.groupError} projectTableMessage={this.props.projectTableMessage} />;
			} else if (typeValue === 'hipchat_channel') {
				currentJobConfig = <HipChatJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} handleSaveBtnClick={this.handleSaveBtnClick} projectTableMessage={this.props.projectTableMessage} />;
			} else if (typeValue === 'slack_channel') {
				currentJobConfig = <SlackJobConfiguration buttonsFixed={false} includeAllProjects={this.props.includeAllProjects} waitingForProjects={this.props.waitingForProjects} projects={this.props.projects} handleCancel={this.props.onModalClose} handleSaveBtnClick={this.handleSaveBtnClick} projectTableMessage={this.props.projectTableMessage} />;
			}
		}
		return currentJobConfig;
	}

	renderOption(option) {
		var fontAwesomeIcon = "";
		if (option.value === 'email_group_channel') {
			fontAwesomeIcon = 'fa fa-envelope';
		} else if (option.value === 'hipchat_channel') {
			fontAwesomeIcon = 'fa fa-comments';
		} else if (option.value === 'slack_channel') {
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
						<ConfigButtons isFixed={false} includeCancel={true} includeSave={false} onCancelClick={this.props.onModalClose} />
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
						    value={this.state.values.typeValue}
                            valueRenderer={this.renderOptions}
						  />
				</div>
				{content}
			</div>
		)
	}
}
