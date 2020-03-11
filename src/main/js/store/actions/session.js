import { push } from 'react-router-redux';
import { SAML_ENABLED, SESSION_CANCEL_LOGOUT, SESSION_CONFIRM_LOGOUT, SESSION_INITIALIZING, SESSION_LOGGED_IN, SESSION_LOGGED_OUT, SESSION_LOGGING_IN, SESSION_LOGIN_ERROR, SESSION_LOGOUT } from 'store/actions/types';

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

function samlEnabled(enabled) {
    return {
        type: SAML_ENABLED,
        saml_enabled: enabled
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

function logOut() {
    return {
        type: SESSION_LOGOUT
    };
}

export function loginError(errorMessage, errors) {
    return {
        type: SESSION_LOGIN_ERROR,
        errorMessage,
        errors: errors || []
    };
}

export function clearLoginError() {
    return (dispatch) => {
        dispatch(loggedOut());
    };
}

export function verifyLogin() {
    return (dispatch) => {
        dispatch(initializing());
        fetch('/alert/api/verify', {
            credentials: 'same-origin'
        }).then((response) => {
            if (!response.ok) {
                dispatch(loggedOut());
            } else {
                const token = response.headers.get('X-CSRF-TOKEN');
                dispatch(loggedIn({ csrfToken: token }));
            }
        }).catch(error => console.log(error));
    };
}

export function verifySaml() {
    return (dispatch) => {
        dispatch(initializing());
        fetch('/alert/api/verify/saml', {
            credentials: 'same-origin'
        }).then((response) => {
            if (!response.ok) {
                dispatch(loggedOut());
            } else {
                response.json().then(body => {
                    const { saml_enabled } = body;
                    dispatch(samlEnabled(Boolean(saml_enabled)));
                });
            }
        }).catch(error => console.log(error))
    };
}

export function login(username, password) {
    return (dispatch) => {
        dispatch(loggingIn());

        const body = {
            blackDuckUsername: username,
            blackDuckPassword: password
        };

        fetch('/alert/api/login', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        }).then((response) => {
            if (response.ok) {
                const token = response.headers.get('X-CSRF-TOKEN');
                dispatch(loggedIn({ csrfToken: token }));
            } else {
                dispatch(loginError('Login Failed.', []));
            }
        }).catch((error) => {
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
        // dispatch(loggingOut());
        const { csrfToken } = getState().session;
        fetch('/alert/api/logout', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then(() => dispatch(loggedOut()))
        .then(() => dispatch(logOut()))
        .then(() => dispatch(push('/alert')))
        .catch((error) => {
            console.log(error);
        });
    };
}

export function verifyLoginByStatus(status) {
    return (dispatch) => {
        switch (status) {
            case 401:
                return dispatch(loggedOut());
        }
    };
}
