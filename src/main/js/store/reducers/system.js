import {SERIALIZE, SYSTEM_LATEST_MESSAGES_FETCH_ERROR, SYSTEM_LATEST_MESSAGES_FETCHED, SYSTEM_LATEST_MESSAGES_FETCHING} from "../actions/types";

const initialState = {
    fetching: false,
    latestMessages: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case SYSTEM_LATEST_MESSAGES_FETCHING:
            return Object.assign({}, state, {
                fetching: true
            });
        case SYSTEM_LATEST_MESSAGES_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                latestMessages: action.latestMessages
            });
        case SYSTEM_LATEST_MESSAGES_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false
            });
        case SERIALIZE:
            return initialState;
        default:
            return state;
    }
}

export default config;
