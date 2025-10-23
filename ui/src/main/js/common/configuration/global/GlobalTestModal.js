import React from 'react';
import PropTypes from 'prop-types';
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
        style="default"
        showLoader={performingAction}
    >
        <div className="modal-description">
            {children}
        </div>
    </Modal>
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
