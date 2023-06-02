import {
    AUDIT_RESEND_ERROR,
    SERIALIZE,
    AUDIT_GET_REQUEST,
    AUDIT_GET_SUCCESS,
    AUDIT_GET_FAIL,
    AUDIT_NOTIFICATION_PUT_REQUEST,
    AUDIT_NOTIFICATION_PUT_SUCCESS,
    AUDIT_NOTIFICATION_PUT_FAIL,
    AUDIT_JOB_PUT_REQUEST,
    AUDIT_JOB_PUT_SUCCESS,
    AUDIT_JOB_PUT_FAIL
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    data: {
        models: [],
        currentPage: 0,
        pageSize: 10,
        mutatorData: {
            searchTerm: '',
            sortName: 'provider',
            sortOrder: 'asc'
        }
    },
    hasError: false,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {},
    inProgress: false,
    refreshNotificationSuccess: false,
    refreshJobSuccess: false
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case AUDIT_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AUDIT_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            };
        case AUDIT_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };

        case AUDIT_NOTIFICATION_PUT_REQUEST:
            return {
                ...state,
                fetching: true,
                refreshNotificationSuccess: false,
                hasError: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AUDIT_NOTIFICATION_PUT_SUCCESS:
            return {
                ...state,
                fetching: false,
                hasError: false,
                refreshNotificationSuccess: true,
                fieldErrors: action.errors || {}
            };
        case AUDIT_NOTIFICATION_PUT_FAIL:
            return {
                ...state,
                fetching: false,
                hasError: true,
                error: action.error,
                fieldErrors: action.errors || {}
            };
        case AUDIT_JOB_PUT_REQUEST:
            return {
                ...state,
                fetching: true,
                refreshJobSuccess: false,
                hasError: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AUDIT_JOB_PUT_SUCCESS:
            return {
                ...state,
                fetching: false,
                hasError: false,
                refreshJobSuccess: true,
                fieldErrors: action.errors || {}
            };
        case AUDIT_JOB_PUT_FAIL:
            return {
                ...state,
                fetching: false,
                hasError: true,
                error: action.error,
                fieldErrors: action.errors || {}
            };
        case AUDIT_RESEND_ERROR:
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
