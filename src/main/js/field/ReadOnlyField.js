import React from 'react';
import { textInput } from '../../css/field.css';
import LabeledField from './LabeledField';

class ReadOnlyField extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
        const { value } = this.props;
        let textDiv = <label className={textInput}>{value}</label>;
        return (
            super.render(textDiv)
        );
    }
}

export default ReadOnlyField;
