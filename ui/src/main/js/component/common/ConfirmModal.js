import React, { useEffect, useState } from 'react';
import { Modal } from 'react-bootstrap';
import PropTypes from 'prop-types';

const ConfirmModal = ({
    id, affirmativeAction, affirmativeButtonText, children, modalSize, negativeAction, negativeButtonText, showModal, title
}) => {
    const [show, setShow] = useState(showModal);

    useEffect(() => {
        setShow(showModal);
    }, [showModal]);

    const resetState = () => {
        setShow(false);
    };

    const handleAffirmativeClick = (event) => {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        affirmativeAction();
        resetState();
    };

    const handleNegativeClick = (event) => {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        negativeAction();
        resetState();
    };

    return (
        <Modal id={id} size={modalSize} show={show} onHide={handleNegativeClick}>
            <Modal.Header closeButton>
                <Modal.Title>{title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {children}
            </Modal.Body>
            <Modal.Footer>
                <button
                    id="confirmNegative"
                    type="button"
                    className="btn btn-link"
                    onClick={handleNegativeClick}
                >
                    {negativeButtonText}
                </button>
                <button
                    id="confirmAffirmative"
                    type="button"
                    className="btn btn-danger"
                    onClick={handleAffirmativeClick}
                >
                    {affirmativeButtonText}
                </button>
            </Modal.Footer>
        </Modal>
    );
};

ConfirmModal.propTypes = {
    id: PropTypes.string,
    affirmativeAction: PropTypes.func.isRequired,
    affirmativeButtonText: PropTypes.string,
    children: PropTypes.node.isRequired,
    modalSize: PropTypes.string,
    negativeAction: PropTypes.func,
    negativeButtonText: PropTypes.string,
    showModal: PropTypes.bool.isRequired,
    title: PropTypes.string.isRequired
};

ConfirmModal.defaultProps = {
    id: 'confirmModal',
    affirmativeButtonText: 'Yes',
    modalSize: 'sm',
    negativeButtonText: 'No',
    negativeAction: () => true
};

export default ConfirmModal;
