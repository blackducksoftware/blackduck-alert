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
    USER_MANAGEMENT_ROLE_SAVING
} from 'store/actions/types';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

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
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleError: action.roleError,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: ''
            });
        case USER_MANAGEMENT_ROLE_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            });
        case USER_MANAGEMENT_ROLE_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                saveStatus: ''
            });
        case USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false,
                saveStatus: ''
            });
        case USER_MANAGEMENT_ROLE_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false,
                saveStatus: ''
            });
        case USER_MANAGEMENT_ROLE_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: ''
            });
        case USER_MANAGEMENT_ROLE_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleError: action.roleError,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            });
        case USER_MANAGEMENT_ROLE_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'SAVED'
            });
        case USER_MANAGEMENT_ROLE_SAVING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            });
        case USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS: {
            return Object.assign({}, state, {
                roleError: null,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            });
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default roles;
