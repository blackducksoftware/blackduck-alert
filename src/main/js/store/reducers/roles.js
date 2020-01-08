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
} from 'store/actions/types'

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    roleFetchError: '',
    roleSaveError: null,
    roleDeleteError: null,
    fieldErrors: {}
};

const roles = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_ROLE_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleDeleteError: action.roleDeleteError,
                fieldErrors: action.errors || {}
            });
        case USER_MANAGEMENT_ROLE_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true,
                fieldErrors: {}
            });
        case USER_MANAGEMENT_ROLE_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleFetchError: action.roleFetchError,
                fetching: false
            });
        case USER_MANAGEMENT_ROLE_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false
            });
        case USER_MANAGEMENT_ROLE_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                data: [],
                fetching: true
            });
        case USER_MANAGEMENT_ROLE_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleSaveError: action.roleSaveError,
                fieldErrors: action.errors || {}
            });
        case USER_MANAGEMENT_ROLE_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                fieldErrors: {}
            });
        case USER_MANAGEMENT_ROLE_SAVING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS: {
            return Object.assign({}, state, {
                roleDeleteError: null,
                fieldErrors: {}
            });
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default roles;
