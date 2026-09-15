import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import BaseInput from 'common/component/input/BaseInput';

const PasswordInput = ({
    id = 'passwordInputId',
    errorName = LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue = LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    isSet = false,
    label,
    fieldDescription,
    name = 'name',
    onChange = () => true,
    readOnly = false,
    required = LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    value = '',
    placeholder,
    tooltipDescription = LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    isDisabled = false
}) => {
    const placeholderText = (isSet) ? '***********' : null;
    const onChangeHandler = readOnly ? null : onChange;

    return (
        <LabeledField
            id={id}
            tooltipDescription={tooltipDescription}
            fieldDescription={fieldDescription}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
            isDisabled={isDisabled}
        >
            <BaseInput
                id={id}
                type="password"
                readOnly={readOnly}
                name={name}
                value={value}
                onChange={onChangeHandler}
                placeholder={placeholderText || placeholder}
                errorValue={errorValue}
                isDisabled={isDisabled}
            />
        </LabeledField>
    );
};

PasswordInput.propTypes = {
    id: PropTypes.string,
    isSet: PropTypes.bool,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func,
    label: PropTypes.string.isRequired,
    fieldDescription: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool,
    tooltipDescription: PropTypes.string,
    isDisabled: PropTypes.bool,
    placeholder: PropTypes.string
};

export default PasswordInput;
