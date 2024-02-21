import {
    CLIENT_CERTIFICATE_GET_REQUEST,
    CLIENT_CERTIFICATE_GET_SUCCESS,
    CLIENT_CERTIFICATE_GET_ERROR,
    CLIENT_CERTIFICATE_POST_REQUEST,
    CLIENT_CERTIFICATE_POST_SUCCESS,
    CLIENT_CERTIFICATE_POST_ERROR,
    CLIENT_CERTIFICATE_DELETE_REQUEST,
    CLIENT_CERTIFICATE_DELETE_SUCCESS,
    CLIENT_CERTIFICATE_DELETE_ERROR,
    CLIENT_CERTIFICATE_CLEAR_FIELD_ERRORS
} from 'store/actions/types';
import { unauthorized } from 'store/actions/session';
import { CLIENT_CERTIFICATE_URL } from 'common/util/configurationRequestBuilder';

import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as RequestUtilities from 'common/util/RequestUtilities';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

function fetchClientCertificateRequest() {
    return {
        type: CLIENT_CERTIFICATE_GET_REQUEST
    }
}

function fetchClientCertificateSuccess() {
    return {
        type: CLIENT_CERTIFICATE_GET_SUCCESS,
        certificate
    }
}

function fetchClientCertificateError() {
    return {
        type: CLIENT_CERTIFICATE_GET_ERROR,
        message
    }
}

function postClientCertificateRequest() {
    return {
        type: CLIENT_CERTIFICATE_POST_REQUEST
    };
}

function postClientCertificateSuccess() {
    return {
        type: CLIENT_CERTIFICATE_POST_SUCCESS
    };
}

function postClientCertificateError(message) {
    return {
        type: CLIENT_CERTIFICATE_POST_ERROR,
        message,
        errors: {}
    };
}

function deleteClientCertificateRequest() {
    return {
        type: CLIENT_CERTIFICATE_DELETE_REQUEST
    };
}

function deleteClientCertificateSuccess() {
    return {
        type: CLIENT_CERTIFICATE_DELETE_SUCCESS
    };
}

function deleteClientCertificateError(message) {
    return {
        type: CLIENT_CERTIFICATE_DELETE_ERROR,
        message
    };
}

function clearFieldErrors() {
    return {
        type: CLIENT_CERTIFICATE_CLEAR_FIELD_ERRORS
    };
}

export function fetchClientCertificate() {
    return (dispatch, getState) => {
        dispatch(fetchClientCertificateRequest());
        const { csrfToken } = getState().session;

        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchClientCertificateError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const request = RequestUtilities.createReadRequest(CLIENT_CERTIFICATE_URL, csrfToken);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchClientCertificateSuccess(responseData.certificate));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchClientCertificateError(message);
                        }));

                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        })
            .catch((error) => {
                console.log(error);
                dispatch(fetchClientCertificateError(error));
            });
    };
}

export function postClientCertificate(certificate) {
    return (dispatch, getState) => {
        dispatch(postClientCertificateRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => postClientCertificateError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));

        const saveRequest = ConfigurationRequestBuilder.createNewConfigurationRequest(CLIENT_CERTIFICATE_URL, csrfToken, certificate);

        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(postClientCertificateSuccess());
                // dispatch(fetchCertificates());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => postClientCertificateError(responseData.message);
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

export function deleteClientCertificate() {
    return (dispatch, getState) => {
        dispatch(deleteClientCertificateRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => deleteClientCertificateError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));

        const request = RequestUtilities.createDeleteRequest(CLIENT_CERTIFICATE_URL, csrfToken);
        request.then((response) => {
            if (response.ok) {
                dispatch(deleteClientCertificateSuccess());
                // dispatch(fetchCertificates());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => deleteClientCertificateError(responseData.message);
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

export function clearClientCertificateFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
