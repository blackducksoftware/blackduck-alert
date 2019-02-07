import {
    SERIALIZE,
    SYSTEM_LATEST_MESSAGES_FETCH_ERROR,
    SYSTEM_LATEST_MESSAGES_FETCHED,
    SYSTEM_LATEST_MESSAGES_FETCHING,
    SYSTEM_SETUP_FETCH_ERROR,
    SYSTEM_SETUP_FETCH_REDIRECTED,
    SYSTEM_SETUP_FETCHED,
    SYSTEM_SETUP_FETCHING,
    SYSTEM_SETUP_HIDE_RESET_PASSWORD_MODAL,
    SYSTEM_SETUP_PASSWORD_RESETTING,
    SYSTEM_SETUP_SHOW_RESET_PASSWORD_MODAL,
    SYSTEM_SETUP_UPDATE_ERROR,
    SYSTEM_SETUP_UPDATED,
    SYSTEM_SETUP_UPDATING
} from 'store/actions/types';

const initialState = {
    fetching: false,
    resettingPassword: false,
    fetchingSetupStatus: '',
    updateStatus: '',
    latestMessages: [],
    errorMessage: '',
    settingsData: {},
    setupRedirect: false,
    showPasswordResetModal: false,
    error: {
        message: ''
    }
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case SYSTEM_LATEST_MESSAGES_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: ''
            });
        case SYSTEM_LATEST_MESSAGES_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                latestMessages: action.latestMessages,
                updateStatus: ''
            });
        case SYSTEM_LATEST_MESSAGES_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: ''
            });
        case SYSTEM_SETUP_FETCHING:
            return Object.assign({}, state, {
                resettingPassword: false,
                fetchingSetupStatus: SYSTEM_SETUP_FETCHING,
                updateStatus: 'FETCHING'
            });
        case SYSTEM_SETUP_FETCH_REDIRECTED:
            return Object.assign({}, state, {
                resettingPassword: false,
                fetchingSetupStatus: '',
                setupRedirect: true,
                updateStatus: '',
                error: {}
            });
        case SYSTEM_SETUP_FETCHED:
            return Object.assign({}, state, {
                resettingPassword: false,
                fetchingSetupStatus: SYSTEM_SETUP_FETCHED,
                updateStatus: 'FETCHED',
                settingsData: action.settingsData,
                error: {}
            });
        case SYSTEM_SETUP_FETCH_ERROR:
            return Object.assign({}, state, {
                resettingPassword: false,
                updateStatus: 'ERROR',
                fetchingSetupStatus: SYSTEM_SETUP_FETCH_ERROR,
                error: {
                    message: action.message,
                    ...action.errors
                }
            });
        case SYSTEM_SETUP_UPDATING:
            return Object.assign({}, state, {
                resettingPassword: false,
                fetchingSetupStatus: SYSTEM_SETUP_UPDATING,
                updateStatus: 'UPDATING'
            });
        case SYSTEM_SETUP_UPDATED:
            return Object.assign({}, state, {
                resettingPassword: false,
                fetchingSetupStatus: SYSTEM_SETUP_UPDATED,
                updateStatus: 'UPDATED',
                error: {}
            });
        case SYSTEM_SETUP_UPDATE_ERROR:
            return Object.assign({}, state, {
                resettingPassword: false,
                fetchingSetupStatus: SYSTEM_SETUP_UPDATE_ERROR,
                updateStatus: 'ERROR',
                error: {
                    message: action.message,
                    ...action.errors
                }
            });
        case SYSTEM_SETUP_SHOW_RESET_PASSWORD_MODAL:
            return Object.assign({}, state, {
                resettingPassword: false,
                showPasswordResetModal: true,
                updateStatus: '',
                errorMessage: '',
                error: {}
            });
        case SYSTEM_SETUP_HIDE_RESET_PASSWORD_MODAL:
            return Object.assign({}, state, {
                resettingPassword: false,
                showPasswordResetModal: false,
                updateStatus: '',
                errorMessage: '',
                error: {}
            });
        case SYSTEM_SETUP_PASSWORD_RESETTING:
            return Object.assign({}, state, {
                resettingPassword: true,
                updateStatus: '',
                errorMessage: '',
                error: {}
            });
        case SERIALIZE:
            return initialState;
        default:
            return state;
    }
};

export default config;
