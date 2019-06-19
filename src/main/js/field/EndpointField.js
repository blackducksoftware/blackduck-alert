import React, { Component } from 'react';
import PropTypes from 'prop-types';
import GeneralButton from 'field/input/GeneralButton';
import PopUp from 'field/PopUp';
import LabeledField from 'field/LabeledField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';
import { connect } from 'react-redux';

class EndpointField extends Component {
    constructor(props) {
        super(props);

        this.onSendClick = this.onSendClick.bind(this);
        this.flipShowModal = this.flipShowModal.bind(this);

        this.state = {
            showModal: false
        };
    }

    onSendClick(popupData) {
        const {
            fieldKey, csrfToken, onChange, currentConfig, endpoint
        } = this.props;
        const mergedData = FieldModelUtilities.combineFieldModels(currentConfig, popupData);

        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, mergedData);
        request.then((response) => {
            if (response.ok) {
                const target = {
                    name: [fieldKey],
                    checked: true,
                    type: 'checkbox'
                };
                onChange({ target });
            } else {
                response.json()
                    .then(() => {
                        const target = {
                            name: [fieldKey],
                            checked: false,
                            type: 'checkbox'
                        };
                        onChange({ target });
                    });
            }
        });
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
            buttonLabel, fields, value, fieldKey, name, successBox
        } = this.props;

        const endpointField = (
            <div>
                <GeneralButton id={fieldKey} onClick={this.flipShowModal}>{buttonLabel}</GeneralButton>
                {successBox &&
                <input
                    id={`${fieldKey}-confirmation`}
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
    fieldKey: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    fields: PropTypes.array,
    value: PropTypes.bool,
    name: PropTypes.string,
    successBox: PropTypes.bool.isRequired
};

EndpointField.defaultProps = {
    value: false,
    fields: [],
    name: ''
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(EndpointField);
