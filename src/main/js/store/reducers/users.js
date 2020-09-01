import {
    SERIALIZE,
    USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_USER_DELETE_ERROR,
    USER_MANAGEMENT_USER_DELETED,
    USER_MANAGEMENT_USER_DELETING,
    USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
    USER_MANAGEMENT_USER_FETCHED_ALL,
    USER_MANAGEMENT_USER_FETCHING_ALL,
    USER_MANAGEMENT_USER_SAVE_ERROR,
    USER_MANAGEMENT_USER_SAVED,
    USER_MANAGEMENT_USER_SAVING
} from 'store/actions/types';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    userFetchError: '',
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {},
    saveStatus: ''
};

const users = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_USER_DELETE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                saveStatus: ''
            };
        case USER_MANAGEMENT_USER_DELETED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: ''
            };
        case USER_MANAGEMENT_USER_DELETING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: ''
            };
        case USER_MANAGEMENT_USER_FETCH_ERROR_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                userFetchError: action.userFetchError,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false,
                saveStatus: ''
            };
        case USER_MANAGEMENT_USER_FETCHED_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false,
                saveStatus: ''
            };
        case USER_MANAGEMENT_USER_FETCHING_ALL:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: ''
            };
        case USER_MANAGEMENT_USER_SAVE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                saveStatus: 'ERROR'
            };
        case USER_MANAGEMENT_USER_SAVED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: 'SAVED'
            };
        case USER_MANAGEMENT_USER_SAVING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            };
        case USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS: {
            return {
                ...state,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: ''
            };
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default users;
