import React, { useRef, useState } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import Overlay from 'react-bootstrap/Overlay';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const LabelFieldPropertyDefaults = {
    LABEL_CLASS_DEFAULT: 'col-sm-3 col-form-label',
    ERROR_NAME_DEFAULT: null,
    ERROR_VALUE_DEFAULT: null,
    DESCRIPTION_DEFAULT: null,
    SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT: true,
    REQUIRED_DEFAULT: false
};

const LabeledField = ({
    id, children, description, errorName, errorValue, label, labelClass, required, showDescriptionPlaceHolder
}) => {
    const [showDescription, setShowDescription] = useState(false);
    const target = useRef(null);

    const baseClasses = `${labelClass} text-right`;
    const labelClasses = (required) ? `${baseClasses} required` : baseClasses;
    const severity = errorValue ? errorValue.severity : 'ERROR';
    const fieldMessage = errorValue ? errorValue.fieldMessage : '';
    const fieldErrorClass = severity === 'ERROR' ? 'fieldError' : 'fieldWarning';
    const errorMessage = severity === 'WARNING' ? `Warning: ${fieldMessage}` : fieldMessage;

    return (
        <div key={label} className="form-group">
            <label id={`${id}-label`} className={labelClasses}>{label}</label>
            {description && (
                <div className="d-inline-flex">
                    <span
                        className="descriptionIcon"
                        onClick={() => setShowDescription(!showDescription)}
                        ref={(icon) => {
                            target.current = icon;
                        }}
                    >
                        <FontAwesomeIcon icon="question-circle" className="alert-icon" size="lg" />
                    </span>
                    <Overlay
                        rootClose
                        placement="top"
                        show={showDescription}
                        onHide={() => setShowDescription(false)}
                        target={() => target.current}
                    >
                        <Tooltip id="description-tooltip">
                            {description}
                        </Tooltip>
                    </Overlay>
                </div>
            )}
            {!description && showDescriptionPlaceHolder && (<div className="descriptionPlaceHolder" />)}
            {children}
            {errorName && errorValue
            && (
                <div className="offset-sm-3 col-sm-8">
                    <p id={`${id}-fieldError`} className={fieldErrorClass} name={errorName}>{errorMessage}</p>
                </div>
            )}
        </div>
    );
};

LabeledField.propTypes = {
    id: PropTypes.string,
    children: PropTypes.element,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

LabeledField.defaultProps = {
    id: 'labeledFieldId',
    children: null,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default LabeledField;
