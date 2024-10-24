import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import ReactDOM from 'react-dom';
import { createUseStyles } from 'react-jss';
import ModalHeader from 'common/component/modal/ModalHeader';
import ModalFooter from 'common/component/modal/ModalFooter';
import Notification from './Notification';

const modalRoot = document.getElementById('alert-modal');

const useStyles = createUseStyles((theme) => ({
    modal: {
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'block',
        inset: '50px 0 36px 80px',
        position: 'fixed',
        zIndex: '10000',
        outline: 0,
        cursor: 'default',
        overflow: 'hidden'
    },
    modalStyle: {
        backgroundColor: theme.colors.white.default,
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
        maxHeight: 'calc(100vh - 355px)',
        padding: ['16px', '32px'],
        overflowY: 'auto'
    },
    noOverflowModalBody: {
        maxHeight: 'calc(100vh - 355px)',
        padding: ['16px', '32px']
    }
}));

const Modal = ({
    isOpen, size, title, closeModal, children, handleCancel, handleSubmit,
    handleTest, submitText, testText, showLoader, notification, showNotification, buttonStyle,
    disableSubmit, submitTitle, noOverflow
}) => {
    const classes = useStyles();
    const [style, setStyle] = useState(noOverflow ? classes.noOverflowModalBody : classes.modalBody);

    function handleWindowResize() {
        if (window.innerHeight < 755) {
            setStyle(classes.modalBody);
        } else {
            setStyle(classes.noOverflowModalBody);
        }
    }

    useEffect(() => {
        if (noOverflow) {
            handleWindowResize();
            window.addEventListener('resize', handleWindowResize);

            return () => {
                window.removeEventListener('resize', handleWindowResize);
            };
        }
        return () => {
        };
    }, []);

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
                    <div className={style}>
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
