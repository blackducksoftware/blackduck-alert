import React from 'react';
// import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';

const useStyles = createUseStyles({
    radioContainer: {
        display: 'inline-flex',
        padding: '0.5rem',
        columnGap: '10px'
    },
    optionContainer: {
        display: 'flex',
        alignItems: 'baseline',
        columnGap: '5px'
    }
})
const RadioInput = ({
    id, description, errorName, errorValue, label, labelClass, name, onChange, readOnly, required, showDescriptionPlaceHolder, radioOptions, checked
}) => {
    const classes = useStyles();

    return (
        <LabeledField
            id={id}
            labelClass={labelClass}
            description={description}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
            label={label}
            errorName={errorName}
            errorValue={errorValue}
            required={required}
        >
            <div className={classes.radioContainer}>
                {radioOptions.map((option) => {
                    return (
                        <div className={classes.optionContainer} key={option.name}>
                            <input 
                                type="radio" 
                                id={id} 
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
                    )
                })}
                
            </div>
        </LabeledField>
    )
}

// RadioInput.propTypes = {
//     description: PropTypes.string,
//     errorName: PropTypes.string,
//     errorValue: PropTypes.object,
//     id: PropTypes.string,
//     isChecked: PropTypes.bool,
//     label: PropTypes.string.isRequired,
//     labelClass: PropTypes.string,
//     name: PropTypes.string,
//     onChange: PropTypes.func,
//     readOnly: PropTypes.bool,
//     required: PropTypes.bool,
//     showDescriptionPlaceHolder: PropTypes.bool,
//     radioOptions: PropTypes.shape({
//         PropTypes.arrayOf(
//             name: PropTypes.string,
//             value: PropTypes.string,
//             label: PropTypes.string
//         )
//     })
// };

// RadioInput.defaultProps = {
//     id: 'checkboxInputId',
//     isChecked: false,
//     name: 'name',
//     onChange: () => true,
//     readOnly: false,
//     description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
//     errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
//     errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
//     labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
//     required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
//     showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT

// };

export default RadioInput;
