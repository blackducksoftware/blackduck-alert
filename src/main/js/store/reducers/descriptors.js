import {DESCRIPTORS_DISTRIBUTION_FETCH_ERROR, DESCRIPTORS_DISTRIBUTION_FETCHED, DESCRIPTORS_DISTRIBUTION_FETCHING, DESCRIPTORS_FETCH_ERROR, DESCRIPTORS_FETCHED, DESCRIPTORS_FETCHING, SERIALIZE} from '../actions/types';

const initialState = {
    fetching: false,
    error: {
        message: ''
    },
    // may need to rethink this state object
    currentDistributionComponents: null,
    items: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DESCRIPTORS_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                error: {
                    message: ''
                }
            });

        case DESCRIPTORS_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                error: {
                    message: ''
                },
                items: {
                    ...state.items,
                    ...action.items
                }
            });

        case DESCRIPTORS_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                error: {
                    message: action.message
                }
            });

        case DESCRIPTORS_DISTRIBUTION_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                currentDistributionComponents: null,
                error: {
                    message: ''
                }
            });

        case DESCRIPTORS_DISTRIBUTION_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                currentDistributionComponents: action.currentDistributionComponents
            });

        case DESCRIPTORS_DISTRIBUTION_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                currentDistributionComponents: null,
                error: {
                    message: action.message,
                    error: action.error
                }
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;

    }
};

export default config;
