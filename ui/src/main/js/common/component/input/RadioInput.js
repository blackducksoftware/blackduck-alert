import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';

const useStyles = createUseStyles({
    radioContainer: {
        display: 'inline-flex',
        padding: '0.5rem',
        columnGap: '10px'
    },
    modalOption: {
        padding: ['0.5rem', '0.5rem', '0.5rem', '0.7rem']
    },
    optionContainer: {
        display: 'flex',
        alignItems: 'baseline',
        columnGap: '5px'
    }
});

const RadioInput = ({
    id, fieldDescription, errorName, errorValue, label, name, onChange, readOnly,
    required, radioOptions, checked, isInModal, tooltipDescription
}) => {
    const classes = useStyles();

    // Modal styling throws the padding off, correct it here
    const containerClass = classNames(classes.radioContainer, {
        [classes.modalOption]: isInModal
    });

    return (
        <LabeledField
            id={id}
            fieldDescription={fieldDescription}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
            tooltipDescription={tooltipDescription}
        >
            <div className={containerClass}>
                {radioOptions.map((option) => (
                    <div className={classes.optionContainer} key={option.name}>
                        <input
                            type="radio"
                            id={`${id}-${option.value}`}
                            name={name}
                            value={option.value}
                            disabled={readOnly}
                            readOnly={readOnly}
                            onChange={onChange}
                            checked={option.value === checked}
                        />
                        <label htmlFor={option.value}>
                            {option.label}
                        </label>
                    </div>
                ))}
            </div>
        </LabeledField>
    );
};

RadioInput.propTypes = {
    checked: PropTypes.string,
    tooltipDescription: PropTypes.string,
    fieldDescription: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    id: PropTypes.string,
    isInModal: PropTypes.bool,
    label: PropTypes.string.isRequired,
    name: PropTypes.string,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
    required: PropTypes.bool,
    radioOptions: PropTypes.array
};

RadioInput.defaultProps = {
    id: 'checkboxInputId',
    name: 'name',
    onChange: () => true,
    readOnly: false,
    isInModal: false,
    tooltipDescription: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default RadioInput;
