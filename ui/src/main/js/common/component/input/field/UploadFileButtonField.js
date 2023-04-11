import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import { createDeleteRequest, createFileUploadRequest, createReadRequest } from 'common/util/configurationRequestBuilder';
import StatusMessage from 'common/component/StatusMessage';
import GeneralButton from 'common/component/button/GeneralButton';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import Button from 'common/component/button/Button';

const UploadFileButtonField = ({
    id,
    accept,
    capture,
    buttonLabel,
    csrfToken,
    description,
    endpoint,
    errorValue,
    fieldKey,
    label,
    labelClass,
    name,
    readOnly,
    required,
    showDescriptionPlaceHolder,
    statusMessage,
    permissions,
    onChange,
    customEndpoint,
    value,
    valueToCheckFileExistsOnChange
}) => {
    const [fieldError, setFieldError] = useState(errorValue);
    const [uploadedValue, setUploadedValue] = useState(value);
    const [success, setSuccess] = useState(false);
    const [progress, setProgress] = useState(false);
    const [uploadStatusMessage, setUploadStatusMessage] = useState(statusMessage);
    const [fileUploaded, setFileUploaded] = useState(false);
    const fileInputField = useRef(null);

    const checkFileExists = () => {
        const request = createReadRequest((customEndpoint || `/alert${endpoint}/${fieldKey}/exists`), csrfToken);
        request.then((response) => {
            if (response.status === 204) {
                setFileUploaded(true);
            } else {
                setFileUploaded(false);
            }
        });
    };

    useEffect(() => {
        checkFileExists();
    }, []);

    useEffect(() => {
        setUploadedValue(uploadedValue || value);
    }, [value]);

    useEffect(() => {
        setFieldError(errorValue);
        setSuccess(false);
    }, [errorValue]);

    useEffect(() => {
        checkFileExists();
    }, [valueToCheckFileExistsOnChange]);

    const onUploadClick = () => {
        const fileData = fileInputField.current.files;
        setFieldError(errorValue);
        setProgress(true);
        setSuccess(false);

        if (!fileData || fileData.length <= 0) {
            setProgress(false);
            setFieldError('Please select a file to upload.');
        } else {
            const request = createFileUploadRequest((customEndpoint || `/alert${endpoint}/${fieldKey}`), csrfToken, 'file', fileData);
            request.then((response) => {
                setProgress(false);
                if (response.ok) {
                    setSuccess(true);
                    setUploadStatusMessage('Upload Metadata File Success');
                    setFileUploaded(true);
                    setUploadedValue(value);
                } else {
                    response.json().then((data) => {
                        setFieldError(HTTPErrorUtils.createFieldError(data.message));
                    });
                }
            });
        }
    };

    const onDeleteClick = () => {
        setFieldError(errorValue);
        setProgress(true);
        setSuccess(false);
        const request = createDeleteRequest((customEndpoint || `/alert${endpoint}/${fieldKey}`), csrfToken);
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
    // For fileUploaded
    const removeFileUploadedText = uploadedValue ? (`Remove Uploaded File: ${uploadedValue}`) : 'Remove Uploaded File';

    return (
        <div>
            <LabeledField
                id={id}
                description={description}
                errorName={fieldKey}
                errorValue={fieldError}
                label={label}
                labelClass={labelClass}
                required={required}
                showDescriptionPlaceHolder={showDescriptionPlaceHolder}
            >
                <div className="d-inline-flex p-2 col-sm-8">
                    <div>
                        <input
                            ref={fileInputField}
                            type="file"
                            id={`${fieldKey}-file`}
                            name={name}
                            disabled={readOnly || !permissions.read || !permissions.write}
                            accept={acceptedContentTypes}
                            capture={capture}
                            onChange={onChange}
                        />
                        <div>
                            <div className="d-inline-flex">
                                <Button
                                    id={`${fieldKey}-upload`}
                                    onClick={onUploadClick}
                                    text={buttonLabel}
                                    style="default"
                                    disabled={readOnly || !permissions.read || !permissions.write}
                                />
                                {fileUploaded && (
                                    <Button
                                        id={`${fieldKey}-delete`}
                                        onClick={onDeleteClick}
                                        text={removeFileUploadedText}
                                        style="default"
                                        disabled={readOnly || !permissions.read || !permissions.delete}
                                    />
                                )}
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
    accept: PropTypes.array,
    buttonLabel: PropTypes.string.isRequired,
    capture: PropTypes.string,
    csrfToken: PropTypes.string.isRequired,
    endpoint: PropTypes.string.isRequired,
    fieldKey: PropTypes.string.isRequired,
    name: PropTypes.string,
    readOnly: PropTypes.bool,
    statusMessage: PropTypes.string,
    description: PropTypes.string,
    errorValue: PropTypes.string,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool,
    permissions: PropTypes.shape({
        read: PropTypes.bool,
        write: PropTypes.bool,
        delete: PropTypes.bool
    }),
    onChange: PropTypes.func,
    customEndpoint: PropTypes.string,
    value: PropTypes.any,
    valueToCheckFileExistsOnChange: PropTypes.any
};

UploadFileButtonField.defaultProps = {
    id: 'uploadFileButtonFieldId',
    accept: null,
    capture: null,
    name: '',
    readOnly: false,
    statusMessage: 'Upload Metadata File Success',
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT,
    permissions: {
        read: true,
        write: true,
        delete: true
    },
    onChange: () => {},
    customEndpoint: '',
    value: '',
    valueToCheckFileExistsOnChange: ''
};

export default UploadFileButtonField;
