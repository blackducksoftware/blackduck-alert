import React, { Component } from 'react';
import { Modal } from 'react-bootstrap';
import TextInput from 'field/input/TextInput';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';
import TextArea from 'field/input/TextArea';

const TOPIC_ID = 'channel.common.custom.message.topic';
const MESSAGE_ID = 'channel.common.custom.message.content';
const TOPIC_ERROR_NAME = 'topicError';
const MESSAGE_ERROR_NAME = 'messageError';

const DEFAULT_TOPIC = 'Alert Test Message';
const DEFAULT_MESSAGE = 'Test Message Content';


class JobCustomMessageModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            topicName: DEFAULT_TOPIC,
            message: DEFAULT_MESSAGE,
            topicError: null,
            messageError: null
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSendMessage = this.handleSendMessage.bind(this);
        this.handleHide = this.handleHide.bind(this);
    }

    createCustomMessageFieldModel() {
        let newModel = FieldModelUtilities.createEmptyFieldModel([TOPIC_ID, MESSAGE_ID], CONTEXT_TYPE.DISTRIBUTION, `${this.props.channelDescriptorName}-CUSTOM_MESSAGE`);
        newModel = FieldModelUtilities.updateFieldModelSingleValue(newModel, TOPIC_ID, this.state.topicName);
        newModel = FieldModelUtilities.updateFieldModelSingleValue(newModel, MESSAGE_ID, this.state.message);
        return newModel;
    }

    handleChange(event) {
        event.preventDefault();
        const { id, value } = event.target;
        if (id === TOPIC_ID) {
            this.setState({
                topicName: value
            });
        }

        if (id === MESSAGE_ID) {
            this.setState({
                message: value
            });
        }
    }

    handleSendMessage(event) {
        event.preventDefault();
        event.stopPropagation();
        const sendMessageCallback = () => {
            if (!this.state.topicError && !this.state.messageError) {
                this.sendMessage();
                this.handleHide();
            }
        };
        this.validateTextField(MESSAGE_ERROR_NAME, this.state.message, sendMessageCallback);
        this.validateTextField(TOPIC_ERROR_NAME, this.state.topicName, sendMessageCallback);
    }

    validateTextField(errorName, value, callback) {
        let cause = null;

        if (!value || value.trim().length <= 0) {
            cause = 'Required Field';
        }

        this.setState({
            [errorName]: cause
        }, callback);
    }

    sendMessage() {
        const { destination } = this.state;
        const { jobFieldModel } = this.props;
        const customMessageFieldModel = this.createCustomMessageFieldModel();
        const allFieldModels = jobFieldModel.fieldModels;
        allFieldModels.push(customMessageFieldModel);
        const newJobFieldModel = {
            jobId: jobFieldModel.jobId,
            fieldModels: allFieldModels
        };

        this.props.sendMessage(newJobFieldModel, destination);
    }

    handleHide() {
        this.setState({
            topicName: DEFAULT_TOPIC,
            message: DEFAULT_MESSAGE
        });
        this.props.handleCancel();
    }

    render() {
        return (
            <Modal show={this.props.showModal} onHide={this.handleHide}>
                <Modal.Header closeButton>
                    <Modal.Title>Send Message</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <TextInput
                        id={TOPIC_ID}
                        label={this.props.topicLabel}
                        name={TOPIC_ID}
                        value={this.state.topicName}
                        onChange={this.handleChange}
                        errorName={TOPIC_ERROR_NAME}
                        errorValue={this.state.topicError}
                    />
                    <TextArea
                        id={MESSAGE_ID}
                        label={this.props.messageLabel}
                        name={MESSAGE_ID}
                        value={this.state.message}
                        onChange={this.handleChange}
                        errorName={MESSAGE_ERROR_NAME}
                        errorValue={this.state.messageError}
                    />
                </Modal.Body>
                <Modal.Footer>
                    <button
                        id="messageSend"
                        type="button"
                        className="btn btn-primary"
                        onClick={this.handleSendMessage}
                    >Send Message
                    </button>
                    <button id="testCancel" type="button" className="btn btn-link" onClick={this.handleHide}>Cancel
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
    jobFieldModel: PropTypes.object,
    channelDescriptorName: PropTypes.string.isRequired
};

JobCustomMessageModal.defaultProps = {
    showModal: false
};

export default JobCustomMessageModal;
