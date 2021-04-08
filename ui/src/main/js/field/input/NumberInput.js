import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';

const NumberInput = ({
    readOnly, inputClass, id, name, value, onChange, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
}) => {
    let field;
    if (readOnly) {
        field = <div className="d-inline-flex flex-column p-2 col-sm-3"><input id={id} type="number" readOnly className={inputClass} name={name} value={value} /></div>;
    } else {
        field = <div className="d-inline-flex flex-column p-2 col-sm-3"><input id={id} type="number" className={inputClass} name={name} value={value} onChange={onChange} /></div>;
    }
    return (
        <LabeledField
            labelClass={labelClass}
            description={description}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
        >
            {field}
        </LabeledField>
    );
};

NumberInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    inputClass: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};
NumberInput.defaultProps = {
    id: 'numberInputId',
    value: '',
    readOnly: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default NumberInput;
