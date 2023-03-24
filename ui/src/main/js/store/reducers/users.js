import {
    SERIALIZE,
    USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_USER_BULK_DELETE_FETCH,
    USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS,
    USER_MANAGEMENT_USER_BULK_DELETE_FAIL,
    USER_MANAGEMENT_USER_DELETE_ERROR,
    USER_MANAGEMENT_USER_DELETED,
    USER_MANAGEMENT_USER_DELETING,
    USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
    USER_MANAGEMENT_USER_FETCHED_ALL,
    USER_MANAGEMENT_USER_FETCHING_ALL,
    USER_MANAGEMENT_USER_SAVE_ERROR,
    USER_MANAGEMENT_USER_SAVED,
    USER_MANAGEMENT_USER_SAVING,
    USER_MANAGEMENT_USER_VALIDATE_ERROR,
    USER_MANAGEMENT_USER_VALIDATED,
    USER_MANAGEMENT_USER_VALIDATING
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    userFetchError: '',
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {},
    saveStatus: '',
    deleteStatus: ''
};

const users = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_USER_BULK_DELETE_FETCH:
            return {
                ...state,
                fetching: true,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                deleteStatus: 'DELETING'
            };
        case USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS:
            return {
                ...state,
                fetching: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                deleteStatus: 'SUCCESS'
            };
        case USER_MANAGEMENT_USER_BULK_DELETE_FAIL:
            return {
                ...state,
                fetching: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                deleteStatus: 'ERROR'
            };
        case USER_MANAGEMENT_USER_DELETE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                saveStatus: '',
                deleteStatus: 'ERROR'
            };
        case USER_MANAGEMENT_USER_DELETED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: '',
                deleteStatus: 'DELETED'
            };
        case USER_MANAGEMENT_USER_DELETING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: '',
                deleteStatus: 'DELETING'
            };
        case USER_MANAGEMENT_USER_FETCH_ERROR_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                userFetchError: action.userFetchError,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false,
                saveStatus: '',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_FETCHED_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false,
                saveStatus: '',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_FETCHING_ALL:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: '',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_SAVE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                saveStatus: 'ERROR',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_SAVED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: 'SAVED',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_SAVING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS: {
            return {
                ...state,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: '',
                deleteStatus: ''
            };
        }
        case USER_MANAGEMENT_USER_VALIDATING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'VALIDATING',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_VALIDATED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'VALIDATED',
                deleteStatus: ''
            };
        case USER_MANAGEMENT_USER_VALIDATE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR',
                deleteStatus: ''
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default users;
