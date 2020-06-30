import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';
import { createDeleteRequest, createFileUploadRequest, createReadRequest } from 'util/configurationRequestBuilder';
import { connect } from 'react-redux';
import StatusMessage from 'field/StatusMessage';
import GeneralButton from './input/GeneralButton';

class UploadFileButtonField extends Component {
    constructor(props) {
        super(props);

        this.onUploadClick = this.onUploadClick.bind(this);
        this.onDeleteClick = this.onDeleteClick.bind(this);
        this.checkFileExists = this.checkFileExists.bind(this);

        this.state = {
            showModal: false,
            fieldError: this.props.errorValue,
            success: false,
            progress: false,
            statusMessage: this.props.statusMessage,
            fileUploaded: false
        };
    }

    componentDidMount() {
        this.checkFileExists();
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

    checkFileExists() {
        const {
            fieldKey, csrfToken, endpoint
        } = this.props;
        const request = createReadRequest(`/alert${endpoint}/${fieldKey}/exists`, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const { exists } = data;
                    this.setState({
                        fileUploaded: exists
                    });
                });
            } else {
                this.setState({
                    fileUploaded: false
                });
            }
        });
    }

    onUploadClick() {
        const fileData = this.refs.fileInputField.files;
        this.setState({
            fieldError: this.props.errorValue,
            progress: true,
            success: false
        });

        if (!fileData || fileData.length <= 0) {
            this.setState({
                progress: false,
                fieldError: "Please select a file to upload."
            });
        } else {
            const {
                fieldKey, csrfToken, endpoint,
            } = this.props;
            const request = createFileUploadRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, "file", fileData);

            request.then((response) => {
                this.setState({
                    progress: false
                });
                if (response.ok) {
                    this.setState({
                        success: true,
                        statusMessage: 'Upload Metadata File Success',
                        fileUploaded: true
                    });

                } else {
                    response.json().then((data) => {
                        this.setState({
                            fieldError: data.message
                        });
                    });
                }
            });
        }
    }

    onDeleteClick() {
        this.setState({
            fieldError: this.props.errorValue,
            progress: true,
            success: false
        });
        const {
            fieldKey, csrfToken, endpoint
        } = this.props;
        const request = createDeleteRequest(`/alert${endpoint}/${fieldKey}`, csrfToken);
        request.then((response) => {
            this.setState({
                progress: false
            });
            if (response.ok) {
                this.setState({
                    success: true,
                    statusMessage: 'Delete Metadata File Success',
                    fileUploaded: false
                });
            } else {
                response.json().then((data) => {
                    this.setState({
                        fieldError: data.message
                    });
                });
            }
        });
    }

    render() {
        const {
            buttonLabel, value, accept, capture, fieldKey, name, readOnly
        } = this.props;

        const acceptedContentTypes = accept ? accept.join(',') : null;
        const endpointField = (
            <div className="d-inline-flex p-2 col-sm-8">
                <div>
                    <input
                        ref="fileInputField"
                        type="file"
                        id={fieldKey}
                        name={name}
                        disabled={readOnly}
                        accept={acceptedContentTypes}
                        capture={capture}
                    />
                    <div>
                        <div className="d-inline-flex">
                            <GeneralButton
                                id={fieldKey}
                                className="uploadButton"
                                onClick={this.onUploadClick}
                                disabled={readOnly}
                                performingAction={this.state.progress}
                            >{buttonLabel}
                            </GeneralButton>
                            {this.state.fileUploaded &&
                            <button id={`${fieldKey}-delete`} className="btn btn-md btn-link" type="reset" onClick={this.onDeleteClick}>Remove Uploaded File</button>
                            }
                        </div>
                    </div>
                </div>
                {this.state.success &&
                <StatusMessage id={`${fieldKey}-status-message`} actionMessage={this.state.statusMessage} />
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
            </div>
        );
    }
}

UploadFileButtonField.propTypes = {
    id: PropTypes.string,
    endpoint: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fieldKey: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    value: PropTypes.bool,
    name: PropTypes.string,
    errorValue: PropTypes.string,
    readOnly: PropTypes.bool,
    statusMessage: PropTypes.string,
    accept: PropTypes.array,
    capture: PropTypes.string,
    multiple: PropTypes.bool
};

UploadFileButtonField.defaultProps = {
    id: 'uploadFileButtonFieldId',
    value: false,
    name: '',
    errorValue: null,
    readOnly: false,
    statusMessage: 'Upload Metadata File Success',
    capture: null,
    accept: null,
    multiple: false
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(UploadFileButtonField);
