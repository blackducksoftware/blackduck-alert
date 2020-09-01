import { REFRESH_DISABLE, REFRESH_ENABLE, SERIALIZE } from 'store/actions/types';

const initialState = {
    autoRefresh: true
};

const refresh = (state = initialState, action) => {
    switch (action.type) {
        case REFRESH_DISABLE:
            return { ...state, autoRefresh: false };

        case REFRESH_ENABLE:
            return { ...state, autoRefresh: true };

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default refresh;
