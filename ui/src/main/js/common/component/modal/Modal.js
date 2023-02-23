import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import ModalFooter from 'common/component/modal/ModalFooter';
import ModalHeader from 'common/component/modal/ModalHeader';

const useStyles = createUseStyles({
    modal: {
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'block',
        // TODO: offset to the navigation width of 250px - hack, please fix when navigation is updated
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
        maxHeight: '100%'
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
    modalHeader: {
        textAlign: 'left',
        padding: ['4px', '10px'],
        fontSize: '20px'
    }
});


const Modal = ({ isOpen, size, title, closeModal, children, handleCancel, handleSubmit, submitText, showLoader }) => {
    const classes = useStyles();
    
    const modalStyleClass = classNames(classes.modalStyle, {
        [classes.modalStyleLarge]: size === 'lg',
        [classes.modalStyleMedium]: size === 'md',
        [classes.modalStyleSmall]: size === 'sm',
    });

    if (!isOpen) {
        return null;
    }


    return (
        <div className={classes.modal}>
            <div className={modalStyleClass}>
                <div className={classes.modalContent}>
                    <ModalHeader 
                        title={title} 
                        closeModal={closeModal}
                    />
                    <div className={classes.modalBody}>
                        {children}
                    </div>
                    <div className={classes.modalFooter}>
                        <ModalFooter 
                            handleCancel={handleCancel}
                            handleSubmit={handleSubmit}
                            submitText={submitText}
                            showLoader={showLoader}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

Modal.propTypes = {
    isOpen: PropTypes.bool,
    size: PropTypes.oneOf(['sm', 'md', 'lg']),
    title: PropTypes.string,
    closeModal: PropTypes.func,
    handleCancel: PropTypes.func,
    handleSubmit: PropTypes.func,
    showLoader: PropTypes.bool,
    submitText: PropTypes.string
};

export default Modal;