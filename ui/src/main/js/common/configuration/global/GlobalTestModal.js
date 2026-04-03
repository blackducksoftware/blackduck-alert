import React from 'react';
import PropTypes from 'prop-types';
import Modal from 'common/component/modal/Modal';

const GlobalTestModal = ({
    children, showTestModal, handleTest, handleCancel, buttonIdPrefix, performingAction, 
    modalSubmitText, disableTestModalSubmit, testModalButtonTitle, testModalTitle
}) => (
    <Modal
        isOpen={showTestModal}
        size="md"
        title={testModalTitle || "Test Your Configuration"}
        closeModal={handleCancel}
        handleSubmit={handleTest}
        disableSubmit={disableTestModalSubmit}
        submitTitle={testModalButtonTitle}
        submitText={ modalSubmitText || 'Send Test Message'}
        showLoader={performingAction}
        buttonStyle="actionSecondary"
    >
        {children}
    </Modal>
);

GlobalTestModal.propTypes = {
    children: PropTypes.node.isRequired,
    showTestModal: PropTypes.bool.isRequired,
    testModalTitle: PropTypes.string,
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
