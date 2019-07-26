import { SERIALIZE, USER_EMAIL_FETCH_ERROR, USER_EMAIL_FETCHED, USER_EMAIL_FETCHING } from 'store/actions/types';

const initialState = {
    fetching: false,
    error: {
        message: ''
    },
    items: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case USER_EMAIL_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                error: {
                    message: ''
                }
            });

        case USER_EMAIL_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                error: {
                    message: ''
                },
                items: action.userEmails
            });

        case USER_EMAIL_FETCH_ERROR:
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
