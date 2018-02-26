import React from 'react';
import LabeledField from '../LabeledField';

export default class TextInput extends LabeledField {
	render() {
		let placeholderText = null;
		if (this.props.isSet) {
			placeholderText = '********';
		}
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = 'form-control';
		}

		return (
			super.render(<div className="col-sm-8">
				<input type="text" readOnly={this.props.readOnly} autoFocus={this.props.autoFocus} className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} placeholder={placeholderText} />
			</div>)
		);
	}
}
