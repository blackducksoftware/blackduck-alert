import {
    SERIALIZE,
    USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_ROLE_DELETE_ERROR,
    USER_MANAGEMENT_ROLE_DELETE_LIST_ERROR,
    USER_MANAGEMENT_ROLE_DELETED,
    USER_MANAGEMENT_ROLE_DELETED_LIST,
    USER_MANAGEMENT_ROLE_DELETING,
    USER_MANAGEMENT_ROLE_DELETING_LIST,
    USER_MANAGEMENT_ROLE_GET_FAIL,
    USER_MANAGEMENT_ROLE_GET_REQUEST,
    USER_MANAGEMENT_ROLE_GET_SUCCESS,
    USER_MANAGEMENT_ROLE_SAVE_ERROR,
    USER_MANAGEMENT_ROLE_SAVED,
    USER_MANAGEMENT_ROLE_SAVING,
    USER_MANAGEMENT_ROLE_VALIDATED,
    USER_MANAGEMENT_ROLE_VALIDATING,
    USER_MANAGEMENT_ROLE_VALIDATION_ERROR
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    data: [],
    roleError: null,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    saveStatus: '',
    deleteStatus: ''
};

const roles = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_ROLE_DELETE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                roleError: action.roleError,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: '',
                deleteStatus: 'FAIL'
            };
        case USER_MANAGEMENT_ROLE_DELETED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: '',
                deleteStatus: 'SUCCESS'
            };
        case USER_MANAGEMENT_ROLE_DELETING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: '',
                deleteStatus: 'PROCESSING'
            };
        case USER_MANAGEMENT_ROLE_DELETE_LIST_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                roleError: action.roleError,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: '',
                deleteStatus: 'FAIL'
            };
        case USER_MANAGEMENT_ROLE_DELETED_LIST:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: '',
                deleteStatus: 'SUCCESS'
            };
        case USER_MANAGEMENT_ROLE_DELETING_LIST:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: '',
                deleteStatus: 'PROCESSING'
            };
        case USER_MANAGEMENT_ROLE_GET_FAIL:
            return {
                ...state,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false
            };
        case USER_MANAGEMENT_ROLE_GET_SUCCESS:
            return {
                ...state,
                data: action.data,
                fetching: false
            };
        case USER_MANAGEMENT_ROLE_GET_REQUEST:
            return {
                ...state,
                fetching: true
            };
        case USER_MANAGEMENT_ROLE_SAVE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                roleError: action.roleError,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            };
        case USER_MANAGEMENT_ROLE_VALIDATED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'VALIDATED'
            };
        case USER_MANAGEMENT_ROLE_VALIDATING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'VALIDATING'
            };
        case USER_MANAGEMENT_ROLE_VALIDATION_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                roleError: action.roleError,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            };
        case USER_MANAGEMENT_ROLE_SAVED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'SAVED'
            };
        case USER_MANAGEMENT_ROLE_SAVING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            };
        case USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS: {
            return {
                ...state,
                roleError: null,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            };
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default roles;
