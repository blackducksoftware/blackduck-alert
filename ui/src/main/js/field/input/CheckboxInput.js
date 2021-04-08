import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';

const CheckboxInput = ({
    id, readOnly, name, isChecked, onChange, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
}) => (
    <LabeledField
        labelClass={labelClass}
        description={description}
        showDescriptionPlaceHolder={showDescriptionPlaceHolder}
        label={label}
        errorName={errorName}
        errorValue={errorValue}
        required={required}
    >
        <div className="d-inline-flex p-2 checkbox">
            <input
                id={id}
                type="checkbox"
                readOnly={readOnly}
                disabled={readOnly}
                name={name}
                checked={isChecked}
                onChange={onChange}
            />
        </div>
    </LabeledField>
);

CheckboxInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    isChecked: PropTypes.bool,
    onChange: PropTypes.func,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};

CheckboxInput.defaultProps = {
    id: 'checkboxInputId',
    isChecked: false,
    readOnly: false,
    name: 'name',
    onChange: () => true,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default CheckboxInput;
