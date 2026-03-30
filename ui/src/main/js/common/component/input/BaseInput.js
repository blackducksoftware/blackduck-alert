import React from 'react';
import PropTypes from 'prop-types';
import { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';

const useStyles = createUseStyles((theme) => ({
    input: {
        border: `1px solid ${theme.colors.grey.lightGrey}`,
        backgroundColor: 'white',
        borderRadius: '8px',
        padding: '0.375rem 0.75rem',
        fontSize: '14px',
        width: ({ width }) => width,
        '&:focus': {
            outline: `1px solid ${theme.colors.defaultBorderColor}`
        },
        '&:hover': {
            border: `1px solid ${theme.colors.defaultBorderColor}`
        }
    },
    errorInput: {
        outline: `1px solid ${theme.colors.red.default}`
    },
    disabledInput: {
        backgroundColor: theme.colors.inputDisabled,
        border: `1px solid ${theme.colors.inputDisabled}`,
        cursor: 'not-allowed'
    }
}));

const BaseInput = ({
    id, errorValue, name, onChange, readOnly, value, placeholder,
    isDisabled, width, type, min, max
}) => {
    const classes = useStyles({ width });
    const inputClass = classNames(classes.input, {
        // TODO: Double check this
        [classes.errorInput]: errorValue?.severity === 'ERROR',
        [classes.disabledInput]: isDisabled
    });

    return (
        <input
            id={id}
            type={type}
            readOnly={readOnly}
            className={inputClass}
            placeholder={placeholder}
            name={name}
            value={value}
            onChange={onChange}
            disabled={isDisabled}
            min={min}
            max={max}
        />
    );
};

BaseInput.propTypes = {
    id: PropTypes.string,
    readOnly: PropTypes.bool,
    name: PropTypes.string,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    onChange: PropTypes.func,
    errorValue: PropTypes.object,
    placeholder: PropTypes.string,
    isDisabled: PropTypes.bool,
    min: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    max: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    type: PropTypes.string
};

BaseInput.defaultProps = {
    id: 'BaseInputId',
    name: 'name',
    onChange: () => true,
    readOnly: false,
    value: '',
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    isDisabled: false,
    width: '100%',
    type: 'text'
};

export default BaseInput;
