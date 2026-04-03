import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import BaseInput from 'common/component/input/BaseInput';

const TextInput = ({
    id, description, errorName, errorValue, label,
    name, onChange, readOnly, required, value,
    placeholder, customDescription, isDisabled, fieldDescription
}) => (
    <LabeledField
        id={id}
        description={description}
        label={label}
        errorName={errorName}
        errorValue={errorValue}
        required={required}
        customDescription={customDescription}
        fieldDescription={fieldDescription}
        isDisabled={isDisabled}
    >
        <BaseInput
            id={id}
            type="text"
            readOnly={readOnly}
            placeholder={placeholder}
            name={name}
            value={value}
            errorValue={errorValue}
            onChange={onChange}
            isDisabled={isDisabled}
        />
    </LabeledField>
);

TextInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    placeholder: PropTypes.string,
    customDescription: PropTypes.string,
    fieldDescription: PropTypes.string,
    isDisabled: PropTypes.bool
};

TextInput.defaultProps = {
    id: 'textInputId',
    name: 'name',
    onChange: () => true,
    readOnly: false,
    value: '',
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    isDisabled: false
};

export default TextInput;
