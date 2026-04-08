import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Dropdown from 'react-bootstrap/Dropdown';

const useStyles = createUseStyles((theme) => ({
    toggleButton: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: `${theme.colors.white.darkWhite} !important`,
        border: `1px solid ${theme.colors.borderColor} !important`,
        color: `${theme.colors.grey.default} !important`,
        height: '30px',
        width: '30px',
        borderRadius: '50%',
        padding: 0,
        margin: [0, '12px', 0, 'auto'],
        '&::after': {
            display: 'none !important'
        },
        '&:hover': {
            backgroundColor: `${theme.colors.grey.lightGrey} !important`,
            color: `${theme.colors.grey.default} !important`,
            border: `1px solid ${theme.colors.borderColor} !important`
        },
        '&:focus': {
            backgroundColor: `${theme.colors.grey.lightGrey} !important`,
            color: `${theme.colors.grey.default} !important`,
            border: `1px solid ${theme.colors.borderColor} !important`
        },
        '&:active': {
            backgroundColor: `${theme.colors.grey.lightGrey} !important`,
            color: `${theme.colors.grey.default} !important`,
            border: `1px solid ${theme.colors.grey.default}  !important`
        }
    },
    rowActionsMenu: {
        borderRadius: '3px',
        minWidth: '150px',
        padding: ['6px', 0],
        boxShadow: '0 8px 16px rgba(0, 0, 0, 0.175)',
        animation: '$menuExpand 150ms ease-in-out',
        '& > .dropdown-item': {
            fontSize: '13px',
            padding: ['6px', '16px'],
            margin: 0,
            '&:hover': {
                backgroundColor: theme.colors.grey.lighterGrey
            }
        }
    },
    '@keyframes menuExpand': {
        from: {
            opacity: 0
        },
        to: {
            opacity: 1
        }
    }
}));

const RowActionsCell = ({ children }) => {
    const classes = useStyles();

    return (
        <Dropdown drop="down">
            <Dropdown.Toggle
                className={classes.toggleButton}
                aria-label="Table Row actions"
                title="Table Row actions"
            >
                <FontAwesomeIcon size="1x" icon="ellipsis" fixedWidth />
            </Dropdown.Toggle>
            <Dropdown.Menu className={classes.rowActionsMenu}>
                {children}
            </Dropdown.Menu>
        </Dropdown>
    );
};

RowActionsCell.propTypes = {
    children: PropTypes.node
};

export default RowActionsCell;
