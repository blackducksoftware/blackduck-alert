import {
    USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_ROLE_DELETE_ERROR,
    USER_MANAGEMENT_ROLE_DELETED,
    USER_MANAGEMENT_ROLE_DELETING,
    USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL,
    USER_MANAGEMENT_ROLE_FETCHED_ALL,
    USER_MANAGEMENT_ROLE_FETCHING_ALL,
    USER_MANAGEMENT_ROLE_SAVE_ERROR,
    USER_MANAGEMENT_ROLE_SAVED,
    USER_MANAGEMENT_ROLE_SAVING
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

function fetchingAllRoles() {
    return {
        type: USER_MANAGEMENT_ROLE_FETCHING_ALL
    };
}

function fetchedAllRoles(roles) {
    return {
        type: USER_MANAGEMENT_ROLE_FETCHED_ALL,
        data: roles
    };
}

function fetchingAllRolesError(message) {
    return {
        type: USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL,
        message
    };
}

function savingRole() {
    return {
        type: USER_MANAGEMENT_ROLE_SAVING
    };
}

function savedRole() {
    return {
        type: USER_MANAGEMENT_ROLE_SAVED
    };
}

function saveRoleErrorMessage(message) {
    return {
        type: USER_MANAGEMENT_ROLE_SAVE_ERROR,
        roleError: message
    };
}

function saveRoleError({ message, errors }) {
    return {
        type: USER_MANAGEMENT_ROLE_SAVE_ERROR,
        roleError: message,
        errors
    };
}

function deletingRole() {
    return {
        type: USER_MANAGEMENT_ROLE_DELETING
    };
}

function deletedRole() {
    return {
        type: USER_MANAGEMENT_ROLE_DELETED
    };
}

function deletingRoleErrorMessage(message) {
    return {
        type: USER_MANAGEMENT_ROLE_DELETE_ERROR,
        roleError: message
    };
}

function deletingRoleError({ message, errors }) {
    return {
        type: USER_MANAGEMENT_ROLE_DELETE_ERROR,
        roleError: message,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS
    };
}

export function fetchRoles() {
    return (dispatch, getState) => {
        dispatch(fetchingAllRoles());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAllRolesError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        fetch(ConfigRequestBuilder.ROLE_API_URL, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(fetchedAllRoles(responseData));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                                let message = '';
                                if (responseData && responseData.message) {
                                    // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                    message = responseData.message.toString();
                                }
                                return fetchingAllRolesError(message);
                            }));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch((error) => {
                console.log(error);
                dispatch(fetchingAllRolesError(error));
            });
    };
}

export function saveRole(role) {
    return (dispatch, getState) => {
        dispatch(savingRole());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveRoleErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const { id } = role;
        let request;
        if (id) {
            request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.ROLE_API_URL, csrfToken, id, role);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.ROLE_API_URL, csrfToken, role);
        }
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(savedRole());
                        dispatch(fetchRoles());
                    } else {
                        const defaultHandler = () => saveRoleError(responseData);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        })
            .catch(console.error);
    };
}

export function deleteRole(roleId) {
    return (dispatch, getState) => {
        dispatch(deletingRole());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => deletingRoleErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.ROLE_API_URL, csrfToken, roleId);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(deletedRole());
                    } else {
                        const defaultHandler = () => deletingRoleError(responseData);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        })
            .catch(console.error);
    };
}

export function clearRoleFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
