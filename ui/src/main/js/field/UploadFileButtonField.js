import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';
import { createDeleteRequest, createFileUploadRequest, createReadRequest } from 'util/configurationRequestBuilder';
import StatusMessage from 'field/StatusMessage';
import GeneralButton from 'field/input/GeneralButton';

const UploadFileButtonField = (props) => {
    const {
        errorValue, statusMessage, fieldKey, csrfToken, endpoint, buttonLabel, value, accept, capture, name, readOnly
    } = props;
    const [showModal, setShowModal] = useState(false);
    const [fieldError, setFieldError] = useState(errorValue);
    const [success, setSuccess] = useState(false);
    const [progress, setProgress] = useState(false);
    const [uploadStatusMessage, setUploadStatusMessage] = useState(statusMessage);
    const [fileUploaded, setFileUploaded] = useState(false);
    const fileInputField = useRef(null);

    const checkFileExists = () => {
        const request = createReadRequest(`/alert${endpoint}/${fieldKey}/exists`, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const { exists } = data;
                    setFileUploaded(exists);
                });
            } else {
                setFileUploaded(false);
            }
        });
    };

    useEffect(() => {
        checkFileExists();
    }, []);

    useEffect(() => {
        setFieldError(errorValue);
        setSuccess(false);
    }, [errorValue]);

    const onUploadClick = () => {
        const fileData = fileInputField.current.files;
        setFieldError(errorValue);
        setProgress(true);
        setSuccess(false);

        if (!fileData || fileData.length <= 0) {
            setProgress(false);
            setFieldError('Please select a file to upload.');
        } else {
            const request = createFileUploadRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, 'file', fileData);
            request.then((response) => {
                setProgress(false);
                if (response.ok) {
                    setSuccess(true);
                    setUploadStatusMessage('Upload Metadata File Success');
                    setFileUploaded(true);
                } else {
                    response.json().then((data) => {
                        setFieldError(data.message);
                    });
                }
            });
        }
    };

    const onDeleteClick = () => {
        setFieldError(errorValue);
        setProgress(true);
        setSuccess(false);
        const request = createDeleteRequest(`/alert${endpoint}/${fieldKey}`, csrfToken);
        request.then((response) => {
            setProgress(false);
            if (response.ok) {
                setSuccess(true);
                setUploadStatusMessage('Delete Metadata File Success');
                setFileUploaded(false);
            } else {
                response.json().then((data) => {
                    setFieldError(data.message);
                });
            }
        });
    };

    const acceptedContentTypes = accept ? accept.join(',') : null;

    return (
        <div>
            <LabeledField
                {...props}
                errorName={fieldKey}
                errorValue={fieldError}
            >
                <div className="d-inline-flex p-2 col-sm-8">
                    <div>
                        <input
                            ref={fileInputField}
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
                                    onClick={onUploadClick}
                                    disabled={readOnly}
                                    performingAction={progress}
                                >
                                    {buttonLabel}
                                </GeneralButton>
                                {fileUploaded
                                && <button id={`${fieldKey}-delete`} className="btn btn-md btn-link" type="reset" onClick={onDeleteClick}>Remove Uploaded File</button>}
                            </div>
                        </div>
                    </div>
                    {success
                    && <StatusMessage id={`${fieldKey}-status-message`} actionMessage={uploadStatusMessage} />}
                </div>
            </LabeledField>
        </div>
    );
};

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

export default UploadFileButtonField;
