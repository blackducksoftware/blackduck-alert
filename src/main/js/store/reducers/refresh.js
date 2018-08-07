import {REFRESH_DISABLE, REFRESH_ENABLE, SERIALIZE} from '../actions/types';

const initialState = {
    autoRefresh: true
};

const refresh = (state = initialState, action) => {
    switch (action.type) {
        case REFRESH_DISABLE:
            return Object.assign({}, state, {
                autoRefresh: false
            });

        case REFRESH_ENABLE:
            return Object.assign({}, state, {
                autoRefresh: true
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default refresh;
