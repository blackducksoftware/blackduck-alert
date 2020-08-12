import React, { Component } from 'react';
import PropTypes from 'prop-types';
import GeneralButton from 'field/input/GeneralButton';
import FieldsPopUp from 'field/FieldsPopUp';
import LabeledField from 'field/LabeledField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';
import { connect } from 'react-redux';
import StatusMessage from 'field/StatusMessage';
import * as HTTErrorUtils from 'util/httpErrorUtilities';

class OAuthEndpointButtonField extends Component {
    constructor(props) {
        super(props);

        this.onSendClick = this.onSendClick.bind(this);
        this.flipShowModal = this.flipShowModal.bind(this);

        this.state = {
            showModal: false,
            fieldError: this.props.errorValue,
            success: false,
            progress: false
        };
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const { errorValue } = prevProps;
        const currentError = this.props.errorValue;
        if (errorValue !== currentError) {
            this.setState({
                fieldError: currentError,
                success: false
            });
        }
    }

    onSendClick(event, popupData) {
        this.setState({
            fieldError: this.props.errorValue,
            progress: true,
            success: false
        });
        const {
            fieldKey, csrfToken, onChange, currentConfig, endpoint, requestedDataFieldKeys
        } = this.props;
        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requestedDataFieldKeys);
        const mergedData = popupData ? FieldModelUtilities.combineFieldModels(newFieldModel, popupData) : newFieldModel;
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, mergedData);
        request.then((response) => {
            this.setState({
                progress: false
            });

            response.json()
            .then((data) => {
                const { httpStatus, authenticated, authorizationUrl, message } = data;
                const target = {
                    name: [fieldKey],
                    checked: true,
                    type: 'checkbox'
                };
                debugger;
                onChange({ target });
                const okRequest = HTTErrorUtils.isOk(httpStatus);
                this.setState({
                    success: okRequest
                });

                if (okRequest) {
                    window.location.replace(authorizationUrl);
                } else {
                    this.setState({
                        fieldError: {
                            severity: 'ERROR',
                            fieldMessage: message
                        }
                    });
                }
            });
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
            buttonLabel, fields, fieldKey, readOnly, statusMessage
        } = this.props;

        const endpointField = (
            <div className="d-inline-flex p-2 col-sm-8">
                <GeneralButton
                    id={fieldKey}
                    onClick={this.flipShowModal}
                    disabled={readOnly}
                    performingAction={this.state.progress}
                >{buttonLabel}
                </GeneralButton>
                {this.state.success &&
                <StatusMessage id={`${fieldKey}-status-message`} actionMessage={statusMessage} />
                }

            </div>
        );

        return (
            <div>
                <LabeledField
                    field={endpointField}
                    {...this.props}
                    errorName={fieldKey}
                    errorValue={this.state.fieldError}
                />
                <FieldsPopUp
                    onCancel={this.flipShowModal}
                    fields={fields}
                    handleSubmit={this.onSendClick}
                    title={buttonLabel}
                    show={this.state.showModal}
                    okLabel="Send"
                />
            </div>

        );
    }
}

OAuthEndpointButtonField.propTypes = {
    id: PropTypes.string,
    endpoint: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fieldKey: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    fields: PropTypes.array,
    requestedDataFieldKeys: PropTypes.array,
    value: PropTypes.bool,
    name: PropTypes.string,
    errorValue: PropTypes.string,
    readOnly: PropTypes.bool,
    statusMessage: PropTypes.string
};

OAuthEndpointButtonField.defaultProps = {
    id: 'oauthEndpointButtonFieldId',
    value: false,
    fields: [],
    requestedDataFieldKeys: [],
    name: '',
    errorValue: null,
    readOnly: false,
    statusMessage: 'Success'
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(OAuthEndpointButtonField);
