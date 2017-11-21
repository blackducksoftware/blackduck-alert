import React from 'react';

import CheckboxInput from './input/CheckboxInput';
import TextInput from './input/TextInput';
import PasswordInput from './input/PasswordInput';
import NumberInput from './input/NumberInput';

import { fieldLabel, fieldError } from '../../css/main.css';

export default class Field extends React.Component {
	constructor(props) {
		super(props);
	}

	render() {
		let inputDiv = null;
		let errorDiv = null;
		if (this.props.errorName && this.props.errorValue) {
			errorDiv = <p className={fieldError} name={this.props.errorName}>{this.props.errorValue}</p>;
		}
		switch (this.props.type) {
			case "password":
				inputDiv = <PasswordInput name={this.props.name} onChange={this.props.onChange} />;
				break;
			case "checkbox":
				inputDiv = <CheckboxInput name={this.props.name} isChecked={this.props.value} onChange={this.props.onChange} />;
				break;
			case "number":
				inputDiv = <NumberInput name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
				break;
			default:
				inputDiv = <TextInput name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		}
		return (
				<div>
					<label className={fieldLabel}>{this.props.label}</label>
					{inputDiv}
					{errorDiv}
				</div>
		)
	}
}