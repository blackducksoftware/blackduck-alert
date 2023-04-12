import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    button: {
        background: 'none',
        whiteSpace: 'nowrap',
        padding: ['4px', '10px'],
        font: 'inherit',
        fontSize: '13px',
        whiteSpace: 'nowrap',
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        columnGap: '4px',
        '&:focus': {
            outline: 0
        }
    },
    default: {
        border: 'solid 1px',
        backgroundColor: '#2E3B4E',
        color: 'white',
        '&:disabled': {
            border: ['1px', 'solid', '#D9D9D9'],
            backgroundColor: '#D9D9D9',
            color: '#666666',
            cursor: 'not-allowed'
        }
    },
    transparent: {
        border: 'solid 1px #2E3B4E',
        color: '#2E3B4E',
        '&:hover': {
            border: 'solid 1px #2E3B4E'
        }
    },
    delete: {
        border: 'solid 1px #d72c20',
        backgroundColor: '#E03C31',
        color: 'white',
        '&:disabled': {
            border: ['1px', 'solid', '#D9D9D9'],
            backgroundColor: '#D9D9D9',
            color: '#666666',
            cursor: 'not-allowed'
        }
    },
    loader: {
        marginLeft: '5px'
    }
});

const Button = ({ id, icon, type, isDisabled, onClick, role, style = 'default', title, text, showLoader }) => {
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
                <FontAwesomeIcon icon={icon} size="sm" />
            )}
            <div>
                {text}
            </div>
            {showLoader && (
                <div className={classes.loader}>
                    <FontAwesomeIcon icon="spinner" size="md" spin />
                </div>
            )}
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
    showLoader: PropTypes.bool
};

export default Button;