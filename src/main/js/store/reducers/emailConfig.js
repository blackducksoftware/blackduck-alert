import {
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_HIDE_ADVANCED,
    EMAIL_CONFIG_SHOW_ADVANCED,
    EMAIL_CONFIG_UPDATE_ERROR,
    EMAIL_CONFIG_UPDATED,
    EMAIL_CONFIG_UPDATING,
    EMAIL_GROUPS_FETCH_ERROR,
    EMAIL_GROUPS_FETCHED,
    EMAIL_GROUPS_FETCHING,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    fetchingGroups: false,
    showAdvanced: false,
    updateStatus: null,
    groups: [],
    error: {
        message: '',
        fieldErrors: []
    },
    id: null
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
                ...action.config,
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

        case EMAIL_GROUPS_FETCHING:
            return Object.assign({}, state, {
                fetchingGroups: true,
                groups: [],
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case EMAIL_GROUPS_FETCHED:
            return Object.assign({}, state, {
                fetchingGroups: false,
                groups: action.groups,
                error: {
                    message: '',
                    fieldErrors: []
                }
            });

        case EMAIL_GROUPS_FETCH_ERROR:
            return Object.assign({}, state, {
                fetchingGroups: false,
                groups: [],
                error: {
                    message: action.message,
                    fieldErrors: action.errors
                }
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
