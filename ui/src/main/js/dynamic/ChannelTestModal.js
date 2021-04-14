import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import FieldsPanel from 'field/FieldsPanel';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as FieldMapping from 'util/fieldMapping';

class ChannelTestModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            testFieldModel: {}
        };
        const { fieldModel, testFields } = this.props;
        if (testFields) {
            const testFieldKeys = FieldMapping.retrieveKeys(testFields);
            const testFieldModel = FieldModelUtilities.createEmptyFieldModel(testFieldKeys, fieldModel.context, fieldModel.descriptorName);
            this.state = {
                testFieldModel
            };
        }

        this.handleChange = this.handleChange.bind(this);
        this.handleSendTestMessage = this.handleSendTestMessage.bind(this);
        this.handleHide = this.handleHide.bind(this);
        this.updateTestModelState = this.updateTestModelState.bind(this);
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
        if (fieldModel && fieldModel.id) {
            combinedModel.id = fieldModel.id;
        }
        this.props.sendTestMessage(combinedModel);
        this.handleHide();
    }

    handleHide() {
        this.setState({
            testFieldModel: {}
        });
        this.props.handleCancel();
    }

    updateTestModelState(newState) {
        this.setState({
            testFieldModel: newState
        });
    }

    render() {
        const { showTestModal, testFields, csrfToken } = this.props;
        const { testFieldModel } = this.state;

        return (
            <Modal show={showTestModal} onHide={this.handleHide}>
                <Modal.Header closeButton>
                    <Modal.Title>Test Your Configuration</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <FieldsPanel
                        descriptorFields={testFields}
                        fieldErrors={null}
                        stateName="testFieldModel"
                        currentConfig={testFieldModel}
                        getCurrentState={() => testFieldModel}
                        setStateFunction={this.updateTestModelState}
                        csrfToken={csrfToken}
                    />
                </Modal.Body>
                <Modal.Footer>
                    <button
                        id="testSend"
                        type="button"
                        className="btn btn-primary"
                        onClick={this.handleSendTestMessage}
                    >
                        Send Test Message
                    </button>
                    <button id="testCancel" type="button" className="btn btn-link" onClick={this.handleHide}>
                        Cancel
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
        fieldModel: PropTypes.object.isRequired,
        testFields: PropTypes.arrayOf(PropTypes.object),
        csrfToken: PropTypes.string.isRequired
    };

ChannelTestModal
    .defaultProps = {
        showTestModal: false,
        testFields: null
    };

export default ChannelTestModal;
