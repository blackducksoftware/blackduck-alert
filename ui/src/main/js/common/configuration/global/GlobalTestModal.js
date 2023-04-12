import React from 'react';
import PropTypes from 'prop-types';
// import { Modal } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';

const GlobalTestModal = ({
    children, showTestModal, handleTest, handleCancel, buttonIdPrefix, performingAction, 
    modalSubmitText, disableTestModalSubmit, testModalButtonTitle
}) => (
    <Modal
        isOpen={showTestModal}
        size="md"
        title="Test Your Configuration"
        closeModal={handleCancel}
        handleSubmit={handleTest}
        disableSubmit={disableTestModalSubmit}
        submitTitle={testModalButtonTitle}
        submitText={ modalSubmitText || 'Send Test Message'}
        submitType="default"
        showLoader={performingAction ? 'save' : ''}
    >
        <div className="modal-description">
            {children}
        </div>
    </Modal>
    // <Modal show={showTestModal} onHide={handleCancel}>
    //     <Modal.Header closeButton>
    //         <Modal.Title>Test Your Configuration</Modal.Title>
    //     </Modal.Header>
    //     <Modal.Body>
    //         {children}
    //     </Modal.Body>
    //     <Modal.Footer>
    //         <div className="progressContainer">
    //             <div className="progressIcon">
    //                 {performingAction
    //                 && <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />}
    //             </div>
    //         </div>
    //         <button
    //             id={`${buttonIdPrefix}-send`}
    //             type="submit"
    //             className="btn btn-primary"
    //             onClick={handleTest}
    //             disabled={disableTestModalSubmit}
    //             title={testModalButtonTitle}
    //         >
    //             { modalSubmitText || 'Send Test Message'}
    //         </button>
    //         <button id={`${buttonIdPrefix}-cancel`} type="button" className="btn btn-link" onClick={handleCancel}>
    //             Cancel
    //         </button>
    //     </Modal.Footer>
    // </Modal>
);

GlobalTestModal.propTypes = {
    children: PropTypes.node.isRequired,
    showTestModal: PropTypes.bool.isRequired,
    handleTest: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    buttonIdPrefix: PropTypes.string,
    performingAction: PropTypes.bool
};

GlobalTestModal.defaultProps = {
    buttonIdPrefix: 'common-test-modal',
    performingAction: false
};

export default GlobalTestModal;
