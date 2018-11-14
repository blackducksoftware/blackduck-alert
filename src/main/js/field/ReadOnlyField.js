import React from 'react';
import LabeledField from './LabeledField';

export default class ReadOnlyField extends LabeledField {
    render() {
        return (
            super.render(<div className="d-inline-flex p-2 col-sm-9"><p className="form-control-static">{this.props.value}</p></div>)
        );
    }
}
