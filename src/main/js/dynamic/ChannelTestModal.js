import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import TextInput from 'field/input/TextInput';
import FieldsPanel from 'field/FieldsPanel';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as FieldMapping from "../util/fieldMapping";

class ChannelTestModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            destination: '',
            testFieldModel: {}
        };
        const { fieldModel, testFields } = this.props;
        if (testFields) {
            const testFieldKeys = FieldMapping.retrieveKeys(testFields);
            const testFieldModel = FieldModelUtilities.createEmptyFieldModel(testFieldKeys, fieldModel.context, fieldModel.descriptorName);
            this.state = {
                destination: '',
                testFieldModel
            };
        }

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
        const { destination, testFieldModel } = this.state;
        const { fieldModel } = this.props;
        const combinedModel = FieldModelUtilities.combineFieldModels(fieldModel, testFieldModel);
        debugger;
        this.props.sendTestMessage(combinedModel, destination);
        this.handleHide();
    }

    handleHide() {
        this.setState({
            destination: ''
        });
        this.props.handleCancel();
    }

    render() {
        const { destinationName, showTestModal, testFields } = this.props;
        const destinationContent = (<TextInput
            id="destinationName"
            label={destinationName}
            name="destinationName"
            value={this.state.destination}
            onChange={this.handleChange}
        />);
        const bodyContent = !testFields || testFields.length <= 0 ? destinationContent : (
            <FieldsPanel descriptorFields={testFields}
                         self={this}
                         fieldErrors={null}
                         stateName='testFieldModel'
                         currentConfig={this.state.testFieldModel}
            />
        )

        return (
            <Modal show={showTestModal} onHide={this.handleHide}>
                <Modal.Header closeButton>
                    <Modal.Title>Test Your Configuration</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {bodyContent}
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

ChannelTestModal
    .propTypes = {
    showTestModal: PropTypes.bool,
    sendTestMessage: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    destinationName: PropTypes.string.isRequired,
    fieldModel: PropTypes.object.isRequired,
    testFields: PropTypes.arrayOf(PropTypes.object)
};

ChannelTestModal
    .defaultProps = {
    showTestModal: false,
    testFields: null
};

export default ChannelTestModal;
