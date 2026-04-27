import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import InfoMessage from 'common/component/notification/InfoMessage';

const useStyles = createUseStyles({
    warningMessage: {
        paddingTop: '10px'
    }
});

const DeleteModal = ({ isOpen, title, confirmationMessage, onClose, onDelete, isLoading, buttonStyle = 'action' }) => {
    const classes = useStyles();

    return (
        <Modal
            isOpen={isOpen}
            size="sm"
            title={title}
            closeModal={onClose}
            handleCancel={onClose}
            handleSubmit={onDelete}
            submitText="Delete"
            showLoader={isLoading}
            disableSubmit={isLoading}
            buttonStyle={buttonStyle}
        >
            <h5>
                {confirmationMessage}
            </h5>
            <div className={classes.warningMessage}>
                <InfoMessage type="warning" message="This action cannot be undone." />
            </div>
        </Modal>
    );
};

DeleteModal.propTypes = {
    isOpen: PropTypes.bool.isRequired,
    title: PropTypes.string,
    confirmationMessage: PropTypes.string.isRequired,
    onClose: PropTypes.func.isRequired,
    onDelete: PropTypes.func.isRequired,
    isLoading: PropTypes.bool,
    buttonStyle: PropTypes.string
};

export default DeleteModal;
