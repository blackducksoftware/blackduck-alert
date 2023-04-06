import {
    SERIALIZE,
    AZURE_GET_REQUEST,
    AZURE_GET_SUCCESS,
    AZURE_GET_FAIL,
    AZURE_VALIDATE_REQUEST,
    AZURE_VALIDATE_SUCCESS,
    AZURE_VALIDATE_FAIL,
    AZURE_SAVE_REQUEST,
    AZURE_SAVE_SUCCESS,
    AZURE_SAVE_FAIL,
    AZURE_DELETE_REQUEST,
    AZURE_DELETE_SUCCESS,
    AZURE_DELETE_FAIL,
    AZURE_CLEAR_FIELD_ERRORS
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
            sortName: 'name',
            sortOrder: 'asc'
        }
    },
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {},
    saveStatus: '',
    deleteStatus: '',
};

const azure = (state = initialState, action) => {
    switch (action.type) {
        case AZURE_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case AZURE_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            }
        case AZURE_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case AZURE_VALIDATE_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'VALIDATING',
            };
        case AZURE_VALIDATE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATED',
                error: HTTPErrorUtils.createEmptyErrorObject(),
            };
        case AZURE_VALIDATE_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AZURE_SAVE_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'SAVING',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case AZURE_SAVE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'SAVED',
                data: action.data,
                fieldErrors: action.errors || {}
            }
        case AZURE_SAVE_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case AZURE_CLEAR_FIELD_ERRORS:
            return {
                ...state,
                saveStatus: '',
                deleteStatus: '',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            }
        case AZURE_DELETE_REQUEST:
            return {
                ...state,
                fetching: true,
                deleteStatus: 'DELETING'
            };
        case AZURE_DELETE_SUCCESS:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'SUCCESS',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
            };
        case AZURE_DELETE_FAIL:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
            };
        
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
}

export default azure;
