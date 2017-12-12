import React from 'react';

import { checkboxInput } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class CheckboxInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = checkboxInput;
		}

		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <input type="checkbox" readOnly disabled="disabled" className={inputClass} name={this.props.name} checked={this.props.value} onChange={this.props.onChange} />;
		} else {
			inputDiv = <input type="checkbox" className={inputClass} name={this.props.name} checked={this.props.value} onChange={this.props.onChange} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}