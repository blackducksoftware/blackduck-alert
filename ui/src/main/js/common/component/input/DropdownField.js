import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    dropdownField: {
        padding: ['4px', '10px'],
        borderColor: '#c8c8dd',
        borderRadius: '2px'
    }
});

const DropdownField = ({ id, isDisabled = false, onChange, options }) => {
    const classes = useStyles();

    function handleChange(e) {
        onChange(e.target.value)
    }

    return (
        <select
            className={classes.dropdownField}
            onChange={handleChange}
            disabled={isDisabled}
            id={id}
        >
            { options.map((option) =>
                <option value={option.value} id={option.value}>
                    {option.label}
                </option>
            )}
        </select>
    )
};

DropdownField.propTypes = {
    options: PropTypes.arrayOf(
        PropTypes.shape({
            label: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
            value: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
        })
    ),
    onChange: PropTypes.func,
    isDisabled: PropTypes.bool,
    id: PropTypes.string
};

export default DropdownField;
