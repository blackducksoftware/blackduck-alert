import {
    HIPCHAT_CONFIG_FETCHED,
    HIPCHAT_CONFIG_FETCHING,
    HIPCHAT_CONFIG_HIDE_TEST_MODAL,
    HIPCHAT_CONFIG_SHOW_TEST_MODAL,
    HIPCHAT_CONFIG_TEST_FAILED,
    HIPCHAT_CONFIG_TEST_SUCCESS,
    HIPCHAT_CONFIG_TESTING,
    HIPCHAT_CONFIG_UPDATE_ERROR,
    HIPCHAT_CONFIG_UPDATED,
    HIPCHAT_CONFIG_UPDATING,
    SERIALIZE
} from 'store/actions/types';

const initialState = {
    updateStatus: null,
    actionMessage: null,
    testing: false,
    error: {
        message: '',
        fieldErrors: {}
    },
    config: {},
    showTestModal: false
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case HIPCHAT_CONFIG_FETCHING:
            return Object.assign({}, state, {
                updateStatus: 'FETCHING',
                testing: false
            });

        case HIPCHAT_CONFIG_FETCHED:
            return Object.assign({}, state, {
                updateStatus: 'FETCHED',
                testing: false,
                apiKeyIsSet: action.apiKeyIsSet,
                apiKey: action.apiKey,
                hostServer: action.hostServer,
                id: action.id
            });

        case HIPCHAT_CONFIG_UPDATING:
            return Object.assign({}, state, {
                updateStatus: 'UPDATING',
                testing: false,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case HIPCHAT_CONFIG_UPDATED:
            return Object.assign({}, state, {
                updateStatus: 'UPDATED',
                actionMessage: 'Update successful',
                testing: false,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case HIPCHAT_CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                actionMessage: null,
                error: {
                    message: action.message,
                    fieldErrors: action.errors || {}
                }
            });


        case HIPCHAT_CONFIG_TESTING:
            return Object.assign({}, state, {
                updateStatus: null,
                testing: true
            });

        case HIPCHAT_CONFIG_SHOW_TEST_MODAL:
            return Object.assign({}, state, {
                updateStatus: null,
                actionMessage: null,
                showTestModal: true
            });

        case HIPCHAT_CONFIG_HIDE_TEST_MODAL:
            return Object.assign({}, state, {
                updateStatus: null,
                actionMessage: null,
                showTestModal: false,
                testing: true
            });

        case HIPCHAT_CONFIG_TEST_SUCCESS:
            return Object.assign({}, state, {
                updateStatus: null,
                actionMessage: 'Test message sent',
                testing: false,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case HIPCHAT_CONFIG_TEST_FAILED:
            return Object.assign({}, state, {
                updateStatus: null,
                actionMessage: null,
                testing: false,
                error: {
                    message: action.message,
                    fieldErrors: action.errors || {}
                }
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
