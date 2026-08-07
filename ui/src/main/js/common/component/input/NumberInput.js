import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import BaseInput from 'common/component/input/BaseInput';

const NumberInput = ({
    readOnly = false,
    id = 'numberInputId',
    name = 'name',
    value = '',
    onChange = () => true,
    label,
    errorName = LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue = LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required = LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    tooltipDescription = LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    minimumValue = 0,
    maximumValue = Number.MAX_SAFE_INTEGER,
    width,
    fieldDescription
}) => {
    const onChangeHandler = readOnly ? null : onChange;

    return (
        <LabeledField
            id={id}
            tooltipDescription={tooltipDescription}
            fieldDescription={fieldDescription}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            required={required}
        >
            <BaseInput
                id={id}
                type="number"
                width={width}
                readOnly={readOnly}
                name={name}
                value={value}
                onChange={onChangeHandler}
                min={minimumValue}
                max={maximumValue}
            />
        </LabeledField>
    );
};

NumberInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    onChange: PropTypes.func,
    fieldDescription: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    tooltipDescription: PropTypes.string,
    minimumValue: PropTypes.number,
    maximumValue: PropTypes.number,
    width: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default NumberInput;
