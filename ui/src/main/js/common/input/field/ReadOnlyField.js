import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';

const ReadOnlyField = ({
    id, alt, description, errorName, errorValue, label, labelClass, required, showDescriptionPlaceHolder, url, value
}) => {
    const altValue = alt || url;
    const content = url ? <a alt={altValue} href={url}>{value}</a> : value;
    return (
        <LabeledField
            id={id}
            description={description}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            labelClass={labelClass}
            required={required}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
        >
            <div className="d-inline-flex p-2 col-sm-8">
                <p className="form-control-static">
                    {content}
                </p>
            </div>
        </LabeledField>
    );
};

ReadOnlyField.propTypes = {
    id: PropTypes.string,
    value: PropTypes.string,
    url: PropTypes.string,
    alt: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

ReadOnlyField.defaultProps = {
    id: 'readOnlyFieldId',
    value: '',
    url: '',
    alt: '',
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default ReadOnlyField;
