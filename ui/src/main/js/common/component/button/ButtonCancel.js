import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

// TEMPORARY UNTIL CancelButton.js IS REMOVED

const useStyles = createUseStyles({
    create: {
        background: 'none',
        border: 'solid .5px #2E3B4E',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        color: '#2E3B4E',
        '&:focus': {
            outline: 0
        },
        '&:hover': {
            border: 'solid 1px #2E3B4E'
        }
    }
});

const ButtonCancel = ({ id, icon, type, isDisabled, onClick, role, title, text }) => {
    const classes = useStyles();

    return (
        <button
            id={id}
            role={role}
            className={classes.create}
            type={type}
            onClick={onClick}
            title={title}
            disabled={isDisabled}
        >
            { icon && (
                <FontAwesomeIcon icon={icon} />
            )}
            {text}
        </button>
    );
};

ButtonCancel.defaultProps = {
    isDisabled: false,
    type: 'button'
};

ButtonCancel.propTypes = {
    id: PropTypes.string,
    onClick: PropTypes.func,
    isDisabled: PropTypes.bool,
    role: PropTypes.string,
    title: PropTypes.string,
    type: PropTypes.string,
    text: PropTypes.string.isRequired
};

export default ButtonCancel;