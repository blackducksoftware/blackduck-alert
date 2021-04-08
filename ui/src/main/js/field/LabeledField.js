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
    id, field, children, labelClass, description, showDescriptionPlaceHolder, label, errorName, errorValue, required
}) => {
    const [showDescription, setShowDescription] = useState(false);
    const target = useRef(null);

    const baseClasses = `${labelClass} text-right`;
    const labelClasses = (required) ? `${baseClasses} required` : baseClasses;
    const severity = errorValue ? errorValue.severity : 'ERROR';
    const fieldMessage = errorValue ? errorValue.fieldMessage : '';
    const fieldErrorClass = severity === 'ERROR' ? 'fieldError' : 'fieldWarning';
    const errorMessage = severity === 'WARNING' ? `Warning: ${fieldMessage}` : fieldMessage;
    let descriptionField = null;
    if (description) {
        descriptionField = (
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
        );
    } else if (showDescriptionPlaceHolder) {
        descriptionField = (<div className="descriptionPlaceHolder" />);
    }

    return (
        <div key={label} className="form-group">
            <label id={`${id}-label`} className={labelClasses}>{label}</label>
            {descriptionField}
            {field}
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
    field: PropTypes.node,
    children: PropTypes.element, // TODO make this required allows for a single child
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    description: PropTypes.string,
    showDescriptionPlaceHolder: PropTypes.bool,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    required: PropTypes.bool
};

LabeledField.defaultProps = {
    id: 'labeledFieldId',
    field: null,
    children: null, // TODO remove this when it is required
    labelClass: 'col-sm-3 col-form-label',
    errorName: null,
    errorValue: null,
    description: null,
    showDescriptionPlaceHolder: true,
    required: false
};

export default LabeledField;
