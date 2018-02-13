import {
    CONFIG_FETCHING,
    CONFIG_FETCHED,
    CONFIG_UPDATING,
    CONFIG_UPDATED,
    CONFIG_TESTING,
    CONFIG_TEST_SUCCESS,
    CONFIG_TEST_FAILED,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    updating: false,
    testing: false,
    testStatus: '',
    error: {
        message: '',
        fieldErrors: []
    },
    hubAlwaysTrustCertificate: true,
    hubApiKey: '',
    hubApiKeyIsSet: false,
    hubProxyHost: '',
    hubProxyPassword: '',
    hubProxyPasswordIsSet: false,
    hubProxyPort: '',
    hubProxyUsername: '',
    hubTimeout: 60,
    hubUrl: '',
    id: null
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updating: false,
                testing: false,
                testStatus: '',
            });

        case CONFIG_FETCHED:
            console.log('hub url---->', action.hubUrl);
            return Object.assign({}, state, {
                fetching: false,
                updating: false,
                testing: false,
                testStatus: '',
                hubAlwaysTrustCertificate: (action.hubAlwaysTrustCertificate == 'true'),
                hubApiKey: action.hubApiKey || '',
                hubApiKeyIsSet: action.hubApiKeyIsSet,
                hubProxyHost: action.hubProxyHost,
                hubProxyPassword: action.hubProxyPassword,
                hubProxyPasswordIsSet: action.hubProxyPasswordIsSet,
                hubProxyPort: action.hubProxyPort,
                hubProxyUsername: action.hubProxyUsername,
                hubTimeout: Number.parseInt(action.hubTimeout) || 60,
                hubUrl: action.hubUrl,
                id: action.id
            });

        case CONFIG_UPDATING:
            return Object.assign({}, state, {
                fetching: false,
                updating: true,
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
                updating: false,
                testing: false,
                testStatus: '',
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case CONFIG_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                updating: false,
                testing: true,
                testStatus: ''
            });

        case CONFIG_TEST_SUCCESS:
            return Object.assign({}, state, {
                fetching: false,
                updating: false,
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
                updating: false,
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
