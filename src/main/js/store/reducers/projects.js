import {
    PROJECTS_FETCHING,
    PROJECTS_FETCHED,
    PROJECTS_FETCH_ERROR,
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
        case PROJECTS_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                error: {
                    message: ''
                }
            });

        case PROJECTS_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                error: {
                    message: ''
                },
                items: action.projects
            });

        case PROJECTS_FETCH_ERROR:
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
