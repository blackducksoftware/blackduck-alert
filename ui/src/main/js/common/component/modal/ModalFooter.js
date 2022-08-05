import React from 'react';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    modalFooter: {
        fontSize: '20px',
        padding: '10px',
        display: 'flex',
        backgroundColor: '#eeeeee',
        borderBottomLeftRadius: '5px',
        borderBottomRightRadius: '5px',
        marginTop: '40px'
    },
    footerActions: {
        margin: [0, '5px', 0, 'auto']
    },
    submitBtn: {
        background: 'none',
        color: 'inherit',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '6px',
        fontSize: '14px',
        backgroundColor: '#2E3B4E',
        color: 'white',
        '&:focus': {
            outline: 0
        }
    },
    cancleBtn: {
        background: 'none',
        color: 'inherit',
        border: 'solid .5px #2E3B4E',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '6px',
        fontSize: '14px',
        color: '#2E3B4E',
        '&:focus': {
            outline: 0
        },
        '&:hover': {
            border: 'solid 1px #2E3B4E',
        }
    }
});

const SubmitOption = ({ handleSubmit, submitText }) => {
    const classes = useStyles();

    return (
        <button className={classes.submitBtn} onClick={handleSubmit}>
            {submitText}
        </button>
    )
}

const CancelOption = ({ handleCancel }) => {
    const classes = useStyles();

    return (
        <button className={classes.cancleBtn} onClick={handleCancel}>
            Cancel
        </button>
    )
}


const ModalFooter = ({ handleCancel, handleSubmit, submitText }) => {
    const classes = useStyles();

    return (
            <div className={classes.modalFooter}>
                <div className={classes.footerActions}>
                    { handleCancel ? <CancelOption handleCancel={handleCancel} /> : null }
                    <SubmitOption submitText={submitText} handleSubmit={handleSubmit}/>
                </div>
            </div>
    );
};

export default ModalFooter;