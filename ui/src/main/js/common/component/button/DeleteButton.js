import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    delete: {
        background: 'none',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        backgroundColor: '#E03C31',
        color: 'white',
        '&:focus': {
            outline: 0
        },
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

const DeleteButton = ({ id, icon, children, type, isDisabled, onClick, role, title, text }) => {
    const classes = useStyles();

    return (
        <button
            id={id}
            role={role}
            className={classes.delete}
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

DeleteButton.defaultProps = {
    autoFocus: false,
    isDisabled: false,
    style: 'default',
    type: 'button'
};

DeleteButton.propTypes = {
    id: PropTypes.string,
    children: PropTypes.node,
    onClick: PropTypes.func,
    isDisabled: PropTypes.bool,
    role: PropTypes.string,
    title: PropTypes.string,
    type: PropTypes.string,
    text: PropTypes.string.isRequired
};

export default DeleteButton;