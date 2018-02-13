import React from 'react';
import { textInput } from '../../css/field.css';
import LabeledField from './LabeledField';

class ReadOnlyField extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
        const { value } = this.props;
        const textDiv = <div className="col-sm-8"><p className={`form-control-static ${textInput}`}>{value}</p></div>;
        return (
            super.render(textDiv)
        );
    }
}

export default ReadOnlyField;
