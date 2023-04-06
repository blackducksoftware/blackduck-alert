import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    create: {
        background: 'none',
        whiteSpace: 'nowrap',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        backgroundColor: '#2E3B4E',
        color: 'white',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        }
    }
});

const CreateButton = ({ id, icon, type, isDisabled, onClick, role, title, text }) => {
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

CreateButton.defaultProps = {
    autoFocus: false,
    isDisabled: false,
    style: 'default',
    type: 'button'
};

CreateButton.propTypes = {
    id: PropTypes.string,
    onClick: PropTypes.func,
    isDisabled: PropTypes.bool,
    role: PropTypes.string,
    title: PropTypes.string,
    type: PropTypes.string,
    text: PropTypes.string.isRequired
};

export default CreateButton;