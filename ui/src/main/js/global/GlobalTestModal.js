import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';

const GlobalTestModal = ({
    children, showTestModal, handleTest, handleCancel
}) => (
    <Modal show={showTestModal} onHide={handleCancel}>
        <Modal.Header closeButton>
            <Modal.Title>Test Your Configuration</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <form className="form-horizontal" onSubmit={handleTest} noValidate>
                <div>
                    {children}
                </div>
            </form>
        </Modal.Body>
        <Modal.Footer>
            <button
                id="testSend"
                type="submit"
                className="btn btn-primary"
            >
                Send Test Message
            </button>
            <button id="testCancel" type="button" className="btn btn-link" onClick={handleCancel}>
                Cancel
            </button>
        </Modal.Footer>
    </Modal>
);

GlobalTestModal.propTypes = {
    children: PropTypes.node.isRequired,
    showTestModal: PropTypes.bool.isRequired,
    handleTest: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired
};

export default GlobalTestModal;
