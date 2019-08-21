import React, { Component } from "react";
import { Modal } from "react-bootstrap";
import TextInput from "../field/input/TextInput";
import PropTypes from "prop-types";

class JobCustomMessageModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            topicName: '',
            message: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSendMessage = this.handleSendMessage.bind(this);
        this.handleHide = this.handleHide.bind(this);
    }

    handleChange(event) {
        event.preventDefault();
        const { value } = event.target;
        this.setState({ destination: value });
    }

    handleSendMessage(event) {
        event.preventDefault();
        event.stopPropagation();
        const { destination } = this.state;
        const { jobFieldModel } = this.props;
        FieldModelUtils.this.props.sendMessage(jobFieldModel, destination);
        this.handleHide();
    }

    handleHide() {
        this.setState({
            destination: ''
        });
        this.props.handleCancel();
    }

    render() {
        return (
            <Modal show={this.props.showModal} onHide={this.handleHide}>
                <Modal.Header closeButton>
                    <Modal.Title>Send Custom Message</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <TextInput
                        id="channel.common.custom.message.topic"
                        label={this.props.topicLabel}
                        name="channel.common.custom.message.topic"
                        value={this.state.topicName}
                        onChange={this.handleChange}
                    />
                    <TextInput
                        id="channel.common.custom.message.content"
                        label={this.props.messageLabel}
                        name="channel.common.custom.message.content"
                        value={this.state.message}
                        onChange={this.handleChange}
                    />
                </Modal.Body>
                <Modal.Footer>
                    <button id="testCancel" type="button" className="btn btn-link" onClick={this.handleHide}>Cancel
                    </button>
                    <button
                        id="messageSend"
                        type="button"
                        className="btn btn-primary"
                        onClick={this.handleSendMessage}
                    >Send Test Message
                    </button>
                </Modal.Footer>
            </Modal>
        );
    }
}

JobCustomMessageModal.propTypes = {
    showModal: PropTypes.bool,
    sendMessage: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    topicLabel: PropTypes.string,
    messageLabel: PropTypes.string,
    jobFieldModel: PropTypes.object.isRequired
};

JobCustomMessageModal.defaultProps = {
    showModal: false
};

export default JobCustomMessageModal;
