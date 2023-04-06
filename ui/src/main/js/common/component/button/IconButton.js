import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    iconBtn: {
        border: 'none',
        background: 'none',
        whiteSpace: 'nowrap',
        padding: ['2px', '6px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        }
    }
});

const IconButton = ({ id, type, onClick, role, title, icon }) => {
    const classes = useStyles();
    return (
        <button
            id={id}
            role={role}
            className={classes.iconBtn}
            type={type}
            onClick={onClick}
            title={title}
        >
            <FontAwesomeIcon icon={icon} />
        </button>
    );
};

IconButton.defaultProps = {
    type: 'button'
};

IconButton.propTypes = {
    id: PropTypes.string,
    onClick: PropTypes.func,
    role: PropTypes.string,
    title: PropTypes.string,
    type: PropTypes.string,
    icon: PropTypes.string.isRequired
};

export default IconButton;