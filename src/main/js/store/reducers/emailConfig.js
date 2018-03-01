import {
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_SHOW_ADVANCED,
    EMAIL_CONFIG_HIDE_ADVANCED,
    EMAIL_CONFIG_UPDATE_ERROR,
    EMAIL_CONFIG_UPDATING,
    EMAIL_CONFIG_UPDATED,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    showAdvanced: false,
    updateStatus: null,
    error: {
        message: '',
        fieldErrors: []
    }
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case EMAIL_CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: 'FETCHING'
            });

        case EMAIL_CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'FETCHED',
                ...action.config
            });

        case EMAIL_CONFIG_SHOW_ADVANCED:
            return Object.assign({}, state, {
                showAdvanced: true
            });

        case EMAIL_CONFIG_HIDE_ADVANCED:
            return Object.assign({}, state, {
                showAdvanced: false
            });

        case EMAIL_CONFIG_UPDATING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATING',
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case EMAIL_CONFIG_UPDATED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATED',
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case EMAIL_CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
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
