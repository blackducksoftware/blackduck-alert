import { SERIALIZE, SYSTEM_LATEST_MESSAGES_FETCH_ERROR, SYSTEM_LATEST_MESSAGES_FETCHED, SYSTEM_LATEST_MESSAGES_FETCHING } from 'store/actions/types';

const initialState = {
    fetching: false,
    resettingPassword: false,
    fetchingSetupStatus: '',
    updateStatus: '',
    latestMessages: [],
    errorMessage: null,
    actionMessage: null,
    settingsData: {},
    settingsDescriptor: {},
    showPasswordResetModal: false,
    error: {}
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case SYSTEM_LATEST_MESSAGES_FETCHING:
            return {
                ...state,
                actionMessage: null,
                fetching: true,
                updateStatus: ''
            };
        case SYSTEM_LATEST_MESSAGES_FETCHED:
            return {
                ...state,
                actionMessage: null,
                fetching: false,
                latestMessages: action.latestMessages,
                updateStatus: ''
            };
        case SYSTEM_LATEST_MESSAGES_FETCH_ERROR:
            return {
                ...state,
                actionMessage: null,
                fetching: false,
                updateStatus: ''
            };
        case SERIALIZE:
            return initialState;
        default:
            return state;
    }
};

export default config;
