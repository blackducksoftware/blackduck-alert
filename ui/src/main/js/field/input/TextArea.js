import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'field/LabeledField';

const TextArea = ({
    inputClass, sizeClass, readOnly, name, value, onChange, id, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
}) => {
    const divClasses = `${sizeClass} d-inline-flex`;

    let field;
    if (readOnly) {
        field = (
            <div className={divClasses}>
                <textarea id={id} rows="8" cols="60" readOnly className={inputClass} name={name} value={value} />
            </div>
        );
    } else {
        field = (
            <div className={divClasses}>
                <textarea id={id} rows="8" cols="60" className={inputClass} name={name} value={value} onChange={onChange} />
            </div>
        );
    }

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
            {field}
        </LabeledField>
    );
};

TextArea.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    inputClass: PropTypes.string,
    sizeClass: PropTypes.string,
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

TextArea.defaultProps = {
    id: 'textAreaId',
    value: '',
    readOnly: false,
    inputClass: 'form-control',
    sizeClass: 'col-sm-8',
    name: 'name',
    onChange: () => true,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default TextArea;
