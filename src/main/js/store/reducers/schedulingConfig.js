import {
    SCHEDULING_CONFIG_FETCHING,
    SCHEDULING_CONFIG_FETCHED,
    SCHEDULING_CONFIG_UPDATE_ERROR,
    SCHEDULING_CONFIG_UPDATING,
    SCHEDULING_CONFIG_UPDATED,
    SCHEDULING_ACCUMULATOR_ERROR,
    SCHEDULING_ACCUMULATOR_RUNNING,
    SCHEDULING_ACCUMULATOR_SUCCESS,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    updateStatus: null,
    errorMessage: null,
    errorFields: [],
    accumulating: false,
    accumulatorError: null
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
                ...action.config
            });

        case SCHEDULING_CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                errorMessage: action.message,
                errorFields: action.errors
            });

        case SCHEDULING_CONFIG_UPDATING:
            return Object.assign({}, state, {
                updateStatus: 'UPDATING',
                errorMessage: null,
                errorFields: null

            });

        case SCHEDULING_CONFIG_UPDATED:
            return Object.assign({}, state, {
                updateStatus: 'UPDATED',
            });

        case SCHEDULING_ACCUMULATOR_RUNNING:
            return Object.assign({}, state, {
                accumulating: true
            });

        case SCHEDULING_ACCUMULATOR_ERROR:
            return Object.assign({}, state, {
                accumulating: false,
                accumulatorError: action.accumulatorError
            });

        case SCHEDULING_ACCUMULATOR_SUCCESS:
            return Object.assign({}, state, {
                accumulating: false,
                accumulatorError: null
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
