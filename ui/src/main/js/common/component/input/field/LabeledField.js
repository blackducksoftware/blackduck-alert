import React, { useRef, useState } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import Overlay from 'react-bootstrap/Overlay';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { createUseStyles } from 'react-jss';

export const LabelFieldPropertyDefaults = {
    ERROR_NAME_DEFAULT: null,
    ERROR_VALUE_DEFAULT: null,
    DESCRIPTION_DEFAULT: null,
    REQUIRED_DEFAULT: false
};

const useStyles = createUseStyles((theme) => ({
    customTooltip: {
        position: 'relative'
    },
    tooltipText: {
        position: 'absolute',
        top: '-25px',
        left: '-35px',
        padding: ['2px', '10px'],
        backgroundColor: theme.colors.grey.blackout,
        width: 'max-content',
        maxWidth: '500px',
        zIndex: 100,
        borderRadius: '2px',
        color: theme.colors.white.default
    },
    field: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'start',
        paddingBottom: '14px'
    },
    fieldLabel: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        columnGap: '4px',
        paddingBottom: '4px'
    },
    label: {
        fontWeight: 'unset',
        textAlign: 'left',
        fontSize: '15px',
        // fontWeight: 'bold',
        padding: 0,
        color: theme.colors.grey.darkerGrey
    },
    requiredLabel: {
        extend: 'label',
        '&::after': {
            content: '" *"',
            color: theme.colors.red.default
        }
    },
    descriptionIcon: {
        background: 'none',
        border: 'none',
        padding: 0,
        cursor: 'pointer',
        fontSize: '12px',
        color: theme.colors.grey.darkGrey,
        '&:hover': {
            color: theme.colors.grey.blackout
        }
    },
    tooltipCustom: {
        pointerEvents: 'none',
        color: theme.colors.white.default,
        fontSize: '13px',
        borderRadius: '4px',
        '--bs-tooltip-bg': theme.colors.purple.darkerPurple,
        '--bs-tooltip-opacity': 'none',
        '--bs-tooltip-max-width': '600px',
        '--bs-tooltip-padding-x': '12px',
        '--bs-tooltip-padding-y': '8px'
    },
    description: {
        paddingBottom: '8px',
        margin: 0
    }
}));

const LabeledField = ({
    id, children, description, errorName, errorValue, label, required, customDescription, visibleDescription
}) => {
    const classes = useStyles();
    const [showDescription, setShowDescription] = useState(false);
    const target = useRef(null);

    const labelClassName = required ? classes.requiredLabel : classes.label;
    const severity = errorValue ? errorValue.severity : 'ERROR';
    const fieldMessage = errorValue ? errorValue.fieldMessage : '';
    const fieldErrorClass = severity === 'ERROR' ? 'fieldError' : 'fieldWarning';
    const errorMessage = severity === 'WARNING' ? `Warning: ${fieldMessage}` : fieldMessage;

    return (
        <div key={label} className={classes.field}>
            <div className={classes.fieldLabel}>
                <label id={`${id}-label`} className={labelClassName} htmlFor={id}>{label}</label>
                { (description || customDescription) && (
                    <div>
                        <button
                            type="button"
                            className={classes.descriptionIcon}
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                setShowDescription(!showDescription)
                            }}
                            ref={(icon) => {
                                target.current = icon;
                            }}
                        >
                            <FontAwesomeIcon icon="question-circle" />
                        </button>
                        { (customDescription && showDescription) && (
                            <div className={classes.customTooltip}>
                                <span className={classes.tooltipCustom}>
                                    {customDescription}
                                </span>
                            </div>
                        )}
                        <Overlay
                            rootClose
                            placement="top"
                            show={showDescription}
                            onHide={() => setShowDescription(false)}
                            target={() => target.current}
                        >
                            <Tooltip className={classes.tooltipCustom}>
                                {description || customDescription}
                            </Tooltip>
                        </Overlay>
                    </div>
                )}
            </div>
            {visibleDescription && <p className={classes.description}>{visibleDescription}</p>}
            {children}
            {(errorName && errorValue) && (
                <p id={`${id}-fieldError`} className={fieldErrorClass} name={errorName}>{errorMessage}</p>
            )}
        </div>
    );
};

LabeledField.propTypes = {
    id: PropTypes.string,
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.array]),
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    customDescription: PropTypes.string
};

LabeledField.defaultProps = {
    id: 'labeledFieldId',
    children: null,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    customDescription: null
};

export default LabeledField;
