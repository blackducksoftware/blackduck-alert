import React from 'react';
import PropTypes from 'prop-types';
import Modal from 'common/component/modal/Modal';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    testModalContent: {
        padding: [0, '40px']
    }
});

const GlobalTestModal = ({
    children, showTestModal, handleTest, handleCancel, performingAction, 
    modalSubmitText, disableTestModalSubmit, testModalButtonTitle, testModalTitle
}) => {
    const classes = useStyles();

    return (
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
            <div className={classes.testModalContent}>
                {children}
            </div>
        </Modal>
    );
}

GlobalTestModal.propTypes = {
    children: PropTypes.node.isRequired,
    showTestModal: PropTypes.bool.isRequired,
    testModalTitle: PropTypes.string,
    handleTest: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    performingAction: PropTypes.bool
};

GlobalTestModal.defaultProps = {
    performingAction: false
};

export default GlobalTestModal;
