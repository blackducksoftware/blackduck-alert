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
function loggedIn(data) {
    return {
        type: SESSION_LOGGED_IN,
        csrfToken: data.csrfToken
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
    return (dispatch, getState) => {
        const csrfToken = getState().session.csrfToken;
        dispatch(initializing());
        fetch('/api/verify', {
            credentials: 'include',
            headers: {
              'X-CSRF-TOKEN': csrfToken
            }
        }).then(function(response) {
            if (!response.ok) {
                dispatch(loggedOut());
            } else {
                const token = getState().session.csrfToken;
                dispatch(loggedIn({csrfToken: token}));
            }
        }).catch((error) => {
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
        }).then((response) => {
            if (response.ok) {
                const token = response.headers.get('X-CSRF-TOKEN');
                dispatch(loggedIn({csrfToken: token}));
            } else {
                // Need to parse out field errors
                dispatch(loginError(response.statusText, []));
            }
        })
            .catch((error) => {
                dispatch(loginError(error.message));
                console.log(error);
            });
    };
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
    return (dispatch, getState) => {
        //dispatch(loggingOut());
        const csrfToken = getState().session.csrfToken;
        fetch('/api/logout', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        }).then((response) => {
            dispatch(loggedOut());
            dispatch(push('/'));
        }).catch((error) => {
            console.log(error);
        });
    };
}
