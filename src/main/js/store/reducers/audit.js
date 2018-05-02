import {
    AUDIT_FETCHING,
    AUDIT_FETCHED,
    AUDIT_FETCH_ERROR,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    items: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case AUDIT_FETCHING:
            return Object.assign({}, state, {
                fetching: true
            });

        case AUDIT_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                totalDataCount: action.totalDataCount,
                items: action.items
            });
        case AUDIT_FETCH_ERROR:
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
