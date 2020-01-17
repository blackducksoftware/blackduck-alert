import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'component/common/ConfigButtons';

class PopUp extends Component {
    constructor(props) {
        super(props);

        this.internalCancel = this.internalCancel.bind(this);
    }

    internalCancel() {
        this.props.onCancel();
    }

    render() {
        const {
            children, show, title, cancelLabel, okLabel, handleSubmit, performingAction
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
                                includeCancel
                                onCancelClick={() => {
                                    this.internalCancel();
                                }}
                                cancelLabel={cancelLabel}
                                submitLabel={okLabel}
                                isFixed={false}
                                performingAction={performingAction}
                            />
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
    show: PropTypes.bool,
    title: PropTypes.string,
    cancelLabel: PropTypes.string,
    okLabel: PropTypes.string,
    performingAction: PropTypes.bool
};

PopUp.defaultProps = {
    show: true,
    title: 'Pop up',
    cancelLabel: 'Cancel',
    okLabel: 'Ok',
    handleSubmit: (event) => true,
    performingAction: false
};

export default PopUp;
