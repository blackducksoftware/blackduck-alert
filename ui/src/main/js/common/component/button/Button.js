import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    button: {
        background: 'none',
        whiteSpace: 'nowrap',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        '&:focus': {
            outline: 0
        }
    },
    default: {
        border: 'solid .5px',
        backgroundColor: '#2E3B4E',
        color: 'white',
        '& > *': {
            marginRight: '5px'
        }
    },
    transparent: {
        border: 'solid .5px #2E3B4E',
        color: '#2E3B4E',
        '&:hover': {
            border: 'solid 1px #2E3B4E'
        }
    },
    delete: {
        border: 'solid .5px',
        backgroundColor: '#E03C31',
        color: 'white',
        '& > *': {
            marginRight: '5px'
        },
        '&:disabled': {
            border: ['1px', 'solid', '#D9D9D9'],
            backgroundColor: '#D9D9D9',
            color: '#666666',
            cursor: 'not-allowed'
        }
    }
});

const Button = ({ id, icon, type, isDisabled, onClick, role, style = 'default', title, text }) => {
    const classes = useStyles();
    const btnClass = classNames(classes.button, {
        [classes.delete]: style === 'delete',
        [classes.default]: style === 'default',
        [classes.transparent]: style === 'transparent'
    });

    return (
        <button
            id={id}
            role={role}
            className={btnClass}
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

Button.defaultProps = {
    isDisabled: false,
    type: 'button'
};

Button.propTypes = {
    id: PropTypes.string,
    onClick: PropTypes.func,
    isDisabled: PropTypes.bool,
    role: PropTypes.string,
    style: PropTypes.oneOf(['default', 'transparent', 'delete']),
    title: PropTypes.string,
    type: PropTypes.string,
    text: PropTypes.string.isRequired,
};

export default Button;