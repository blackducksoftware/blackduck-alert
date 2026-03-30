import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    checkbox: {
        display: 'flex',
        alignItems: 'stretch',
        columnGap: '10px'
    },
    input: {
        border: `1px solid ${theme.colors.grey.lightGrey}`
    },
    errorInput: {
        outline: `1px solid ${theme.colors.red.default}`
    },
    checkboxValueLabel: {
        fontSize: '13px',
        fontWeight: 'bold'
    },
    checkboxValueDescription: {
        padding: ['6px', 0, 0, '22px'],
        fontSize: '13px'
    }
}));

const CheckboxInput = ({
    id, description, errorName, errorValue, isChecked, label, name, onChange, readOnly, required, customDescription, checkboxValueLabel, checkboxValueDescription
}) => {
    const classes = useStyles();
    const inputClass = classNames(classes.input, {
        [classes.errorInput]: errorValue?.severity === 'ERROR'
    });

    return (
        <LabeledField
            id={id}
            customDescription={customDescription}
            description={description}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
        >
            <div className={classes.checkbox}>
                <input
                    id={id}
                    type="checkbox"
                    readOnly={readOnly}
                    disabled={readOnly}
                    name={name}
                    checked={isChecked}
                    onChange={onChange}
                    className={inputClass}
                />
                {checkboxValueLabel && (
                    <div className={classes.checkboxValueLabel}>{checkboxValueLabel}</div>
                )}
            </div>
            {checkboxValueDescription && (
                <div className={classes.checkboxValueDescription}>{checkboxValueDescription}</div>
            )}
        </LabeledField>
    );
};

CheckboxInput.propTypes = {
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    id: PropTypes.string,
    isChecked: PropTypes.bool,
    label: PropTypes.string.isRequired,
    name: PropTypes.string,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
    required: PropTypes.bool,
    customDescription: PropTypes.string,
    checkboxValueLabel: PropTypes.string,
    checkboxValueDescription: PropTypes.string
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
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default CheckboxInput;
