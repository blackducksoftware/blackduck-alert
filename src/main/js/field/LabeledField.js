'use strict';
import React, { Component } from 'react';

import { fieldLabel, fieldError } from '../../css/field.css';

class LabeledField extends Component {
	constructor(props) {
		super(props);
	}
	render(inputDiv) {
		let errorDiv = null;
		if (this.props.errorName && this.props.errorValue) {
			errorDiv = <p className={fieldError} name={this.props.errorName}>{this.props.errorValue}</p>;
		}
		var field = inputDiv;
		if (!inputDiv) {
			field = this.props.field;
		}

		return (
				<div className="form-group">
					<label className="col-sm-4 control-label">{this.props.label}</label>
					{field}
                    <div className="col-sm-offset-4 col-sm-8">
						{errorDiv}
					</div>
				</div>
		)
	}
};

export default LabeledField;
