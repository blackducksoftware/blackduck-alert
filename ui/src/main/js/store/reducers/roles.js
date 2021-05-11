import {
    SERIALIZE,
    USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_ROLE_DELETE_ERROR,
    USER_MANAGEMENT_ROLE_DELETED,
    USER_MANAGEMENT_ROLE_DELETING,
    USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL,
    USER_MANAGEMENT_ROLE_FETCHED_ALL,
    USER_MANAGEMENT_ROLE_FETCHING_ALL,
    USER_MANAGEMENT_ROLE_SAVE_ERROR,
    USER_MANAGEMENT_ROLE_SAVED,
    USER_MANAGEMENT_ROLE_SAVING,
    USER_MANAGEMENT_ROLE_VALIDATED,
    USER_MANAGEMENT_ROLE_VALIDATING,
    USER_MANAGEMENT_ROLE_VALIDATION_ERROR
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    roleError: null,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    saveStatus: ''
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
                saveStatus: ''
            };
        case USER_MANAGEMENT_ROLE_DELETED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            };
        case USER_MANAGEMENT_ROLE_DELETING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: ''
            };
        case USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false,
                saveStatus: ''
            };
        case USER_MANAGEMENT_ROLE_FETCHED_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false,
                saveStatus: ''
            };
        case USER_MANAGEMENT_ROLE_FETCHING_ALL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: ''
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
