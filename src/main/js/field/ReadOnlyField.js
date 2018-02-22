import React from 'react';
import LabeledField from './LabeledField';

class ReadOnlyField extends LabeledField {
	constructor(props) {
		super(props);
	}

	render() {
        return (
            super.render(<div className="col-sm-8"><p className="form-control-static">{this.props.value}</p></div>)
        );
    }
}

export default ReadOnlyField;
