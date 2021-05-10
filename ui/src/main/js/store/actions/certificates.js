import {
    CERTIFICATE_VALIDATE_ERROR,
    CERTIFICATE_VALIDATED,
    CERTIFICATE_VALIDATING,
    CERTIFICATES_CLEAR_FIELD_ERRORS,
    CERTIFICATES_DELETE_ERROR,
    CERTIFICATES_DELETED,
    CERTIFICATES_DELETING,
    CERTIFICATES_FETCH_ERROR_ALL,
    CERTIFICATES_FETCHED_ALL,
    CERTIFICATES_FETCHING_ALL,
    CERTIFICATES_SAVE_ERROR,
    CERTIFICATES_SAVED,
    CERTIFICATES_SAVING
} from 'store/actions/types';

import * as RequestUtilities from 'common/util/RequestUtilities';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

const CERTIFICATES_API_URL = '/alert/api/certificates';
const CERTIFICATES_VALIDATION_API_URL = `${CERTIFICATES_API_URL}/validate`;

function fetchingAllCertificates() {
    return {
        type: CERTIFICATES_FETCHING_ALL
    };
}

function fetchedAllCertificates(certificates) {
    return {
        type: CERTIFICATES_FETCHED_ALL,
        certificates
    };
}

function fetchingAllCertificatesError(message) {
    return {
        type: CERTIFICATES_FETCH_ERROR_ALL,
        message
    };
}

function validatingCertificate() {
    return {
        type: CERTIFICATE_VALIDATING
    };
}

function validatedCertificate() {
    return {
        type: CERTIFICATE_VALIDATED
    };
}

function certificateValidationError(message, errors) {
    return {
        type: CERTIFICATE_VALIDATE_ERROR,
        message,
        errors
    };
}

function savingCertificate() {
    return {
        type: CERTIFICATES_SAVING
    };
}

function savedCertificate() {
    return {
        type: CERTIFICATES_SAVED
    };
}

function saveCertificateErrorMessage(message) {
    return {
        type: CERTIFICATES_SAVE_ERROR,
        message,
        errors: {}
    };
}

function deletingCertificate() {
    return {
        type: CERTIFICATES_DELETING
    };
}

function deletedCertificate() {
    return {
        type: CERTIFICATES_DELETED
    };
}

function deletingCertificateErrorMessage(message) {
    return {
        type: CERTIFICATES_DELETE_ERROR,
        message
    };
}

function clearFieldErrors() {
    return {
        type: CERTIFICATES_CLEAR_FIELD_ERRORS
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchCertificates() {
    return (dispatch, getState) => {
        dispatch(fetchingAllCertificates());
        const { csrfToken } = getState().session;

        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAllCertificatesError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const request = RequestUtilities.createReadRequest(CERTIFICATES_API_URL, csrfToken);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchedAllCertificates(responseData.certificates));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchingAllCertificatesError(message);
                        }));

                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        })
            .catch((error) => {
                console.log(error);
                dispatch(fetchingAllCertificatesError(error));
            });
    };
}

// FIXME clean this up
export function validateCertificate(certificate) {
    return (dispatch, getState) => {
        dispatch(validatingCertificate());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => certificateValidationError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION, HTTPErrorUtils.createEmptyErrorObject())));

        const validateRequest = RequestUtilities.createPostRequest(CERTIFICATES_VALIDATION_API_URL, csrfToken, certificate);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        // FIXME figure out the best way to handle warning statuses
                        if (!Object.keys(validationResponse.errors).length) {
                            dispatch(validatedCertificate());
                        } else {
                            handleValidationError(dispatch, errorHandlers, response.status, () => certificateValidationError(validationResponse.message, validationResponse.errors));
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => certificateValidationError(response.message, HTTPErrorUtils.createEmptyErrorObject()));
            }
        })
            .catch(console.error);
    };
}

export function saveCertificate(certificate) {
    return (dispatch, getState) => {
        dispatch(savingCertificate());
        const { id } = certificate;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveCertificateErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));

        let saveRequest;
        if (id) {
            const url = CERTIFICATES_API_URL.concat(`/${id}`);
            saveRequest = RequestUtilities.createUpdateRequest(url, csrfToken, certificate);
        } else {
            saveRequest = RequestUtilities.createPostRequest(CERTIFICATES_API_URL, csrfToken, certificate);
        }
        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(savedCertificate());
                dispatch(fetchCertificates());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => saveCertificateErrorMessage(responseData.message);
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));

                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    });
            }
        })
            .catch(console.error);
    };
}

export function deleteCertificate(certificateId) {
    return (dispatch, getState) => {
        dispatch(deletingCertificate());
        const { csrfToken } = getState().session;
        const url = CERTIFICATES_API_URL.concat(`/${certificateId}`);
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => deletingCertificateErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));

        const request = RequestUtilities.createDeleteRequest(url, csrfToken);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletedCertificate());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => deletingCertificateErrorMessage(responseData.message);
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));

                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    });
            }
        })
            .catch(console.error);
    };
}

export function clearCertificateFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
