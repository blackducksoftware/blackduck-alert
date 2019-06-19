import React, { Component } from 'react';
import PropTypes from 'prop-types';
import GeneralButton from 'field/input/GeneralButton';
import PopUp from 'field/PopUp';
import LabeledField from 'field/LabeledField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

class EndpointField extends Component {
    constructor(props) {
        super(props);

        this.onSendClick = this.onSendClick.bind(this);
        this.flipShowModal = this.flipShowModal.bind(this);

        this.state = {
            showModal: false
        };
    }

    // Hits endpoint with data from pop up modal (if exists) and current config
    onSendClick(popupData) {
        const { currentConfig, endpoint } = this.state;
        const mergedData = FieldModelUtilities.combineFieldModels(currentConfig, popupData);

        console.log(`Send to location: ${endpoint}`);
        console.log(mergedData);
    }

    flipShowModal() {
        const { fields } = this.props;
        if (fields.length > 0) {
            this.setState({
                showModal: !this.state.showModal
            });
        } else {
            this.onSendClick({});
        }
    }

    render() {
        const {
            buttonLabel, fields, value, id, name, successBox
        } = this.props;

        const endpointField = (
            <div>
                <GeneralButton id={id} onClick={this.flipShowModal}>{buttonLabel}</GeneralButton>
                {successBox &&
                <input
                    id={`${id}-confirmation`}
                    type="checkbox"
                    name={name}
                    checked={value}
                    readOnly
                />
                }
            </div>
        );

        return (
            <div>
                <LabeledField field={endpointField} {...this.props} />
                <PopUp
                    onCancel={this.flipShowModal}
                    fields={fields}
                    onOk={this.onSendClick}
                    title={buttonLabel}
                    show={this.state.showModal}
                    okLabel="Send"
                />
            </div>

        );
    }
}

EndpointField.propTypes = {
    endpoint: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fields: PropTypes.array,
    value: PropTypes.bool,
    id: PropTypes.string,
    name: PropTypes.string,
    successBox: PropTypes.bool.isRequired
};

EndpointField.defaultProps = {
    value: false,
    fields: [],
    id: '',
    name: ''
};

export default EndpointField;
