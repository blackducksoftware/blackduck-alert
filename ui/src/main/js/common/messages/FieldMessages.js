export const COMMON_FIELD_EROR_MESSAGES = {
    REQUIRED_FIELD_MISSING_KEY: 'Required field missing',
    INVALID_OPTION_KEY: 'Invalid option selected',
    ENCRYPTION_MISSING_KEY: 'Encryption configuration missing.'
};

export function findErrorMessage(fieldErrorKey, fieldErrorMessageMap) {
    const commonMessage = fieldErrorKey && COMMON_FIELD_EROR_MESSAGES[fieldErrorKey];
    const message = !commonMessage && fieldErrorMessageMap[fieldErrorKey];
    if (!message) {
        // this is for backwards compatibility where we may have a status message.
        return fieldErrorKey;
    }
    return message;
}
