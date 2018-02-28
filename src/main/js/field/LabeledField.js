import React, { Component } from 'react';

class LabeledField extends Component {
	render(inputDiv) {
		const field = inputDiv || this.props.field;
		return (
			<div className="form-group">
				<label className="col-sm-3 control-label">{this.props.label}</label>

				{field}

				{ this.props.errorMessage && <div className="col-sm-offset-3 col-sm-8">
					<p className="fieldError">{this.props.errorMessage}</p>
				</div> }
			</div>
		)
	}
};

export default LabeledField;
