import {CONFIG_FETCHED, CONFIG_FETCHING, CONFIG_TEST_FAILED, CONFIG_TEST_SUCCESS, CONFIG_TESTING, CONFIG_UPDATE_ERROR, CONFIG_UPDATED, CONFIG_UPDATING, SERIALIZE} from '../actions/types';

const initialState = {
    fetching: false,
    updateStatus: null,
    testing: false,
    testStatus: '',
    error: {
        message: '',
        fieldErrors: []
    },
    blackDuckApiKey: '',
    blackDuckApiKeyIsSet: false,
    blackDuckProxyHost: '',
    blackDuckProxyPassword: '',
    blackDuckProxyPasswordIsSet: false,
    blackDuckProxyPort: '',
    blackDuckProxyUsername: '',
    blackDuckTimeout: 300,
    blackDuckUrl: '',
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
                updateStatus: 'FETCHED',
                testing: false,
                testStatus: '',
                blackDuckApiKey: action.blackDuckApiKey || '',
                blackDuckApiKeyIsSet: action.blackDuckApiKeyIsSet,
                blackDuckProxyHost: action.blackDuckProxyHost,
                blackDuckProxyPassword: action.blackDuckProxyPassword,
                blackDuckProxyPasswordIsSet: action.blackDuckProxyPasswordIsSet,
                blackDuckProxyPort: action.blackDuckProxyPort,
                blackDuckProxyUsername: action.blackDuckProxyUsername,
                blackDuckTimeout: Number.parseInt(action.blackDuckTimeout, 10) || 300,
                blackDuckUrl: action.blackDuckUrl,
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
                blackDuckApiKey: action.blackDuckApiKey || '',
                blackDuckApiKeyIsSet: action.blackDuckApiKeyIsSet,
                blackDuckProxyHost: action.blackDuckProxyHost,
                blackDuckProxyPassword: action.blackDuckProxyPassword,
                blackDuckProxyPasswordIsSet: action.blackDuckProxyPasswordIsSet,
                blackDuckProxyPort: action.blackDuckProxyPort,
                blackDuckProxyUsername: action.blackDuckProxyUsername,
                blackDuckTimeout: Number.parseInt(action.blackDuckTimeout, 10) || 300,
                blackDuckUrl: action.blackDuckUrl
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
