import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    modalHeader: {
        display: 'flex',
        fontSize: '20px',
        padding: '10px',
        backgroundColor: '#eeeeee',
        borderBottom: 'solid 1px #D6D6D6'
    },
    title: {
        flexBasis: '98%',
        padding: ['2px', '4px'],
        textAlign: 'left'
    },
    closeBtn: {
        background: 'none',
        color: 'inherit',
        border: 'none',
        padding: ['2px', '10px'],
        font: 'inherit',
        cursor: 'pointer',
        '&:focus': {
            outline: 0
        }
    }
});

const ModalHeader = ({ title, closeModal }) => {
    const classes = useStyles();

    return (
        <div className={classes.modalHeader}>
            <div className={classes.title}>
                {title}
            </div>
            <button className={classes.closeBtn} onClick={closeModal} type="button">
                <span>{'\u2715'}</span>
            </button>
        </div>
    );
};

ModalHeader.propTypes = {
    title: PropTypes.string,
    closeModal: PropTypes.func
};

export default ModalHeader;
