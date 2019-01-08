import { SERIALIZE, SESSION_CANCEL_LOGOUT, SESSION_CONFIRM_LOGOUT, SESSION_INITIALIZING, SESSION_LOGGED_IN, SESSION_LOGGED_OUT, SESSION_LOGGING_IN, SESSION_LOGIN_ERROR } from 'store/actions/types';

const initialState = {
    csrfToken: null,
    fetching: false,
    loggedIn: false,
    initializing: true,
    showLogoutConfirm: false,
    name: '',
    errorMessage: null,
    errors: []
};

const session = (state = initialState, action) => {
    switch (action.type) {
        case SESSION_INITIALIZING:
            return Object.assign({}, state, {
                initializing: true
            });

        case SESSION_LOGGING_IN:
            return Object.assign({}, state, {
                fetching: true,
                loggedIn: false,
                name: '',
                errorMessage: null,
                errors: []
            });

        case SESSION_LOGGED_IN:
            return Object.assign({}, state, {
                csrfToken: action.csrfToken,
                fetching: false,
                loggedIn: true,
                initializing: false,
                name: action.name,
                errorMessage: null,
                errors: []
            });

        case SESSION_LOGGED_OUT:
            return Object.assign({}, initialState, {
                initializing: false,
                loggedIn: false,
                showLogoutConfirm: false
            });

        case SESSION_LOGIN_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                loggedIn: false,
                errorMessage: action.errorMessage,
                errors: action.errors
            });

        case SESSION_CANCEL_LOGOUT:
            return Object.assign({}, state, {
                showLogoutConfirm: false
            });

        case SESSION_CONFIRM_LOGOUT:
            return Object.assign({}, state, {
                showLogoutConfirm: true
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default session;
