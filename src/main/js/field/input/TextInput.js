import React from 'react';
import LabeledField from 'field/LabeledField';

export default class TextInput extends LabeledField {
    render() {
        const { isSet, inputClass } = this.props;
        const placeholderText = (isSet) ? '********' : null;
        const className = inputClass || 'form-control';

        return (super.render(<div className="d-inline-flex flex-column p-2 col-sm-8">
            <input
                id={this.props.id}
                type="text"
                readOnly={this.props.readOnly}
                autoFocus={this.props.autoFocus}
                className={className}
                name={this.props.name}
                value={this.props.value}
                onChange={this.props.onChange}
                placeholder={placeholderText}
            />
            <label className="fieldError">{this.props.errorValue}</label>
        </div>));
    }
}
