import React from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'component/common/ConfigButtons';
import MessageFormatter from 'field/MessageFormatter';

const PopUp = ({
    id, children, show, title, cancelLabel, okLabel, includeSave, handleSubmit, performingAction, includeTest, testLabel, handleTest, actionMessage, onCancel
}) => {
    const internalCancel = () => {
        onCancel();
    };

    const internalTest = () => {
        handleTest();
    };

    return (
        <div id={id}>
            <Modal size="lg" show={show} onHide={internalCancel}>
                <Modal.Header closeButton>
                    <Modal.Title>{title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <form
                        className="form-horizontal"
                        noValidate
                        onSubmit={(event) => {
                            handleSubmit(event);
                        }}
                    >
                        {children}
                        <ConfigButtons
                            cancelId="popup-cancel"
                            submitId="popup-submit"
                            testId="popup-test"
                            includeCancel
                            includeSave={includeSave}
                            includeTest={includeTest}
                            onCancelClick={() => {
                                internalCancel();
                            }}
                            onTestClick={() => {
                                if (handleTest) {
                                    internalTest();
                                }
                            }}
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
    onCancel: PropTypes.func.isRequired,
    children: PropTypes.any.isRequired,
    handleSubmit: PropTypes.func,
    handleTest: PropTypes.func,
    show: PropTypes.bool,
    title: PropTypes.string,
    cancelLabel: PropTypes.string,
    okLabel: PropTypes.string,
    testLabel: PropTypes.string,
    performingAction: PropTypes.bool,
    actionMessage: PropTypes.string,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool
};

PopUp.defaultProps = {
    id: 'popupId',
    show: true,
    title: 'Pop up',
    cancelLabel: 'Cancel',
    okLabel: 'Ok',
    testLabel: null,
    handleSubmit: (event) => true,
    handleTest: null,
    performingAction: false,
    actionMessage: null,
    includeSave: true,
    includeTest: false
};

export default PopUp;
