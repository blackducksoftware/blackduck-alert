import { AUDIT_FETCH_ERROR, AUDIT_FETCHED, AUDIT_FETCHING, AUDIT_RESEND_COMPLETE, AUDIT_RESEND_ERROR, AUDIT_RESEND_START, SERIALIZE } from 'store/actions/types';

const initialState = {
    fetching: false,
    totalPageCount: 0,
    items: [],
    message: '',
    inProgress: false
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case AUDIT_RESEND_START:
            return Object.assign({}, state, {
                fetching: true,
                inProgress: true,
                message: 'Sending...'
            });

        case AUDIT_RESEND_COMPLETE:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                items: action.items,
                message: 'Send successful'
            });

        case AUDIT_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                inProgress: true,
                message: 'Loading...'
            });

        case AUDIT_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                totalPageCount: action.totalPageCount,
                items: action.items,
                message: ''
            });

        case AUDIT_RESEND_ERROR:
        case AUDIT_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                message: action.message
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
