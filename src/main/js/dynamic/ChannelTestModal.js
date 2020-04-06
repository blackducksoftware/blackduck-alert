import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import TextInput from 'field/input/TextInput';

class ChannelTestModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            destination: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSendTestMessage = this.handleSendTestMessage.bind(this);
        this.handleHide = this.handleHide.bind(this);
    }

    handleChange(event) {
        event.preventDefault();
        const { value } = event.target;
        this.setState({ destination: value });
    }

    handleSendTestMessage(event) {
        event.preventDefault();
        event.stopPropagation();
        const { destination } = this.state;
        const { fieldModel } = this.props;
        this.props.sendTestMessage(fieldModel, destination);
        this.handleHide();
    }

    handleHide() {
        this.setState({
            destination: ''
        });
        this.props.handleCancel();
    }

    render() {
        //TODO FIELD PANEL FOR THE body
        return (
            <Modal show={this.props.showTestModal} onHide={this.handleHide}>
                <Modal.Header closeButton>
                    <Modal.Title>Test Your Configuration</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <TextInput
                        id="destinationName"
                        label={this.props.destinationName}
                        name="destinationName"
                        value={this.state.destination}
                        onChange={this.handleChange}
                    />
                </Modal.Body>
                <Modal.Footer>
                    <button
                        id="testSend"
                        type="button"
                        className="btn btn-primary"
                        onClick={this.handleSendTestMessage}
                    >Send Test Message
                    </button>
                    <button id="testCancel" type="button" className="btn btn-link" onClick={this.handleHide}>Cancel
                    </button>
                </Modal.Footer>
            </Modal>
        );
    }
}

ChannelTestModal.propTypes = {
    showTestModal: PropTypes.bool,
    sendTestMessage: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    destinationName: PropTypes.string.isRequired,
    fieldModel: PropTypes.object.isRequired
};

ChannelTestModal.defaultProps = {
    showTestModal: false
};

export default ChannelTestModal;
