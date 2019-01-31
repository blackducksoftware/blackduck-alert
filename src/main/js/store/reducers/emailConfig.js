import {
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_HIDE_TEST_MODAL,
    EMAIL_CONFIG_SHOW_TEST_MODAL,
    EMAIL_CONFIG_TEST_SUCCESSFUL,
    EMAIL_CONFIG_UPDATE_ERROR,
    EMAIL_CONFIG_UPDATED,
    EMAIL_CONFIG_UPDATING,
    SERIALIZE
} from 'store/actions/types';

const initialState = {
    fetching: false,
    showTestModal: false,
    updateStatus: null,
    actionMessage: null,
    config: {},
    error: {
        message: '',
        fieldErrors: {}
    }
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case EMAIL_CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                actionMessage: null,
                updateStatus: 'FETCHING'
            });

        case EMAIL_CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                actionMessage: null,
                updateStatus: 'FETCHED',
                config: action.config
            });

        case EMAIL_CONFIG_SHOW_TEST_MODAL:
            return Object.assign({}, state, {
                actionMessage: null,
                updateStatus: null,
                showTestModal: true
            });

        case EMAIL_CONFIG_HIDE_TEST_MODAL:
            return Object.assign({}, state, {
                updateStatus: null,
                actionMessage: null,
                showTestModal: false
            });

        case EMAIL_CONFIG_TEST_SUCCESSFUL:
            return Object.assign({}, state, {
                actionMessage: 'Test message sent',
                showTestModal: false,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case EMAIL_CONFIG_UPDATING:
            return Object.assign({}, state, {
                fetching: false,
                actionMessage: null,
                updateStatus: 'UPDATING',
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case EMAIL_CONFIG_UPDATED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATED',
                actionMessage: 'Update successful',
                config: action.config,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case EMAIL_CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                actionMessage: null,
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
