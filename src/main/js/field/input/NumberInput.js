import React from 'react';

import { numberInput } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class NumberInput extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <input type="number" readOnly className={numberInput} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		} else {
			inputDiv = <input type="number" className={numberInput} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}