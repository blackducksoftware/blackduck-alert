import React from 'react';

import { numberInput } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class NumberInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = numberInput;
		}
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <input type="number" readOnly className={inputClass} name={this.props.name} value={this.props.value} />;
		} else {
			inputDiv = <input type="number" className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}