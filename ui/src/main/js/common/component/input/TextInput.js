import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import BaseInput from 'common/component/input/BaseInput';

const TextInput = ({
    id, errorName, errorValue, label,
    name, onChange, readOnly, required, value,
    placeholder, tooltipDescription, isDisabled, fieldDescription
}) => (
    <LabeledField
        id={id}
        label={label}
        errorName={errorName}
        errorValue={errorValue}
        required={required}
        tooltipDescription={tooltipDescription}
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
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    placeholder: PropTypes.string,
    tooltipDescription: PropTypes.string,
    fieldDescription: PropTypes.string,
    isDisabled: PropTypes.bool
};

TextInput.defaultProps = {
    id: 'textInputId',
    name: 'name',
    onChange: () => true,
    readOnly: false,
    value: '',
    tooltipDescription: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    isDisabled: false
};

export default TextInput;
