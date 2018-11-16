import {
    SERIALIZE,
    SYSTEM_LATEST_MESSAGES_FETCH_ERROR,
    SYSTEM_LATEST_MESSAGES_FETCHED,
    SYSTEM_LATEST_MESSAGES_FETCHING,
    SYSTEM_SETUP_FETCH_ERROR,
    SYSTEM_SETUP_FETCH_REDIRECTED,
    SYSTEM_SETUP_FETCHED,
    SYSTEM_SETUP_FETCHING,
    SYSTEM_SETUP_UPDATE_ERROR,
    SYSTEM_SETUP_UPDATED,
    SYSTEM_SETUP_UPDATING
} from "../actions/types";

const initialState = {
    fetching: false,
    fetchingSetup: false,
    updateStatus: null,
    latestMessages: [],
    errorMessage: '',
    setupData: {},
    setupRedirect: false,
    error: {
        message: '',
        fieldErrors: []
    }
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case SYSTEM_LATEST_MESSAGES_FETCHING:
            return Object.assign({}, state, {
                fetching: true
            });
        case SYSTEM_LATEST_MESSAGES_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                latestMessages: action.latestMessages
            });
        case SYSTEM_LATEST_MESSAGES_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false
            });
        case SYSTEM_SETUP_FETCHING:
            return Object.assign({}, state, {
                fetchingSetup: true,
                updateStatus: 'FETCHING'
            });
        case SYSTEM_SETUP_FETCH_REDIRECTED:
            return Object.assign({}, state, {
                fetchingSetup: false,
                setupRedirect: true
            });
        case SYSTEM_SETUP_FETCHED:
            return Object.assign({}, state, {
                fetchingSetup: false,
                updateStatus: 'FETCHED',
                setupData: action.setupData
            });
        case SYSTEM_SETUP_FETCH_ERROR:
            return Object.assign({}, state, {
                fetchingSetup: false,
                updateStatus: 'ERROR',
                error: {
                    message: action.message,
                    ...action.errors,
                }
            });
        case SYSTEM_SETUP_UPDATING:
            return Object.assign({}, state, {
                fetchingSetup: true,
                updateStatus: 'UPDATING'
            });
        case SYSTEM_SETUP_UPDATED:
            return Object.assign({}, state, {
                fetchingSetup: false,
                updateStatus: 'UPDATED'
            });
        case SYSTEM_SETUP_UPDATE_ERROR:
            return Object.assign({}, state, {
                fetchingSetup: false,
                updateStatus: 'ERROR',
                error: {
                    message: action.message,
                    ...action.errors,
                }
            });
        case SERIALIZE:
            return initialState;
        default:
            return state;
    }
}

export default config;
