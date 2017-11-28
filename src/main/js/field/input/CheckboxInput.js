import React from 'react';

import { checkboxInput } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class CheckboxInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <input type="checkbox" readOnly disabled="disabled" className={checkboxInput} name={this.props.name} checked={this.props.isChecked} onChange={this.props.onChange} />;
		} else {
			inputDiv = <input type="checkbox" className={checkboxInput} name={this.props.name} checked={this.props.isChecked} onChange={this.props.onChange} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}