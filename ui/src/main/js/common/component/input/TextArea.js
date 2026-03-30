import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';

const useStyles = createUseStyles(theme => ({
    input: {
        border: `1px solid ${theme.colors.grey.lightGrey}`,
        backgroundColor: 'white',
        borderRadius: '8px',
        padding: '0.375rem 0.75rem',
        fontSize: '14px',
        width: '100%',
        '&:focus': {
            outline: `1px solid ${theme.colors.defaultBorderColor}`,
        },
        '&:hover:not(:focus)': {
            border: `1px solid ${theme.colors.defaultBorderColor}`,
        }
    },
    errorInput: {
        outline: `1px solid ${theme.colors.red.default}`,
    },
    disabledInput: {
        backgroundColor: theme.colors.inputDisabled,
        border: `1px solid ${theme.colors.inputDisabled}`,
        cursor: 'not-allowed'
    }
}));

const TextArea = ({
    id, description, errorName, errorValue, label, name, onChange, readOnly, required, 
    value, customDescription, isDisabled, rows
}) => {
    const classes = useStyles();
    const onChangeHandler = readOnly ? null : onChange;

    const inputClass = classNames(classes.input, {
        // TODO: Double check this
        [classes.errorInput]: errorValue?.severity === 'ERROR',
        [classes.disabledInput]: isDisabled
    });

    return (
        <LabeledField
            customDescription={customDescription}
            description={description}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
        >
            <textarea className={inputClass} id={id} rows={rows} cols="60" readOnly={readOnly} name={name} value={value} onChange={onChangeHandler} disabled={isDisabled} />
        </LabeledField>
    );
};

TextArea.propTypes = {
    id: PropTypes.string,
    name: PropTypes.string,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
    value: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    customDescription: PropTypes.string,
    isDisabled: PropTypes.bool,
    rows: PropTypes.number
};

TextArea.defaultProps = {
    id: 'textAreaId',
    name: 'name',
    onChange: () => true,
    readOnly: false,
    value: '',
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    isDisabled: false,
    rows: 8
};

export default TextArea;
