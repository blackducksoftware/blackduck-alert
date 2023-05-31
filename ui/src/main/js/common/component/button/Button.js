import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles((theme) => ({
    button: {
        background: 'none',
        whiteSpace: 'nowrap',
        padding: ['4px', '10px'],
        font: 'inherit',
        fontSize: '13px',
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
        border: `solid 1px ${theme.colors.defaultAlertColor}`,
        backgroundColor: theme.colors.defaultAlertColor,
        color: theme.colors.white.default,
        '&:disabled': {
            border: `solid 1px ${theme.colors.grey.lightGrey}`,
            backgroundColor: theme.colors.grey.lightGrey,
            color: theme.colors.grey.darkGrey,
            cursor: 'not-allowed'
        },
        '&:active': {
            backgroundColor: theme.colors.darkGreyAlertColor
        }
    },
    transparent: {
        border: `solid 1px ${theme.colors.defaultAlertColor}`,
        color: theme.colors.defaultAlertColor,
        '&:hover': {
            color: theme.colors.white.default,
            border: 'solid 1px',
            backgroundColor: theme.colors.defaultAlertColor
        },
        '&:active': {
            color: theme.colors.white.default,
            border: 'solid 1px',
            backgroundColor: theme.colors.darkGreyAlertColor
        }
    },
    delete: {
        border: `solid 1px ${theme.colors.red.default}`,
        backgroundColor: theme.colors.red.lightRed,
        color: theme.colors.white.default,
        '&:disabled': {
            border: `solid 1px ${theme.colors.grey.lightGrey}`,
            backgroundColor: theme.colors.grey.lightGrey,
            color: theme.colors.grey.darkGrey,
            cursor: 'not-allowed'
        },
        '&:active': {
            border: `solid 1px ${theme.colors.red.lighterRed}`,
            backgroundColor: theme.colors.red.lighterRed
        }
    },
    loader: {
        marginLeft: '5px'
    }
}));

const Button = ({ id, icon, type, isDisabled, onClick, role, buttonStyle = 'default', title, text, showLoader }) => {
    const classes = useStyles();
    const btnClass = classNames(classes.button, {
        [classes.delete]: buttonStyle === 'delete',
        [classes.default]: buttonStyle === 'default',
        [classes.transparent]: buttonStyle === 'transparent'
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
    buttonStyle: PropTypes.oneOf(['default', 'transparent', 'delete']),
    title: PropTypes.string,
    type: PropTypes.string,
    text: PropTypes.string.isRequired,
    showLoader: PropTypes.bool,
    icon: PropTypes.oneOfType([PropTypes.string, PropTypes.array])
};

export default Button;