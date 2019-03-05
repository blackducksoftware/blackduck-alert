import React from 'react';
import LabeledField from 'field/LabeledField';

export default class TextArea extends LabeledField {
    render() {
        const {
            inputClass, sizeClass, readOnly, name, value, onChange, id
        } = this.props;
        const className = inputClass || 'textInput';
        const containerClass = sizeClass || 'col-sm-8';

        if (readOnly) {
            return super.render(<div className={containerClass}>
                <textarea id={id} rows="8" cols="60" readOnly className={className} name={name} value={value} />
            </div>);
        }
        return super.render(<div className={containerClass}>
            <textarea id={id} rows="8" cols="60" className={className} name={name} value={value} onChange={onChange} />
        </div>);
    }
}
