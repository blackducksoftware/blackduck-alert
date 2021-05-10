import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const CheckboxInput = ({
    id, description, errorName, errorValue, isChecked, label, labelClass, name, onChange, readOnly, required, showDescriptionPlaceHolder
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
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    id: PropTypes.string,
    isChecked: PropTypes.bool,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    name: PropTypes.string,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

CheckboxInput.defaultProps = {
    id: 'checkboxInputId',
    isChecked: false,
    name: 'name',
    onChange: () => true,
    readOnly: false,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT

};

export default CheckboxInput;
