import React, { useMemo } from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    paginationBtn: {
        background: 'none',
        whiteSpace: 'nowrap',
        padding: ['2px', '6px'],
        font: 'inherit',
        cursor: 'pointer',
        fontSize: '14px',
        color: theme.colors.white.default,
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        }
    },
    activePage: {
        backgroundColor: 'oklch(55.8% 0.288 302.321)',
        border: 'solid .5px'
    },
    inactivePage: {
        background: 'none',
        color: theme.colors.defaultAlertColor,
        border: 'none',
        '&:hover': {
            backgroundColor: theme.colors.grey.lightGrey
        }
    }
}));

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
            // eslint-disable-next-line
            type={type}
            onClick={onClick}
            title={title}
        >
            {pageNumber}
        </button>
    );
};

PaginationButton.defaultProps = {
    type: 'button'
};

PaginationButton.propTypes = {
    id: PropTypes.string,
    onClick: PropTypes.func,
    role: PropTypes.string,
    title: PropTypes.string,
    type: PropTypes.string,
    isActive: PropTypes.bool.isRequired,
    pageNumber: PropTypes.number.isRequired
};

export default PaginationButton;
