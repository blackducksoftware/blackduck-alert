import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';
import { createFileUploadRequest } from 'util/configurationRequestBuilder';
import { connect } from 'react-redux';
import StatusMessage from 'field/StatusMessage';

class UploadFileButtonField extends Component {
    constructor(props) {
        super(props);

        this.onUploadClick = this.onUploadClick.bind(this);

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

    onUploadClick() {
        const fileData = this.refs.fileInputField.files;
        this.setState({
            fieldError: this.props.errorValue,
            progress: true,
            success: false
        });
        const {
            fieldKey, csrfToken, onChange, currentConfig, endpoint
        } = this.props;
        const request = createFileUploadRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, fileData);
        request.then((response) => {
            this.setState({
                progress: false
            });
            if (response.ok) {
                this.setState({
                    success: true
                })
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
            buttonLabel, value, accept, capture, multiple, fieldKey, name, readOnly, statusMessage
        } = this.props;

        const acceptedContentTypes = accept ? accept.join(',') : null;
        const endpointField = (
            <div className="d-inline-flex p-2 col-sm-8">
                <input
                    ref="fileInputField"
                    type="file"
                    id={fieldKey}
                    name={name}
                    onChange={this.onUploadClick}
                    disabled={readOnly}
                    accept={acceptedContentTypes}
                    capture={capture}
                    multiple={multiple}
                />
                {this.state.success &&
                <StatusMessage actionMessage={statusMessage} />
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
    endpoint: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fieldKey: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    fields: PropTypes.array,
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
    value: false,
    fields: [],
    name: '',
    errorValue: null,
    readOnly: false,
    statusMessage: 'Success',
    capture: null,
    accept: null,
    multiple: false
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps, null)(UploadFileButtonField);
