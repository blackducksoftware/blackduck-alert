import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import FieldLabel from 'common/component/input/field/FieldLabel';

const useStyles = createUseStyles({
    checkboxField: {
        display: 'flex',
        columnGap: '10px'
    }
});

const Checkbox = ({ id, name, label, placeholder, value = '', isChecked = false, isDisabled = false, onChange }) => {
    const classes = useStyles();

    function changeHandler({ target: { value, checked } }) {
        if (!isDisabled) {
            onChange({ name, value, checked });
        }
    }

    return (
        <div className={classes.checkboxField} >
            <FieldLabel label={label}/>
            <input
                name={name}
                aria-checked={isChecked}
                aria-label={label}
                className={classes.input}
                defaultChecked={isChecked}
                disabled={isDisabled ? 'disabled' : null}
                id={id}
                onChange={changeHandler}
                placeholder={placeholder}
                type="checkbox"
                value={value || ''}
                tabIndex={0}
            />
        </div>
    );
};

Checkbox.propTypes = {
    id: PropTypes.string,
    name: PropTypes.string,
    label: PropTypes.string.isRequired,
    placeholder: PropTypes.string,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.bool]),
    isChecked: PropTypes.bool,
    isDisabled: PropTypes.bool,
    onChange: PropTypes.func
};

export default Checkbox;
