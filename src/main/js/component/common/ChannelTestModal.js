import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import TextInput from 'field/input/TextInput';

class ChannelTestModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            destination: '',
            show: this.props.showTestModal
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSendTestMessage = this.handleSendTestMessage.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
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
        const destination = this.state.destination;
        this.props.sendTestMessage(destination);
    }

    handleCancel(event) {
        event.preventDefault();
        event.stopPropagation();
        this.setState({
            show: false
        });
    }

    handleHide(event) {
        event.preventDefault();
        event.stopPropagation();
        this.setState({
            destination: ''
        });
    }

    render() {
        // TODO figure out a way to toggle the spinner/ there is no point to toggling the spinner if the sendTestMessage is closing the modal
        return (
            <Modal show={this.state.show} onHide={this.handleHide}>
                <Modal.Header closeButton>
                    <Modal.Title>Test Your Configuration</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <TextInput id="destinationName" label={this.props.destinationName} name="destinationName" value={this.state.destination} onChange={this.handleChange} />
                </Modal.Body>
                <Modal.Footer>
                    <button id="testCancel" type="button" className="btn btn-link" onClick={this.handleCancel}>Cancel</button>
                    <button id="testSend" type="button" className="btn btn-primary" onClick={this.handleSendTestMessage}>Send Test Message</button>
                    {this.state.show &&
                    <div className="progressIcon">
                        <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                    </div>
                    }
                </Modal.Footer>
            </Modal>
        );
    }
}

ChannelTestModal.propTypes = {
    showTestModal: PropTypes.bool,
    sendTestMessage: PropTypes.func.isRequired
};

ChannelTestModal.defaultProps = {
    showTestModal: false
}

export default ChannelTestModal;
