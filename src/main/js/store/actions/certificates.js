import {
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

import * as RequestUtilities from 'util/RequestUtilities';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

const CERTIFICATES_API_URL = `/alert/api/certificates`;

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
        certificatesFetchError: message
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

function saveCertificateError({ message, errors }) {
    return {
        type: CERTIFICATES_SAVE_ERROR,
        message,
        errors

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

function deletingCertificateError({ message, errors }) {
    return {
        type: CERTIFICATES_DELETE_ERROR,
        message,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: CERTIFICATES_CLEAR_FIELD_ERRORS
    };
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
                    dispatch(fetchedAllCertificates(responseData));
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

export function saveCertificate(certificate) {
    return (dispatch, getState) => {
        dispatch(savingCertificate());
        const { id } = certificate;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveCertificateError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        let request;
        if (id) {
            const url = CERTIFICATES_API_URL.concat(`/${id}`);
            request = RequestUtilities.createUpdateRequest(url, csrfToken, certificate);
        } else {
            request = RequestUtilities.createPostRequest(CERTIFICATES_API_URL, csrfToken, certificate);
        }
        request.then((response) => {
            response.json()
            .then((responseData) => {
                if (response.ok) {
                    dispatch(savedCertificate());
                    dispatch(fetchCertificates());
                } else {
                    const defaultHandler = () => saveCertificateError(responseData);
                    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));

                    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                    dispatch(handler(response.status));
                }
            });
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
            response.json()
            .then((responseData) => {
                if (response.ok) {
                    dispatch(deletedCertificate());
                } else {
                    const defaultHandler = () => deletingCertificateError(responseData);
                    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));

                    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                    dispatch(handler(response.status));
                }
            });
        })
        .catch(console.error);
    };
}

export function clearCertificateFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
