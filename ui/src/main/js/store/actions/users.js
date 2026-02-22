import {
    USER_MANAGEMENT_GET_FAIL,
    USER_MANAGEMENT_GET_REQUEST,
    USER_MANAGEMENT_GET_SUCCESS,
    USER_MANAGEMENT_USER_BULK_DELETE_FAIL,
    USER_MANAGEMENT_USER_BULK_DELETE_FETCH,
    USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS,
    USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_USER_DELETE_ERROR,
    USER_MANAGEMENT_USER_DELETED,
    USER_MANAGEMENT_USER_DELETING,
    USER_MANAGEMENT_USER_SAVE_ERROR,
    USER_MANAGEMENT_USER_SAVED,
    USER_MANAGEMENT_USER_SAVING,
    USER_MANAGEMENT_USER_VALIDATE_ERROR,
    USER_MANAGEMENT_USER_VALIDATED,
    USER_MANAGEMENT_USER_VALIDATING
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import HeaderUtilities from 'common/util/HeaderUtilities';

function fetchUsersRequest() {
    return {
        type: USER_MANAGEMENT_GET_REQUEST
    };
}

function fetchUsersSuccess(users) {
    return {
        type: USER_MANAGEMENT_GET_SUCCESS,
        data: users
    };
}

function fetchUsersFail(message) {
    return {
        type: USER_MANAGEMENT_GET_FAIL,
        message
    };
}

function saveUserRequest() {
    return {
        type: USER_MANAGEMENT_USER_SAVING
    };
}

function saveUserSuccess() {
    return {
        type: USER_MANAGEMENT_USER_SAVED
    };
}

function saveUserFailMessage(message) {
    return {
        type: USER_MANAGEMENT_USER_SAVE_ERROR,
        message
    };
}

function saveUserFail({ message, errors }) {
    return {
        type: USER_MANAGEMENT_USER_SAVE_ERROR,
        message,
        errors

    };
}

function deleteUserRequest() {
    return {
        type: USER_MANAGEMENT_USER_DELETING
    };
}

function deleteUserSuccess() {
    return {
        type: USER_MANAGEMENT_USER_DELETED
    };
}

function deletingUserErrorMessage(message) {
    return {
        type: USER_MANAGEMENT_USER_DELETE_ERROR,
        message
    };
}

function deleteUserFail({ message, errors }) {
    return {
        type: USER_MANAGEMENT_USER_DELETE_ERROR,
        message,
        errors
    };
}

function bulkDeleteUserRequest() {
    return {
        type: USER_MANAGEMENT_USER_BULK_DELETE_FETCH
    };
}

function bulkDeleteUserSuccess() {
    return {
        type: USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS
    };
}

function bulkDeleteUserFail(errors) {
    return {
        type: USER_MANAGEMENT_USER_BULK_DELETE_FAIL,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS
    };
}

function validatingUser() {
    return {
        type: USER_MANAGEMENT_USER_VALIDATING
    };
}

function validatedUser() {
    return {
        type: USER_MANAGEMENT_USER_VALIDATED
    };
}

function userValidationError(message, errors) {
    return {
        type: USER_MANAGEMENT_USER_VALIDATE_ERROR,
        message,
        errors
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchUsers() {
    return (dispatch, getState) => {
        dispatch(fetchUsersRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchUsersFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(ConfigRequestBuilder.USER_API_URL, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders(),
            redirect: 'manual'
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(fetchUsersSuccess(responseData.users));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                                let message = '';
                                if (responseData && responseData.message) {
                                    // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                    message = responseData.message.toString();
                                }
                                return fetchUsersFail(message);
                            }));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch((error) => {
                console.log(error);
                dispatch(fetchUsersFail(error));
            });
    };
}

export function validateUser(user) {
    return (dispatch, getState) => {
        dispatch(validatingUser());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveUserFailMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const validateRequest = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, user);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        // FIXME figure out the best way to handle warning statuses
                        if (!Object.keys(validationResponse.errors).length) {
                            dispatch(validatedUser());
                        } else {
                            handleValidationError(dispatch, errorHandlers, response.status, () => userValidationError(validationResponse.message, validationResponse.errors));
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => userValidationError(response.message, HTTPErrorUtils.createEmptyErrorObject()));
            }
        })
            .catch(console.error);
    };
}

export function saveUser(user) {
    return (dispatch, getState) => {
        dispatch(saveUserRequest());
        const { id } = user;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveUserFailMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        let saveRequest;
        if (id) {
            saveRequest = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, id, user);
        } else {
            saveRequest = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, user);
        }
        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(saveUserSuccess());
                dispatch(fetchUsers());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => saveUserFail(responseData);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    });
            }
        })
            .catch(console.error);
    };
}

export function deleteUser(userId) {
    return (dispatch, getState) => {
        dispatch(deleteUserRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => deletingUserErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, userId);
        request.then((response) => {
            if (response.ok) {
                dispatch(deleteUserSuccess());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => deleteUserFail(responseData);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    });
            }
        })
            .catch(console.error);
    };
}

export function bulkDeleteUsers(userIdArray) {
    return (dispatch, getState) => {
        dispatch(bulkDeleteUserRequest());
        const { csrfToken } = getState().session;

        Promise.all(userIdArray.map((user) => { // eslint-disable-line
            return ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, user.id);
        })).catch((error) => {
            dispatch(bulkDeleteUserFail(error));
            console.error; // eslint-disable-line
        }).then((response) => {
            if (response) {
                dispatch(bulkDeleteUserSuccess());
            }
        });
    };
}

export function clearUserFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
