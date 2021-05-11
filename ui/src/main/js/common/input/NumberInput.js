import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const NumberInput = ({
    readOnly, inputClass, id, name, value, onChange, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
}) => {
    const onChangeHandler = readOnly ? null : onChange;
    return (
        <LabeledField
            description={description}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            labelClass={labelClass}
            required={required}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
        >
            <div className="d-inline-flex flex-column p-2 col-sm-3">
                <input id={id} type="number" className={inputClass} readOnly={readOnly} name={name} value={value} onChange={onChangeHandler} />
            </div>
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
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};
NumberInput.defaultProps = {
    id: 'numberInputId',
    value: '',
    readOnly: false,
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default NumberInput;
