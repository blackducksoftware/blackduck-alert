import React, { useMemo } from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    paginationBtn: {
        background: 'none',
        whiteSpace: 'nowrap',
        padding: ['2px', '6px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        color: 'white',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        }
    },
    activePage: {
        backgroundColor: '#2E3B4E',
        border: 'solid .5px'
    },
    inactivePage: {
        background: 'none',
        color: '#2E3B4E',
        border: 'none',
        '&:hover': {
            backgroundColor: '#D9D9D9'
        }
    }
});

const PaginationButton = ({ id, type, onClick, role, title, pageNumber, isActive }) => {
    const classes = useStyles();

    const paginationBtnClass = useMemo(() => classNames(classes.paginationBtn, {
        [classes.activePage]: isActive,
        [classes.inactivePage]: !isActive
    }), [isActive]);

    return (
        <button
            id={id}
            role={role}
            className={paginationBtnClass}
            type={type}
            onClick={onClick}
            title={title}
        >
            {pageNumber}
        </button>
    );
};

PaginationButton.defaultProps = {
    autoFocus: false,
    style: 'default',
    type: 'button'
};

PaginationButton.propTypes = {
    id: PropTypes.string,
    onClick: PropTypes.func,
    role: PropTypes.string,
    title: PropTypes.string,
    type: PropTypes.string,
    pageNumber: PropTypes.number.isRequired
};

export default PaginationButton;