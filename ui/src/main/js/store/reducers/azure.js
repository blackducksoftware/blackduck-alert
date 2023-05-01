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
    AZURE_TEST_REQUEST,
    AZURE_TEST_SUCCESS,
    AZURE_TEST_FAIL,
    AZURE_OAUTH_REQUEST,
    AZURE_OAUTH_SUCCESS,
    AZURE_OAUTH_FAIL,
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
    testStatus: '',
    oAuthStatus: '',
    oAuthLink: ''
};

const azure = (state = initialState, action) => {
    switch (action.type) {
        case AZURE_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AZURE_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            };
        case AZURE_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AZURE_VALIDATE_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'VALIDATING'
            };
        case AZURE_VALIDATE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATED',
                error: HTTPErrorUtils.createEmptyErrorObject()
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
            };
        case AZURE_SAVE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'SAVED',
                fieldErrors: action.errors || {}
            };
        case AZURE_SAVE_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AZURE_CLEAR_FIELD_ERRORS:
            return {
                ...state,
                saveStatus: '',
                deleteStatus: '',
                testStatus: '',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };
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
                deleteStatus: 'DELETED',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };
        case AZURE_DELETE_FAIL:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case AZURE_TEST_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: '',
                testStatus: 'TESTING'
            };
        case AZURE_TEST_FAIL:
            return {
                ...state,
                fetching: false,
                testStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case AZURE_TEST_SUCCESS:
            return {
                ...state,
                fetching: false,
                testStatus: 'SUCCESS'
            };
        case AZURE_OAUTH_REQUEST:
            return {
                ...state,
                fetching: true,
                oAuthStatus: 'FETCHING'
            };
        case AZURE_OAUTH_SUCCESS:
            return {
                ...state,
                fetching: false,
                oAuthStatus: 'SUCCESS',
                oAuthLink: action.oAuthLink,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };
        case AZURE_OAUTH_FAIL:
            return {
                ...state,
                fetching: false,
                oAuthStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default azure;
