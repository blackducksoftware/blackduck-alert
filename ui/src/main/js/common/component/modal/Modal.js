import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ReactDOM from 'react-dom';
import { createUseStyles } from 'react-jss';
import ModalHeader from 'common/component/modal/ModalHeader';
import ModalFooter from 'common/component/modal/ModalFooter';
import Notification from './Notification';

const modalRoot = document.getElementById('alert-modal');

const useStyles = createUseStyles({
    modal: {
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'block',
        // TODO: offset to the navigation width of 250px - please fix when navigation is updated
        inset: '0 0 0 250px',
        position: 'fixed',
        zIndex: '10000',
        outline: 0,
        cursor: 'default'
    },
    modalStyle: {
        backgroundColor: 'white',
        borderRadius: '5px',
        position: 'relative',
        width: '90%',
        maxWidth: 600,
        margin: ['100px', 'auto'],
        maxHeight: '80%',
        overflow: 'auto'
    },
    modalStyleLarge: {
        maxWidth: 900
    },
    modalStyleMedium: {
        maxWidth: 700
    },
    modalStyleSmall: {
        maxWidth: 400
    },
    modalContent: {
        position: 'relative'
    },
    modalBody: {
        overflowY: 'auto',
        maxHeight: 'calc(100vh - 355px)',
        padding: ['16px', 0]
    }
});

const Modal = ({ isOpen, size, title, closeModal, children, handleCancel, handleSubmit, 
    handleTest, submitText, testText, showLoader, notification, showNotification
}) => {
    const classes = useStyles();

    const modalStyleClass = classNames(classes.modalStyle, {
        [classes.modalStyleLarge]: size === 'lg',
        [classes.modalStyleMedium]: size === 'md',
        [classes.modalStyleSmall]: size === 'sm'
    });

    if (!isOpen) {
        return null;
    }

    return ReactDOM.createPortal(
        <div className={classes.modal}>
            <div className={modalStyleClass}>
                <div className={classes.modalContent}>
                    <ModalHeader
                        title={title}
                        closeModal={closeModal}
                    />
                    <div className={classes.modalBody}>
                        { showNotification && (
                            <Notification notification={notification} />
                        )}
                        {children}
                    </div>
                    <ModalFooter
                        handleCancel={handleCancel}
                        handleSubmit={handleSubmit}
                        handleTest={handleTest}
                        submitText={submitText}
                        testText={testText}
                        showLoader={showLoader}
                    />
                </div>
            </div>
        </div>,
        modalRoot
    );
};

Modal.defaultProps = {
    open: true,
    size: 'md'
};

Modal.propTypes = {
    isOpen: PropTypes.bool,
    size: PropTypes.oneOf(['sm', 'md', 'lg']),
    title: PropTypes.string,
    closeModal: PropTypes.func,
    handleCancel: PropTypes.func,
    handleSubmit: PropTypes.func,
    handleTest: PropTypes.func,
    notification: PropTypes.shape({
        message: PropTypes.string,
        title: PropTypes.string,
        type: PropTypes.oneOf(['error', 'success'])
    }),
    showNotification: PropTypes.bool,
    showLoader: PropTypes.bool,
    submitText: PropTypes.string,
    testText: PropTypes.string
};

export default Modal;
