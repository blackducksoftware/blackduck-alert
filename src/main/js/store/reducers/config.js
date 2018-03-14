import {
    CONFIG_FETCHING,
    CONFIG_FETCHED,
    CONFIG_UPDATE_ERROR,
    CONFIG_UPDATING,
    CONFIG_UPDATED,
    CONFIG_TESTING,
    CONFIG_TEST_SUCCESS,
    CONFIG_TEST_FAILED,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    updateStatus: null,
    testing: false,
    testStatus: '',
    error: {
        message: '',
        fieldErrors: []
    },
    hubApiKey: '',
    hubApiKeyIsSet: false,
    hubProxyHost: '',
    hubProxyPassword: '',
    hubProxyPasswordIsSet: false,
    hubProxyPort: '',
    hubProxyUsername: '',
    hubTimeout: 300,
    hubUrl: '',
    id: null
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: null,
                testing: false,
                testStatus: ''
            });

        case CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                testStatus: '',
                hubApiKey: action.hubApiKey || '',
                hubApiKeyIsSet: action.hubApiKeyIsSet,
                hubProxyHost: action.hubProxyHost,
                hubProxyPassword: action.hubProxyPassword,
                hubProxyPasswordIsSet: action.hubProxyPasswordIsSet,
                hubProxyPort: action.hubProxyPort,
                hubProxyUsername: action.hubProxyUsername,
                hubTimeout: Number.parseInt(action.hubTimeout, 10) || 300,
                hubUrl: action.hubUrl,
                id: action.id
            });

        case CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                error: {
                    message: action.message,
                    fieldErrors: action.errors
                }
            });
        case CONFIG_UPDATING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATING',
                testing: false,
                testStatus: '',
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case CONFIG_UPDATED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATED',
                testing: false,
                testStatus: '',
                error: {
                    message: '',
                    fieldErrors: []
                },
                hubApiKey: action.hubApiKey || '',
                hubApiKeyIsSet: action.hubApiKeyIsSet,
                hubProxyHost: action.hubProxyHost,
                hubProxyPassword: action.hubProxyPassword,
                hubProxyPasswordIsSet: action.hubProxyPasswordIsSet,
                hubProxyPort: action.hubProxyPort,
                hubProxyUsername: action.hubProxyUsername,
                hubTimeout: Number.parseInt(action.hubTimeout, 10) || 300,
                hubUrl: action.hubUrl
            });

        case CONFIG_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: true,
                testStatus: ''
            });

        case CONFIG_TEST_SUCCESS:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                testStatus: 'SUCCESS',
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case CONFIG_TEST_FAILED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                testStatus: 'FAILED',
                error: {
                    message: action.message,
                    fieldErrors: action.errors || []
                }
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
