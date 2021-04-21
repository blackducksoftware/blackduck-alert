import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';

const GlobalTestModal = ({
    children, showTestModal, handleTest, handleCancel, buttonIdPrefix
}) => (
    <Modal show={showTestModal} onHide={handleCancel}>
        <Modal.Header closeButton>
            <Modal.Title>Test Your Configuration</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            {children}
        </Modal.Body>
        <Modal.Footer>
            <button
                id={`${buttonIdPrefix}-send`}
                type="submit"
                className="btn btn-primary"
                onClick={handleTest}
            >
                Send Test Message
            </button>
            <button id={`${buttonIdPrefix}-cancel`} type="button" className="btn btn-link" onClick={handleCancel}>
                Cancel
            </button>
        </Modal.Footer>
    </Modal>
);

GlobalTestModal.propTypes = {
    children: PropTypes.node.isRequired,
    showTestModal: PropTypes.bool.isRequired,
    handleTest: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    buttonIdPrefix: PropTypes.string
};

GlobalTestModal.defaultProps = {
    buttonIdPrefix: 'common-test-modal'
};

export default GlobalTestModal;
