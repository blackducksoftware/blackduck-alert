import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'component/common/ConfigButtons';
import MessageFormatter from 'field/MessageFormatter';

class PopUp extends Component {
    constructor(props) {
        super(props);

        this.internalCancel = this.internalCancel.bind(this);
        this.internalTest = this.internalTest.bind(this);
    }

    internalCancel() {
        this.props.onCancel();
    }

    internalTest() {
        this.props.handleTest();
    }

    render() {
        const {
            children, show, title, cancelLabel, okLabel, includeSave, handleSubmit, performingAction, includeTest, testLabel, handleTest, actionMessage
        } = this.props;
        return (
            <div>
                <Modal size="lg" show={show} onHide={this.internalCancel}>
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
                                    this.internalCancel();
                                }}
                                onTestClick={() => {
                                    if (handleTest) {
                                        this.internalTest();
                                    }
                                }}
                                cancelLabel={cancelLabel}
                                submitLabel={okLabel}
                                isFixed={false}
                                performingAction={performingAction}
                            />
                            <MessageFormatter name="actionMessage" message={actionMessage} />
                        </form>
                    </Modal.Body>
                </Modal>
            </div>
        );
    }
}

PopUp.propTypes = {
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
