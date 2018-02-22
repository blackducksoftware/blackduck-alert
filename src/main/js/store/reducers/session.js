import {
    SESSION_INITIALIZING,
    SESSION_LOGGING_IN,
    SESSION_LOGGED_IN,
    SESSION_LOGGED_OUT,
    SESSION_LOGIN_ERROR,
    SESSION_SHOW_ADVANCED,
    SESSION_HIDE_ADVANCED,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    loggedIn: false,
    initializing: true,
    showAdvanced: false,
    name: '',
    errorMessage: null,
    errors: []
};

const session = (state = initialState, action) => {
    switch (action.type) {
        case SESSION_INITIALIZING:
            return Object.assign({}, state, {
                initializing: true,
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
                fetching: false,
                loggedIn: true,
                initializing: false,
                name: action.name,
                errorMessage: null,
                errors: []
            });

        case SESSION_LOGGED_OUT:
            return Object.assign({}, initialState, {
                initializing: false
            });

        case SESSION_SHOW_ADVANCED:
            return Object.assign({}, state, {
                showAdvanced: true
            });

        case SESSION_HIDE_ADVANCED:
            return Object.assign({}, state, {
                showAdvanced: false
            });

        case SESSION_LOGIN_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                loggedIn: false,
                errorMessage: action.errorMessage,
                errors: action.errors
            })

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default session;
