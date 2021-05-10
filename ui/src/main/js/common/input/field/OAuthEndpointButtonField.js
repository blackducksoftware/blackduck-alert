import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import GeneralButton from 'common/button/GeneralButton';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import StatusMessage from 'common/StatusMessage';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const OAuthEndpointButtonField = ({
    id,
    buttonLabel,
    csrfToken,
    currentConfig,
    description,
    endpoint,
    errorValue,
    fields,
    fieldKey,
    label,
    labelClass,
    onChange,
    readOnly,
    required,
    requiredRelatedFields,
    showDescriptionPlaceHolder,
    statusMessage
}) => {
    const [showModal, setShowModal] = useState(false);
    const [fieldError, setFieldError] = useState(errorValue);
    const [success, setSuccess] = useState(false);
    const [progress, setProgress] = useState(false);

    useEffect(() => {
        setFieldError(errorValue);
        setSuccess(false);
    }, [errorValue]);

    const onSendClick = (event, popupData) => {
        setFieldError(errorValue);
        setProgress(true);
        setSuccess(false);
        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
        const mergedData = popupData ? FieldModelUtilities.combineFieldModels(newFieldModel, popupData) : newFieldModel;
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, mergedData);
        request.then((response) => {
            response.json()
                .then((data) => {
                    const {
                        authorizationUrl, message
                    } = data;
                    const target = {
                        name: [fieldKey],
                        checked: true,
                        type: 'checkbox'
                    };
                    onChange({ target });
                    const okRequest = HTTPErrorUtils.isOk(response.status);
                    if (okRequest) {
                    // REDIRECT: This is where we redirect the current tab to the Azure OAuth URL.
                        window.location.replace(authorizationUrl);
                    } else {
                        setFieldError(HTTPErrorUtils.createFieldError(message));
                        setProgress(false);
                    }
                });
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
                    <GeneralButton
                        id={fieldKey}
                        onClick={flipShowModal}
                        disabled={readOnly}
                        performingAction={progress}
                    >
                        {buttonLabel}
                    </GeneralButton>
                    {success
                    && <StatusMessage id={`${fieldKey}-status-message`} actionMessage={statusMessage} />}

                </div>
            </LabeledField>
        </div>
    );
};

OAuthEndpointButtonField.propTypes = {
    id: PropTypes.string,
    endpoint: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    fieldKey: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    fields: PropTypes.array,
    requiredRelatedFields: PropTypes.array,
    errorValue: PropTypes.string,
    readOnly: PropTypes.bool,
    statusMessage: PropTypes.string,
    description: PropTypes.string,
    label: PropTypes.string.isRequired,
    labelClass: PropTypes.string,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

OAuthEndpointButtonField.defaultProps = {
    id: 'oauthEndpointButtonFieldId',
    fields: [],
    readOnly: false,
    requiredRelatedFields: [],
    statusMessage: 'Success',
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT

};

export default OAuthEndpointButtonField;
