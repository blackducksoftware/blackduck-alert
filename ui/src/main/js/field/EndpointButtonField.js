import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import GeneralButton from 'field/input/GeneralButton';
import FieldsPopUp from 'field/FieldsPopUp';
import LabeledField from 'field/LabeledField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';
import StatusMessage from 'field/StatusMessage';

const EndpointButtonField = (props) => {
    const {
        buttonLabel, fields, value, fieldKey, name, successBox, readOnly, statusMessage, errorValue, csrfToken, onChange, currentConfig, endpoint, requiredRelatedFields
    } = props;
    const [showModal, setShowModal] = useState(false);
    const [fieldError, setFieldError] = useState(errorValue);
    const [success, setSuccess] = useState(false);
    const [progress, setProgress] = useState(false);

    useEffect(() => {
        if (fieldError !== errorValue) {
            setFieldError(errorValue);
            setSuccess(false);
        }
    }, [errorValue]);

    const onSendClick = (event, popupData) => {
        setFieldError(errorValue);
        setProgress(true);
        setSuccess(false);

        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
        const mergedData = popupData ? FieldModelUtilities.combineFieldModels(newFieldModel, popupData) : newFieldModel;
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, mergedData);
        request.then((response) => {
            setProgress(false);
            if (response.ok) {
                const target = {
                    name: [fieldKey],
                    checked: true,
                    type: 'checkbox'
                };
                onChange({ target });
                setSuccess(true);
            } else {
                response.json()
                    .then((data) => {
                        const target = {
                            name: [fieldKey],
                            checked: false,
                            type: 'checkbox'
                        };
                        onChange({ target });
                        setFieldError({
                            severity: 'ERROR',
                            fieldMessage: data.message
                        });
                    });
            }
        });
    };

    const flipShowModal = () => {
        if (fields.length > 0) {
            setShowModal(!showModal);
        } else {
            onSendClick({});
        }
    };
    return (
        <div>
            <LabeledField
                {...props}
                errorName={fieldKey}
                errorValue={fieldError}
            >
                <div className="d-inline-flex p-2 col-sm-8">
                    <GeneralButton
                        id={fieldKey}
                        onClick={flipShowModal}
                        disabled={readOnly}
                        performingAction={progress}
                    >
                        {buttonLabel}
                    </GeneralButton>
                    {successBox
                    && (
                        <div className="d-inline-flex p-2 checkbox">
                            <input
                                className="form-control"
                                id={`${fieldKey}-confirmation`}
                                type="checkbox"
                                name={name}
                                checked={value}
                                readOnly
                            />
                        </div>
                    )}
                    {success
                    && <StatusMessage id={`${fieldKey}-status-message`} actionMessage={statusMessage} />}

                </div>
            </LabeledField>
            <FieldsPopUp
                onCancel={flipShowModal}
                fields={fields}
                handleSubmit={onSendClick}
                title={buttonLabel}
                show={showModal}
                okLabel="Send"
            />
        </div>

    );
};

EndpointButtonField.propTypes = {
    id: PropTypes.string,
    endpoint: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fieldKey: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    fields: PropTypes.array,
    requiredRelatedFields: PropTypes.array,
    value: PropTypes.bool,
    name: PropTypes.string,
    successBox: PropTypes.bool.isRequired,
    errorValue: PropTypes.string,
    readOnly: PropTypes.bool,
    statusMessage: PropTypes.string
};

EndpointButtonField.defaultProps = {
    id: 'endpointButtonFieldId',
    value: false,
    fields: [],
    requiredRelatedFields: [],
    name: '',
    errorValue: null,
    readOnly: false,
    statusMessage: 'Success'
};

export default EndpointButtonField;
