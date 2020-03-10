import {
    CERTIFICATES_DELETE_ERROR,
    CERTIFICATES_DELETED,
    CERTIFICATES_DELETING,
    CERTIFICATES_FETCH_ERROR_ALL,
    CERTIFICATES_FETCHED_ALL,
    CERTIFICATES_FETCHING_ALL,
    CERTIFICATES_SAVE_ERROR,
    CERTIFICATES_SAVED,
    CERTIFICATES_SAVING
} from "store/actions/types";

import * as RequestUtilities from 'util/RequestUtilities'
import { verifyLoginByStatus } from "./session";
import { fetchUsers } from "./users";

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
        certificateSaveError: message,
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

function deletingCertificateError({ message, errors }) {
    return {
        type: CERTIFICATES_DELETE_ERROR,
        certificateDeleteError: message,
        errors
    };
}

export function fetchCertificates() {
    return (dispatch, getState) => {
        dispatch(CERTIFICATES_FETCHING_ALL);
        const { csrfToken } = getState().session;
        const request = RequestUtilities.createReadRequest(CERTIFICATES_API_URL, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json()
                .then((jsonArray) => {
                    dispatch(fetchCertificates(jsonArray));
                });
            } else {
                switch (response.status) {
                    case 401:
                    case 403:
                        dispatch(verifyLoginByStatus(response.status));
                        break;
                    default:
                        response.json()
                        .then((json) => {
                            let message = '';
                            if (json && json.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = json.message.toString();
                            }
                            dispatch(fetchingAllCertificatesError(message));
                        });
                }
            }
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
        let request;
        if (id) {
            request = RequestUtilities.createUpdateRequest(CERTIFICATES_API_URL, csrfToken, id, certificate);
        } else {
            request = RequestUtilities.createPostRequest(CERTIFICATES_API_URL, csrfToken, certificate);
        }
        request.then((response) => {
            if (response.ok) {
                response.json()
                .then(() => {
                    dispatch(savedCertificate());
                });
            } else {
                response.json()
                .then((data) => {
                    switch (response.status) {
                        case 401:
                            dispatch(saveCertificateError(data));
                            return dispatch(verifyLoginByStatus(response.status));
                        case 400:
                        default: {
                            return dispatch(saveCertificateError(data));
                        }
                    }
                });
            }
        })
        .then(() => dispatch(fetchUsers()))
        .catch(console.error);
    };
}

export function deleteCertificate(certificateId) {
    return (dispatch, getState) => {
        dispatch(deletingCertificate());
        const { csrfToken } = getState().session;
        const url = CERTIFICATES_API_URL.concat(`/${certificateId}`);
        const request = RequestUtilities.createDeleteRequest(url, csrfToken);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletedCertificate());
            } else {
                response.json()
                .then((data) => {
                    switch (response.status) {
                        case 401:
                            dispatch(deletingCertificateError(data));
                            return dispatch(verifyLoginByStatus(response.status));
                        case 400:
                        default: {
                            return dispatch(deletingCertificateError(data));
                        }
                    }
                });
            }
        })
        .then(() => dispatch(fetchUsers()))
        .catch(console.error);
    };
}
