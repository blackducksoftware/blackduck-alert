import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    modalFooter: {
        fontSize: '20px',
        padding: '10px',
        display: 'flex',
        borderBottomLeftRadius: '5px',
        borderBottomRightRadius: '5px',
        position: 'relative',
        backgroundColor: '#eeeeee',
        borderTop: 'solid 1px #D6D6D6'
    },
    footerActions: {
        margin: [0, '5px', 0, 'auto']
    },
    submitBtn: {
        background: 'none',
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
            border: 'solid 1px #2E3B4E'
        }
    },
    loader: {
        position: 'absolute',
        right: '195px',
        top: '2px'
    }
});

const SubmitButton = ({ handleSubmit, submitText }) => {
    const classes = useStyles();

    return (
        <button className={classes.submitBtn} onClick={handleSubmit} type="submit">
            {submitText}
        </button>
    );
};

SubmitButton.propTypes = {
    handleSubmit: PropTypes.func,
    submitText: PropTypes.string
};

const CancelButton = ({ handleCancel }) => {
    const classes = useStyles();

    return (
        <button className={classes.cancleBtn} onClick={handleCancel}>
            Cancel
        </button>
    )
}

CancelButton.propTypes = {
    handleCancel: PropTypes.func
};

const ModalFooter = ({ handleCancel, handleSubmit, submitText, showLoader }) => {
    const classes = useStyles();

    return (
        <div className={classes.modalFooter}>
            <div className={classes.footerActions}>
                {showLoader && (
                    <div className={classes.loader}>
                        <FontAwesomeIcon icon="spinner" className="alert-icon" size="2x" spin />
                    </div>
                )}

                { handleCancel && (
                    <CancelButton handleCancel={handleCancel} />
                )}
                <SubmitButton submitText={submitText} handleSubmit={handleSubmit} />
            </div>
        </div>
    );
};

ModalFooter.propTypes = {
    handleCancel: PropTypes.func,
    handleSubmit: PropTypes.func,
    showLoader: PropTypes.bool,
    submitText: PropTypes.string
};

export default ModalFooter;
