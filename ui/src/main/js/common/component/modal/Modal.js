import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ReactDOM from 'react-dom';
import { createUseStyles } from 'react-jss';
import ModalHeader from 'common/component/modal/ModalHeader';
import ModalFooter from 'common/component/modal/ModalFooter';
import Notification from 'common/component/modal/Notification';

const modalRoot = document.getElementById('alert-modal');

const useStyles = createUseStyles((theme) => ({
    '@keyframes fadeIn': {
        from: { opacity: 0 },
        to: { opacity: 1 }
    },
    modal: {
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'block',
        inset: '0 0 36px 80px',
        position: 'fixed',
        zIndex: '10000',
        outline: 0,
        cursor: 'default',
        overflow: 'hidden',
        animation: '$fadeIn 0.2s ease-in-out'
    },
    modalStyle: {
        backgroundColor: theme.colors.white.default,
        borderRadius: theme.modal.modalBorderRadius,
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: '90%',
        maxWidth: 600
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
        maxHeight: 'calc(100vh - 355px)'
    },
    modalBodyLarge: {
        padding: ['16px', '70px']
    },
    modalBodyMedium: {
        padding: ['16px', '50px']
    },
    modalBodySmall: {
        padding: ['16px', '30px']
    },
    overFlowContent: {
        overflowY: 'auto'
    }
}));

const Modal = ({
    isOpen, size, title, closeModal, children, handleCancel, handleSubmit,
    handleTest, submitText, testText, showLoader, notification, showNotification, buttonStyle,
    disableSubmit, submitTitle, noOverflow
}) => {
    const classes = useStyles();

    const modalBodyClass = classNames(classes.modalBody, {
        [classes.modalBodyLarge]: size === 'lg',
        [classes.modalBodyMedium]: size === 'md',
        [classes.modalBodySmall]: size === 'sm',
        [classes.overFlowContent]: !noOverflow
    });

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
                    <div className={modalBodyClass}>
                        {showNotification && (
                            <Notification notification={notification} />
                        )}
                        {children}
                    </div>
                    <ModalFooter
                        handleCancel={handleCancel}
                        handleSubmit={handleSubmit}
                        handleTest={handleTest}
                        buttonStyle={buttonStyle}
                        submitText={submitText}
                        testText={testText}
                        showLoader={showLoader}
                        disableSubmit={disableSubmit}
                        submitTitle={submitTitle}
                    />
                </div>
            </div>
        </div>,
        modalRoot
    );
};

Modal.defaultProps = {
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
    showLoader: PropTypes.oneOfType([PropTypes.string, PropTypes.bool]),
    submitText: PropTypes.string,
    testText: PropTypes.string,
    buttonStyle: PropTypes.string,
    disableSubmit: PropTypes.bool,
    submitTitle: PropTypes.string,
    noOverflow: PropTypes.bool,
    children: PropTypes.any
};

export default Modal;
