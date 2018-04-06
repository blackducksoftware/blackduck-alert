import {
    HIPCHAT_CONFIG_FETCHING,
    HIPCHAT_CONFIG_FETCHED,
    HIPCHAT_CONFIG_UPDATE_ERROR,
    HIPCHAT_CONFIG_UPDATING,
    HIPCHAT_CONFIG_UPDATED,
    HIPCHAT_CONFIG_TESTING,
    HIPCHAT_CONFIG_TEST_SUCCESS,
    HIPCHAT_CONFIG_TEST_FAILED,
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
    apiKeyIsSet: false,
    apiKey: null,
    id: null
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case HIPCHAT_CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: null,
                testing: false,
                testStatus: ''
            });

        case HIPCHAT_CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'FETCHED',
                testing: false,
                testStatus: '',
                apiKeyIsSet: action.apiKeyIsSet,
                apiKey: action.apiKey,
                id: action.id
            });

        case HIPCHAT_CONFIG_UPDATING:
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

        case HIPCHAT_CONFIG_UPDATED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATED',
                testing: false,
                testStatus: '',
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case HIPCHAT_CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                error: {
                    message: action.message,
                    fieldErrors: action.errors || []
                }
            });


        case HIPCHAT_CONFIG_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: true,
                testStatus: ''
            });

        case HIPCHAT_CONFIG_TEST_SUCCESS:
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

        case HIPCHAT_CONFIG_TEST_FAILED:
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
