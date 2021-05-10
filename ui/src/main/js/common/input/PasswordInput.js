import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const PasswordInput = ({
    id, description, errorName, errorValue, inputClass, isSet, label, labelClass, name, onChange, readOnly, required, showDescriptionPlaceHolder, value
}) => {
    const placeholderText = (isSet) ? '***********' : null;
    const onChangeHandler = readOnly ? null : onChange;
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
            <div className="d-inline-flex flex-column p-2 col-sm-8">
                <input id={id} type="password" className={inputClass} readOnly={readOnly} name={name} value={value} onChange={onChangeHandler} placeholder={placeholderText} />
            </div>
        </LabeledField>
    );
};

PasswordInput.propTypes = {
    id: PropTypes.string,
    isSet: PropTypes.bool,
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

PasswordInput.defaultProps = {
    id: 'passwordInputId',
    isSet: false,
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

export default PasswordInput;
