import {
    USER_MANAGEMENT_USER_DELETE_ERROR,
    USER_MANAGEMENT_USER_DELETED,
    USER_MANAGEMENT_USER_DELETING,
    USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
    USER_MANAGEMENT_USER_FETCHED_ALL,
    USER_MANAGEMENT_USER_FETCHING_ALL,
    USER_MANAGEMENT_USER_SAVE_ERROR,
    USER_MANAGEMENT_USER_SAVED,
    USER_MANAGEMENT_USER_SAVING
} from 'store/actions/types'
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import { verifyLoginByStatus } from 'store/actions/session';

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

function saveUserError(message) {
    return {
        type: USER_MANAGEMENT_USER_SAVE_ERROR,
        userSaveError: message
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

function deletingUserError(message) {
    return {
        type: USER_MANAGEMENT_USER_DELETE_ERROR,
        userDeleteError: message
    };
}

export function fetchUsers() {
    return (dispatch, getState) => {
        dispatch(fetchingAllUsers());
        const { csrfToken } = getState().session;
        fetch(ConfigRequestBuilder.USER_API_URL, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            if (response.ok) {
                response.json().then((jsonArray) => {
                    dispatch(fetchedAllUsers(jsonArray));
                });
            } else {
                switch (response.status) {
                    case 401:
                    case 403:
                        dispatch(verifyLoginByStatus(response.status));
                        break;
                    default:
                        response.json().then((json) => {
                            let message = '';
                            if (json && json.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = json.message.toString();
                            }
                            dispatch(fetchingAllUsersError(message));
                        });
                }
            }
        }).catch((error) => {
            console.log(error);
            dispatch(fetchingAllUsersError(error));
        });
    };
}

export function createNewUser(user) {
    return (dispatch, getState) => {
        dispatch(savingUser());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, user);
        request.then((response) => {
            if (response.ok) {
                response.json().then(() => {
                    dispatch(savedUser());
                });
            } else {
                response.json()
                    .then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(saveUserError(data.message));
                            case 401:
                                dispatch(saveUserError(data.message));
                                return dispatch(verifyLoginByStatus(response.status));
                            case 412:
                                return dispatch(saveUserError(data.message));
                            default: {
                                return dispatch(saveUserError(data.message, null));
                            }
                        }
                    });
            }
        }).catch(console.error);
    };
}

export function deleteUser(user) {
    return (dispatch, getState) => {
        const { userName } = user;
        dispatch(deletingUser());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.USER_API_URL, csrfToken, userName);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletedUser());
            } else {
                response.json()
                    .then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(deletingUserError(data.message));
                            case 401:
                                dispatch(deletingUserError(data.message));
                                return dispatch(verifyLoginByStatus(response.status));
                            case 412:
                                return dispatch(deletingUserError(data.message));
                            default: {
                                return dispatch(deletingUserError(data.message, null));
                            }
                        }
                    });
            }
        }).catch(console.error);
    };
}
