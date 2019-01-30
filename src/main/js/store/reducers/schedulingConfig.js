import {
    SCHEDULING_ACCUMULATOR_ERROR,
    SCHEDULING_ACCUMULATOR_RUNNING,
    SCHEDULING_ACCUMULATOR_SUCCESS,
    SCHEDULING_CONFIG_FETCH_ERROR,
    SCHEDULING_CONFIG_FETCHED,
    SCHEDULING_CONFIG_FETCHING,
    SCHEDULING_CONFIG_UPDATE_ERROR,
    SCHEDULING_CONFIG_UPDATED,
    SCHEDULING_CONFIG_UPDATING,
    SERIALIZE
} from 'store/actions/types';

const initialState = {
    fetching: false,
    updateStatus: null,
    error: {
        message: '',
        fieldErrors: {}
    },
    config: {}
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case SCHEDULING_CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: ''
            });

        case SCHEDULING_CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                error: {
                    message: '',
                    fieldErrors: {}
                },
                config: action.config
            });

        case SCHEDULING_CONFIG_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                error: {
                    message: action.message
                }
            });

        case SCHEDULING_CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                error: {
                    message: action.message,
                    fieldErrors: action.errors
                }
            });

        case SCHEDULING_CONFIG_UPDATING:
            return Object.assign({}, state, {
                updateStatus: 'UPDATING',
                error: {
                    message: '',
                    fieldErrors: {}
                }
            });

        case SCHEDULING_CONFIG_UPDATED:
            return Object.assign({}, state, {
                updateStatus: 'UPDATED',
                error: {
                    message: '',
                    fieldErrors: {}
                },
                config: action.config
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
