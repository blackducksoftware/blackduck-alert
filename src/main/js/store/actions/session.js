import { push } from 'react-router-redux';
import {
    SESSION_INITIALIZING,
    SESSION_LOGGING_IN,
    SESSION_LOGGED_IN,
    SESSION_LOGGED_OUT,
    SESSION_LOGIN_ERROR,
    SESSION_CONFIRM_LOGOUT,
    SESSION_CANCEL_LOGOUT
} from './types';

/**
 * Triggers Logging In Reducer
 * @returns {{type}}
 */
function loggingIn() {
    return {
        type: SESSION_LOGGING_IN
    };
}

function initializing() {
    return {
        type: SESSION_INITIALIZING
    };
}

/**
 * Triggers Logged In Reducer
 * @returns {{type}}
 */
function loggedIn() {
    return {
        type: SESSION_LOGGED_IN
    };
}

/**
 * Triggers Logged Out Reducer
 * @returns {{type}}
 */
function loggedOut() {
    return {
        type: SESSION_LOGGED_OUT
    };
}

function loginError(errorMessage, errors) {
    return {
        type: SESSION_LOGIN_ERROR,
        errorMessage,
        errors: errors || []
    };
}

export function verifyLogin() {
    return (dispatch) => {
        dispatch(initializing());
        fetch('/api/verify', {
            credentials: 'include'
        }).then(function(response) {
            if (!response.ok) {
                dispatch(loggedOut());
            } else {
                dispatch(loggedIn());
            }
        }).catch(function(error) {
            // TODO: Dispatch Error
            console.log(error);
        });
    };
}

export function login(username, password) {
    return (dispatch) => {
        dispatch(loggingIn());

        const body = {
            hubUsername: username,
            hubPassword: password
        };

        fetch('/api/login', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        }).then(function(response) {
            if (response.ok) {
                dispatch(loggedIn());
            } else {
                // Need to parse out field errors
                dispatch(loginError(response.statusText, []));
            }
        })
        .catch((error) => {
            dispatch(loginError(error.message));
            console.log(error);
        });
    }
}

export function confirmLogout() {
    console.log('confirming logout');
    return {
        type: SESSION_CONFIRM_LOGOUT
    };
}

export function cancelLogout() {
    return {
        type: SESSION_CANCEL_LOGOUT
    };
}

export function logout() {
    return (dispatch) => {
        //dispatch(loggingOut());

        fetch('/api/logout', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(function(response) {
            if (response.ok) {
                dispatch(loggedOut());
                dispatch(push('/'));
            }
        }).catch(function(error) {
            console.log(error);
        });
    };
}
