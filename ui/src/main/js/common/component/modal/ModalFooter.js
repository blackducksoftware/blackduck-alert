import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import CreateButton from '../button/CreateButton';
import ButtonCancel from '../button/ButtonCancel';

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
    loader: {
        position: 'absolute',
        right: '195px',
        top: '2px'
    }
});

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
                    <ButtonCancel onClick={handleCancel} text="Cancel" />
                )}
                <CreateButton onClick={handleSubmit} text={submitText} />
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
