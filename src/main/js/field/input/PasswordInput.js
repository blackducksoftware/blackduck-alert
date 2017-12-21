import React from 'react';

import { textInput } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class PasswordInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let placeholderText = null;
		if (this.props.isSet) {
			placeholderText = '***********';
		} 
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = textInput;
		}
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <input type="password" readOnly className={inputClass} name={this.props.name} value={this.props.value} placeholder={placeholderText} />;
		} else {
			inputDiv = <input type="password" className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} placeholder={placeholderText} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}