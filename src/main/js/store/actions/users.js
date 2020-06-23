import {
    USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_USER_DELETE_ERROR,
    USER_MANAGEMENT_USER_DELETED,
    USER_MANAGEMENT_USER_DELETING,
    USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
    USER_MANAGEMENT_USER_FETCHED_ALL,
    USER_MANAGEMENT_USER_FETCHING_ALL,
    USER_MANAGEMENT_USER_SAVE_ERROR,
    USER_MANAGEMENT_USER_SAVED,
    USER_MANAGEMENT_USER_SAVING
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

function fetchingAllUsers() {
    return {
        type: USER_MANAGEMENT_USER_FETCHING_ALL
    };
}

function fetchedAllUsers(users) {
    return {
        type: USER_MANAGEMENT_USER_FETCHED_ALL,
        data: users
    };
}

function fetchingAllUsersError(message) {
    return {
        type: USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
        userFetchError: message
    };
}

function savingUser() {
    return {
        type: USER_MANAGEMENT_USER_SAVING
    };
}

function savedUser() {
    return {
        type: USER_MANAGEMENT_USER_SAVED
    };
}

function saveUserErrorMessage(message) {
    return {
        type: USER_MANAGEMENT_USER_SAVE_ERROR,
        userSaveError: message
    };
}

function saveUserError({ message, errors }) {
    return {
        type: USER_MANAGEMENT_USER_SAVE_ERROR,
        userSaveError: message,
        errors

    };
}

function deletingUser() {
    return {
        type: USER_MANAGEMENT_USER_DELETING
    };
}

function deletedUser() {
    return {
        type: USER_MANAGEMENT_USER_DELETED
    };
}

function deletingUserErrorMessage(message) {
    return {
        type: USER_MANAGEMENT_USER_DELETE_ERROR,
        userDeleteError: message
    };
}

function deletingUserError({ message, errors }) {
    return {
        type: USER_MANAGEMENT_USER_DELETE_ERROR,
        userDeleteError: message,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS
    };
}

export function fetchUsers() {
    return (dispatch, getState) => {
        dispatch(fetchingAllUsers());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAllUsersError('You are not permitted to view this information.')));
        fetch(ConfigRequestBuilder.USER_API_URL, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        })
        .then((response) => {
            if (response.ok) {
                response.json()
                .then((jsonArray) => {
                    dispatch(fetchedAllUsers(jsonArray));
                });
            } else {
                errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                    response.json()
                    .then((json) => {
                        let message = '';
                        if (json && json.message) {
                            // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                            message = json.message.toString();
                        }
                        dispatch(fetchingAllUsersError(message));
                    });
                }));
                const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                dispatch(handler.call(response.status));
            }
        })
        .catch((error) => {
            console.log(error);
            dispatch(fetchingAllUsersError(error));
        });
    };
}

export function saveUser(user) {
    return (dispatch, getState) => {
        dispatch(savingUser());
        const { id } = user;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveUserErrorMessage('You are not permitted to perform this action.')));
        let request;
        if (id) {
            request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, id, user);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, user);
        }
        request.then((response) => {
            if (response.ok) {
                response.json()
                .then(() => {
                    dispatch(savedUser());
                });
            } else {
                response.json()
                .then((data) => {
                    const defaultHandler = () => saveUserError(data);
                    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                    dispatch(handler.call(response.status));
                });
            }
        })
        .catch(console.error);
    };
}

export function deleteUser(userId) {
    return (dispatch, getState) => {
        dispatch(deletingUser());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => deletingUserErrorMessage('You are not permitted to perform this action.')));
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, userId);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletedUser());
            } else {
                response.json()
                .then((data) => {
                    const defaultHandler = () => deletingUserError(data);
                    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                    dispatch(handler.call(response.status));
                });
            }
        })
        .catch(console.error);
    };
}

export function clearUserFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
