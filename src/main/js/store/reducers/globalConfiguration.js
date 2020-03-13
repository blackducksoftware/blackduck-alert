import {
    CONFIG_ALL_FETCHED,
    CONFIG_DELETED,
    CONFIG_DELETING,
    CONFIG_FETCHED,
    CONFIG_FETCHING,
    CONFIG_REFRESH,
    CONFIG_REFRESHING,
    CONFIG_TEST_FAILED,
    CONFIG_TEST_SUCCESS,
    CONFIG_TESTING,
    CONFIG_UPDATE_ERROR,
    CONFIG_UPDATED,
    CONFIG_UPDATING,
    SERIALIZE
} from 'store/actions/types';

const initialState = {
    fetching: false,
    updateStatus: null,
    testing: false,
    actionMessage: null,
    error: {
        message: '',
        fieldErrors: {}
    },
    config: {},
    allConfigs: []
};

const globalConfiguration = (state = initialState, action) => {
    switch (action.type) {
        case CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: null,
                testing: false,
                actionMessage: null
            });
        case CONFIG_ALL_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                allConfigs: action.config,
            });
        case CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'FETCHED',
                testing: false,
                actionMessage: null,
                config: action.config,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case CONFIG_REFRESHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: null,
                testing: false
            });

        case CONFIG_REFRESH:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'FETCHED',
                testing: false,
                config: action.config
            });

        case CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                actionMessage: null,
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
                actionMessage: null,
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case CONFIG_UPDATED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATED',
                testing: false,
                actionMessage: 'Update successful',
                error: {
                    message: '',
                    fieldErrors: {}
                },
                config: action.config
            });

        case CONFIG_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: true,
                actionMessage: null
            });

        case CONFIG_TEST_SUCCESS:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                actionMessage: 'Test successful',
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case CONFIG_TEST_FAILED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                actionMessage: null,
                error: {
                    message: action.message,
                    fieldErrors: action.errors || {}
                }
            });

        case CONFIG_DELETED:
            return Object.assign({}, state, {
                updateStatus: 'DELETED',
                actionMessage: 'Delete successful',
                config: {},
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case CONFIG_DELETING:
            return Object.assign({}, state, {
                actionMessage: null,
                updateStatus: 'DELETING',
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default globalConfiguration;
