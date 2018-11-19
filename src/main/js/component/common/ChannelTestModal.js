import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {Modal} from 'react-bootstrap';
import {connect} from 'react-redux';
import TextInput from "../../field/input/TextInput";

class ChannelTestModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            value: "",
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSendTestMessage = this.handleSendTestMessage.bind(this);
    }

    handleChange(event) {
        event.preventDefault();
        const target = event.target;
        this.setState({value: target.value});
    }

    handleSendTestMessage() {
        // TODO add spinner and success/failure message?
        this.props.sendTestMessage(this.state.value);
    }

    render() {
        return (<Modal show={this.props.showTestModal} onHide={this.props.cancelTestModal}>
            <Modal.Header closeButton>
                <Modal.Title>Test Your Configuration</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <TextInput id="destinationName" label={this.props.destinationName} name="destinationName" value={this.state.value} onChange={this.handleChange}/>
            </Modal.Body>
            <Modal.Footer>
                <button id="testCancel" type="button" className="btn btn-link" onClick={this.props.cancelTestModal}>Cancel</button>
                <button id="testSend" type="button" className="btn btn-primary" onClick={this.handleSendTestMessage}>Send Test Message</button>
            </Modal.Footer>
        </Modal>);
    }
}

ChannelTestModal.propTypes = {
    destinationName: PropTypes.string,
    showTestModal: PropTypes.bool.isRequired,
    cancelTestModal: PropTypes.func.isRequired,
    sendTestMessage: PropTypes.func.isRequired
};

ChannelTestModal.defaultProps = {
    showTestModal: false,
    destinationName: "Destination",
    cancelTestModal: () => {
        this.setProperty({showTestModal: false})
    },
    sendTestMessage: (dest) => {
    }
};

// TODO is connect required?
const mapStateToProps = state => ({});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(ChannelTestModal);