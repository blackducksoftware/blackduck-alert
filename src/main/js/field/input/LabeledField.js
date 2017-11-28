'use strict';
import React from 'react';

import { fieldLabel, fieldError } from '../../../css/field.css';

export default class LabeledField extends React.Component {
	constructor(props) {
		super(props);
	}
	render(inputDiv) {
		let errorDiv = null;
		if (this.props.errorName && this.props.errorValue) {
			errorDiv = <p className={fieldError} name={this.props.errorName}>{this.props.errorValue}</p>;
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