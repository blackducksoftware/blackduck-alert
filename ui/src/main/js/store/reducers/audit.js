import { AUDIT_FETCH_ERROR, AUDIT_FETCHED, AUDIT_FETCHING, AUDIT_RESEND_COMPLETE, AUDIT_RESEND_ERROR, AUDIT_RESEND_START, SERIALIZE } from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    totalPageCount: 0,
    items: [],
    message: '',
    error: {},
    inProgress: false
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case AUDIT_RESEND_START:
            return {
                ...state,
                fetching: true,
                inProgress: true,
                message: 'Sending...'
            };

        case AUDIT_RESEND_COMPLETE:
            return {
                ...state,
                fetching: false,
                inProgress: false,
                message: 'Send successful',
                error: HTTPErrorUtils.createEmptyErrorObject()
            };

        case AUDIT_FETCHING:
            return {
                ...state,
                fetching: true,
                inProgress: true,
                message: ''
            };

        case AUDIT_FETCHED:
            return {
                ...state,
                fetching: false,
                inProgress: false,
                totalPageCount: action.totalPageCount,
                items: action.items,
                message: '',
                error: HTTPErrorUtils.createEmptyErrorObject()
            };

        case AUDIT_RESEND_ERROR:
        case AUDIT_FETCH_ERROR:
            return {
                ...state,
                fetching: false,
                inProgress: false,
                message: '',
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
