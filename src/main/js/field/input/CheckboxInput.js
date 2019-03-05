import React from 'react';
import LabeledField from 'field/LabeledField';

export default class CheckboxInput extends LabeledField {
    render() {
        const {
            name, onChange, readOnly, id, isChecked
        } = this.props;

        const checkboxId = id || 'id';
        const checkboxReadOnly = readOnly || false;
        const checkboxIsChecked = isChecked || false;

        return (super.render(<div className="d-inline-flex p-2 checkbox">
            <input
                id={checkboxId}
                type="checkbox"
                readOnly={checkboxReadOnly}
                disabled={readOnly}
                name={name}
                checked={checkboxIsChecked}
                onChange={onChange}
            />
        </div>));
    }
}
