import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const TextArea = ({
    id, description, errorName, errorValue, inputClass, label, labelClass, name, onChange, readOnly, required, showDescriptionPlaceHolder, sizeClass, value
}) => {
    const divClasses = `${sizeClass} d-inline-flex`;
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
            <div className={divClasses}>
                <textarea id={id} rows="8" cols="60" readOnly={readOnly} className={inputClass} name={name} value={value} onChange={onChangeHandler} />
            </div>
        </LabeledField>
    );
};

TextArea.propTypes = {
    id: PropTypes.string,
    inputClass: PropTypes.string,
    name: PropTypes.string,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
    sizeClass: PropTypes.string,
    value: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

TextArea.defaultProps = {
    id: 'textAreaId',
    inputClass: 'form-control',
    name: 'name',
    onChange: () => true,
    readOnly: false,
    sizeClass: 'col-sm-8',
    value: '',
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default TextArea;
