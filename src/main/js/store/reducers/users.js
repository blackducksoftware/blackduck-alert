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

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    userFetchError: '',
    userSaveError: null,
    userDeleteError: null,
    fieldErrors: {},
    saveStatus: ''
};

const users = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_USER_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                userDeleteError: action.userDeleteError,
                fieldErrors: action.errors || {},
                saveStatus: ''
            });
        case USER_MANAGEMENT_USER_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true,
                fieldErrors: {},
                saveStatus: ''
            });
        case USER_MANAGEMENT_USER_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                saveStatus: ''
            });
        case USER_MANAGEMENT_USER_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                userFetchError: action.userFetchError,
                fetching: false,
                saveStatus: ''
            });
        case USER_MANAGEMENT_USER_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false,
                saveStatus: ''
            });
        case USER_MANAGEMENT_USER_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: ''
            });
        case USER_MANAGEMENT_USER_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                userSaveError: action.userSaveError,
                fieldErrors: action.errors || {},
                saveStatus: 'ERROR'
            });
        case USER_MANAGEMENT_USER_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                fieldErrors: {},
                saveStatus: 'SAVED'
            });
        case USER_MANAGEMENT_USER_SAVING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            });
        case USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS: {
            return Object.assign({}, state, {
                userDeleteError: null,
                fieldErrors: {},
                saveStatus: ''
            });
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default users;
