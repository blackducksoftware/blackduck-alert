import React, { useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';

const ButtonField = ({
    id = 'endpointButtonFieldId',
    buttonLabel,
    tooltipDescription = LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    fieldDescription,
    fieldError = LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    fieldKey,
    label,
    readOnly = false,
    onSendClick,
    required = LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    success,
    statusMessage = 'Success'
}) => {
    const [progress, setProgress] = useState(false);

    const callOnSendClick = async () => {
        setProgress(true);
        await onSendClick();
        setProgress(false);
    };

    return (
        <div>
            <LabeledField
                id={id}
                tooltipDescription={tooltipDescription}
                fieldDescription={fieldDescription}
                label={label}
                required={required}
                errorName={fieldKey}
                errorValue={fieldError}
            >
                <Button id={fieldKey} onClick={callOnSendClick} text={buttonLabel} isDisabled={readOnly} buttonStyle="action" />
                {success && <StatusMessage id={`${fieldKey}-status-message`} actionMessage={statusMessage} />}
            </LabeledField>
        </div>

    );
};

ButtonField.propTypes = {
    onSendClick: PropTypes.func.isRequired,
    id: PropTypes.string,
    buttonLabel: PropTypes.string.isRequired,
    fieldKey: PropTypes.string.isRequired,
    readOnly: PropTypes.bool,
    fieldError: PropTypes.string,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    success: PropTypes.bool.isRequired,
    statusMessage: PropTypes.string,
    fieldDescription: PropTypes.string,
    tooltipDescription: PropTypes.string
};

export default ButtonField;
