import React from 'react';

import { numberInput } from '../../../css/field.css';
import LabeledField from '../LabeledField';

export default class NumberInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = 'form-control';
		}
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <div className="col-sm-3"><input type="number" readOnly className={inputClass} name={this.props.name} value={this.props.value} /></div>;
		} else {
			inputDiv = <div className="col-sm-3"><input type="number" className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} /></div>;
		}
		return (
				super.render(inputDiv)
		)
	}
}
