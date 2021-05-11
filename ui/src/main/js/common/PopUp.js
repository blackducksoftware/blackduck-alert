import React from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'common/button/ConfigButtons';
import MessageFormatter from 'common/MessageFormatter';

const PopUp = ({
    id, actionMessage, cancelLabel, children, handleSubmit, handleTest, includeSave, includeTest, okLabel, onCancel, performingAction, show, testLabel, title
}) => {
    const internalTest = () => {
        if (handleTest) {
            handleTest();
        }
    };

    return (
        <div id={id}>
            <Modal size="lg" show={show} onHide={onCancel}>
                <Modal.Header closeButton>
                    <Modal.Title>{title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <form
                        className="form-horizontal"
                        noValidate
                        onSubmit={handleSubmit}
                    >
                        {children}
                        <ConfigButtons
                            cancelId="popup-cancel"
                            submitId="popup-submit"
                            testId="popup-test"
                            includeCancel
                            includeSave={includeSave}
                            includeTest={includeTest}
                            onCancelClick={onCancel}
                            onTestClick={internalTest}
                            cancelLabel={cancelLabel}
                            submitLabel={okLabel}
                            testLabel={testLabel}
                            isFixed={false}
                            performingAction={performingAction}
                        />
                        <MessageFormatter
                            id={`${id}-action-message`}
                            name="actionMessage"
                            message={actionMessage}
                        />
                    </form>
                </Modal.Body>
            </Modal>
        </div>
    );
};

PopUp.propTypes = {
    id: PropTypes.string,
    actionMessage: PropTypes.string,
    cancelLabel: PropTypes.string,
    children: PropTypes.any.isRequired,
    handleSubmit: PropTypes.func,
    handleTest: PropTypes.func,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool,
    okLabel: PropTypes.string,
    onCancel: PropTypes.func.isRequired,
    performingAction: PropTypes.bool,
    testLabel: PropTypes.string,
    title: PropTypes.string,
    show: PropTypes.bool
};

PopUp.defaultProps = {
    id: 'popupId',
    actionMessage: null,
    cancelLabel: 'Cancel',
    handleSubmit: () => true,
    handleTest: null,
    includeSave: true,
    includeTest: false,
    okLabel: 'Ok',
    performingAction: false,
    show: true,
    testLabel: null,
    title: 'Pop up'
};

export default PopUp;
