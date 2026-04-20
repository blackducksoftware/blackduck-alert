import React, { useRef, useState } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import Overlay from 'react-bootstrap/Overlay';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';

export const LabelFieldPropertyDefaults = {
    ERROR_NAME_DEFAULT: null,
    ERROR_VALUE_DEFAULT: null,
    DESCRIPTION_DEFAULT: null,
    REQUIRED_DEFAULT: false
};

const useStyles = createUseStyles((theme) => ({
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
        columnGap: '4px'
    },
    label: {
        fontWeight: 'unset',
        textAlign: 'left',
        fontSize: '15px',
        padding: 0,
        color: theme.colors.grey.blackout
    },
    requiredLabel: {
        extend: 'label',
        '&::after': {
            content: '" *"',
            color: theme.colors.red.default
        }
    },
    disabledLabel: {
        extend: 'label',
        color: theme.colors.mutedTextColor
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
        '--bs-tooltip-opacity': '1',
        '--bs-tooltip-max-width': '600px',
        '--bs-tooltip-padding-x': '12px',
        '--bs-tooltip-padding-y': '8px',
        zIndex: 10000
    },
    fieldDescription: {
        margin: 0,
        color: theme.colors.mutedTextColor,
        fontSize: '12px',
        lineHeight: '0.8',
        paddingBottom: '8px'
    }
}));

const LabeledField = ({
    id, children, tooltipDescription, errorName, errorValue, label, required, fieldDescription, isDisabled
}) => {
    const classes = useStyles();
    const [showTooltip, setShowTooltip] = useState(false);
    const target = useRef(null);

    const labelClasses = classNames(classes.label, {
        [classes.requiredLabel]: required,
        [classes.disabledLabel]: isDisabled
    });

    const severity = errorValue ? errorValue.severity : 'ERROR';
    const fieldMessage = errorValue ? errorValue.fieldMessage : '';
    const fieldErrorClass = severity === 'ERROR' ? 'fieldError' : 'fieldWarning';
    const errorMessage = severity === 'WARNING' ? `Warning: ${fieldMessage}` : fieldMessage;

    return (
        <div key={label} className={classes.field}>
            <div className={classes.fieldLabel}>
                <label id={`${id}-label`} className={labelClasses} htmlFor={id}>{label}</label>
                {tooltipDescription && (
                    <div>
                        <button
                            type="button"
                            className={classes.descriptionIcon}
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                setShowTooltip(!showTooltip);
                            }}
                            ref={(icon) => {
                                target.current = icon;
                            }}
                        >
                            <FontAwesomeIcon icon="question-circle" />
                        </button>
                        <Overlay
                            rootClose
                            placement="top"
                            show={showTooltip}
                            onHide={() => setShowTooltip(false)}
                            target={() => target.current}
                        >
                            <Tooltip id={id} className={classes.tooltipCustom}>
                                {tooltipDescription}
                            </Tooltip>
                        </Overlay>
                    </div>
                )}
            </div>
            {fieldDescription && <p className={classes.fieldDescription}>{fieldDescription}</p>}
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
    tooltipDescription: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    isDisabled: PropTypes.bool,
    fieldDescription: PropTypes.string
};

LabeledField.defaultProps = {
    id: 'labeledFieldId',
    children: null,
    tooltipDescription: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default LabeledField;
