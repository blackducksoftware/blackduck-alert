import { SAML_ENABLED, SERIALIZE, SESSION_CANCEL_LOGOUT, SESSION_CONFIRM_LOGOUT, SESSION_INITIALIZING, SESSION_LOGGED_IN, SESSION_LOGGED_OUT, SESSION_LOGGING_IN, SESSION_LOGIN_ERROR, SESSION_LOGOUT } from 'store/actions/types';

const initialState = {
    csrfToken: null,
    fetching: false,
    loggedIn: false,
    logoutPerformed: false,
    initializing: true,
    showLogoutConfirm: false,
    name: '',
    samlEnabled: false,
    errorMessage: null,
    errors: []
};

const session = (state = initialState, action) => {
    switch (action.type) {
        case SESSION_INITIALIZING:
            return { ...state, initializing: true };

        case SESSION_LOGGING_IN:
            return {
                ...state,
                fetching: true,
                loggedIn: false,
                name: '',
                errorMessage: null,
                errors: []
            };

        case SESSION_LOGGED_IN:
            return {
                ...state,
                csrfToken: action.csrfToken,
                fetching: false,
                loggedIn: true,
                initializing: false,
                name: action.name,
                errorMessage: null,
                errors: []
            };

        case SESSION_LOGGED_OUT:
            return {
                ...initialState,
                initializing: false,
                samlEnabled: state.samlEnabled,
                loggedIn: false,
                showLogoutConfirm: false,
                errorMessage: null,
                errors: []
            };

        case SESSION_LOGIN_ERROR:
            return {
                ...state,
                fetching: false,
                loggedIn: false,
                errorMessage: action.errorMessage,
                errors: action.errors
            };

        case SESSION_CANCEL_LOGOUT:
            return { ...state, showLogoutConfirm: false };

        case SESSION_CONFIRM_LOGOUT:
            return { ...state, showLogoutConfirm: true };
        case SESSION_LOGOUT:
            return { ...state, logoutPerformed: true };
        case SAML_ENABLED:
            return { ...state, samlEnabled: action.saml_enabled };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default session;
