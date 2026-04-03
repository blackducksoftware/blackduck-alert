import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import BaseInput from 'common/component/input/BaseInput';

const PasswordInput = ({
    id, description, errorName, errorValue, isSet, label, fieldDescription,
    name, onChange, readOnly, required, value, placeholder, customDescription, isDisabled
}) => {
    const placeholderText = (isSet) ? '***********' : null;
    const onChangeHandler = readOnly ? null : onChange;

    return (
        <LabeledField
            id={id}
            customDescription={customDescription}
            description={description}
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
    description: PropTypes.string,
    fieldDescription: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool,
    customDescription: PropTypes.string,
    isDisabled: PropTypes.bool,
    placeholder: PropTypes.string
};

PasswordInput.defaultProps = {
    id: 'passwordInputId',
    isSet: false,
    value: '',
    readOnly: false,
    name: 'name',
    onChange: () => true,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    isDisabled: false
};

export default PasswordInput;
