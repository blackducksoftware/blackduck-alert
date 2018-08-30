import {
    DESCRIPTORS_FETCHING,
    DESCRIPTORS_FETCHED,
    DESCRIPTORS_FETCH_ERROR,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    error: {
        message: ''
    },
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

        case SERIALIZE:
            return initialState;

        default:
            return state;

    }
};

export default config;
