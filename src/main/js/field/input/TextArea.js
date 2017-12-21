import React from 'react';

import { textInput, fieldLabelTextArea } from '../../../css/field.css';
import LabeledField from './LabeledField';

export default class TextArea extends LabeledField {
	constructor(props) {
		super(props);

	}

	render() {
		let inputClass = this.props.inputClass;
		if (!inputClass) {
			inputClass = textInput;
		}
		let inputDiv = null;
		if (this.props.readOnly) {
			inputDiv = <textarea rows="8" cols="60" readOnly className={inputClass} name={this.props.name} value={this.props.value} />;
		} else {
			inputDiv = <textarea rows="8" cols="60" className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />;
		}
		return (
				super.render(inputDiv)
		)
	}
}

TextArea.defaultProps = {
    labelClass: fieldLabelTextArea
};
