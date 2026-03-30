import React, { useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';

const ButtonField = ({
    id,
    buttonLabel,
    customDescription,
    description,
    fieldError,
    fieldKey,
    label,
    readOnly,
    onSendClick,
    required,
    success,
    statusMessage
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
                customDescription={customDescription}
                description={description}
                label={label}
                required={required}
                errorName={fieldKey}
                errorValue={fieldError}
            >
                <Button id={fieldKey} onClick={callOnSendClick} text={buttonLabel} disabled={readOnly} buttonStyle="action" />
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
    description: PropTypes.string,
    fieldError: PropTypes.string,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    success: PropTypes.bool.isRequired,
    statusMessage: PropTypes.string,
    customDescription: PropTypes.string
};

ButtonField.defaultProps = {
    id: 'endpointButtonFieldId',
    readOnly: false,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    fieldError: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    statusMessage: 'Success'
};

export default ButtonField;
