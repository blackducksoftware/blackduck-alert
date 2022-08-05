import React from 'react';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    modalHeader: {
        fontSize: '20px',
        padding: '10px',
        marginBottom: '30px',
        display: 'flex'
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
                <button className={classes.closeBtn} onClick={closeModal}>
                    <span>{'\u2715'}</span>
                </button>
            </div>
    );
};

export default ModalHeader;