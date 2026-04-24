import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    modalHeader: {
        display: 'flex',
        fontSize: '20px',
        margin: ['20px', '20px', '10px'],
        paddingBottom: '10px',
        borderBottom: `1px solid ${theme.colors.defaultBackgroundColor}`,
        borderTopLeftRadius: theme.modal.modalBorderRadius,
        borderTopRightRadius: theme.modal.modalBorderRadius
    },
    title: {
        flexBasis: '98%',
        textAlign: 'left'
    },
    closeBtn: {
        background: 'none',
        color: theme.colors.grey.default,
        border: 'none',
        font: 'inherit',
        height: 'fit-content',
        '&:focus': {
            outline: 0
        },
        '&:hover': {
            color: theme.colors.grey.blackout,
            backgroundColor: theme.colors.inputDisabled
        }
    }
}));

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
