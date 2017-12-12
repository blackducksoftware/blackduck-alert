'use strict';
import React, { Component } from 'react';

import { fieldLabel, fieldError } from '../../../css/field.css';

class LabeledField extends Component {
	constructor(props) {
		super(props);
	}
	render(inputDiv) {
		let labelClass = this.props.labelClass;
		if (!labelClass) {
			labelClass = fieldLabel;
		}
		let errorDiv = null;
		if (this.props.errorName && this.props.errorValue) {
			errorDiv = <p className={fieldError} name={this.props.errorName}>{this.props.errorValue}</p>;
		}
		return (
				<div>
					<label className={labelClass}>{this.props.label}</label>
					{inputDiv}
					{errorDiv}
				</div>
		)
	}
};

export default LabeledField;
