import React from 'react';

import { textInput } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class TextInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = textInput;
		}
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <input type="text" readOnly className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		} else {
			inputDiv = <input type="text" className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}