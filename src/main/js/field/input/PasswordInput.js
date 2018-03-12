import React from 'react';

import LabeledField from '../LabeledField';

export default class PasswordInput extends LabeledField {
    constructor(props) {
        super(props);
    }

    render() {
        let placeholderText = null;
        if (this.props.isSet) {
            placeholderText = '***********';
        }
        let inputClass = this.props.inputClass;
        if (!inputClass) {
            inputClass = 'form-control';
        }
        let inputDiv = null;
        if (this.props.readOnly) {
            inputDiv = <div className="col-sm-8"><input type="password" readOnly className={inputClass} name={this.props.name} value={this.props.value} placeholder={placeholderText} /></div>;
        } else {
            inputDiv = <div className="col-sm-8"><input type="password" className={inputClass} name={this.props.name} value={this.props.value} onChange={this.props.onChange} placeholder={placeholderText} /></div>;
        }
        return (
            super.render(inputDiv)
        );
    }
}
