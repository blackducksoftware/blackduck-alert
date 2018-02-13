import {
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_SHOW_ADVANCED,
    EMAIL_CONFIG_HIDE_ADVANCED,
    SERIALIZE
} from '../actions/types';

const initialState = {
    fetching: false,
    showAdvanced: false
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case EMAIL_CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true
            });

        case EMAIL_CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false
            });

        case EMAIL_CONFIG_SHOW_ADVANCED:
            return Object.assign({}, state, {
                showAdvanced: true
            });

        case EMAIL_CONFIG_HIDE_ADVANCED:
            return Object.assign({}, state, {
                showAdvanced: false
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
