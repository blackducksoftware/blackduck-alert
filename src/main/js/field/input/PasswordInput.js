import React from 'react';

import LabeledField from '../LabeledField';

export default class PasswordInput extends LabeledField {
    render() {
        const {isSet, inputClass, id} = this.props;

        const placeholderText = (isSet) ? '***********' : null;
        const className = inputClass || 'form-control';

        if (this.props.readOnly) {
            return super.render(<div className="d-inline-flex p-2 col-sm-9">
                <input id={id} type="password" readOnly className={className} name={this.props.name} value={this.props.value} placeholder={placeholderText}/>
            </div>);
        }

        return super.render(<div className="d-inline-flex p-2 col-sm-9">
            <input id={id} type="password" className={className} name={this.props.name} value={this.props.value} onChange={this.props.onChange} placeholder={placeholderText}/>
            <label className="fieldError">{this.props.errorValue}</label>
        </div>);
    }
}
