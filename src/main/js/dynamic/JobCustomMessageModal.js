import React, { Component } from "react";
import { Modal } from "react-bootstrap";
import TextInput from "../field/input/TextInput";
import PropTypes from "prop-types";
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { CONTEXT_TYPE } from "../util/descriptorUtilities";

const TOPIC_ID = 'channel.common.custom.message.topic';
const MESSAGE_ID = 'channel.common.custom.message.content';

class JobCustomMessageModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            topicName: 'Alert Test Message',
            message: 'Test Message'
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSendMessage = this.handleSendMessage.bind(this);
        this.handleHide = this.handleHide.bind(this);
    }

    createCustomMessageFieldModel() {
        const { jobFieldModel } = this.props;
        const fieldModel = jobFieldModel.fieldModels.find(model => model.descriptorName === this.props.channelDescriptorName);
        let newModel = FieldModelUtilities.createEmptyFieldModel([TOPIC_ID, MESSAGE_ID], CONTEXT_TYPE.DISTRIBUTION, this.props.channelDescriptorName);
        newModel = FieldModelUtilities.combineFieldModels(newModel, fieldModel);
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
        const { destination } = this.state;
        const { jobFieldModel } = this.props;
        const newJobFieldModel = {
            jobId: jobFieldModel.jobId,
            fieldModels: []
        };
        const customMessageFieldModel = this.createCustomMessageFieldModel();
        newJobFieldModel.fieldModels.push(customMessageFieldModel);
        const otherModels = jobFieldModel.fieldModels.filter(model => model.descriptorName !== this.props.channelDescriptorName);
        newJobFieldModel.fieldModels = newJobFieldModel.fieldModels.concat(otherModels);
        this.props.sendMessage(newJobFieldModel, destination);
        this.handleHide();
    }

    handleHide() {
        this.setState({
            topicName: 'Alert Test Message',
            message: 'Test Message'
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
                    >Send Message
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
    jobFieldModel: PropTypes.object.isRequired,
    channelDescriptorName: PropTypes.string.isRequired
};

JobCustomMessageModal.defaultProps = {
    showModal: false
};

export default JobCustomMessageModal;
