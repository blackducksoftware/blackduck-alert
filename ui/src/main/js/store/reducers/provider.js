import {
    PROVIDER_DELETE_REQUEST,
    PROVIDER_DELETE_FAIL,
    PROVIDER_DELETE_SUCCESS,
    PROVIDER_GET_REQUEST,
    PROVIDER_GET_FAIL,
    PROVIDER_GET_SUCCESS,
    PROVIDER_POST_REQUEST,
    PROVIDER_POST_FAIL,
    PROVIDER_POST_SUCCESS,
    PROVIDER_VALIDATE_REQUEST,
    PROVIDER_VALIDATE_FAIL,
    PROVIDER_VALIDATE_SUCCESS,
    PROVIDER_TEST_REQUEST,
    PROVIDER_TEST_FAIL,
    PROVIDER_TEST_SUCCESS,
    PROVIDER_CLEAR_FIELD_ERRORS,
    SERIALIZE
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    data: [],
    fetching: false,
    saveStatus: '',
    deleteStatus: '',
    testStatus: '',
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {}
};

const provider = (state = initialState, action) => {
    switch (action.type) {
        case PROVIDER_DELETE_REQUEST:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                deleteStatus: 'DELETING'
            };
        case PROVIDER_DELETE_SUCCESS:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                deleteStatus: 'SUCCESS'
            };
        case PROVIDER_DELETE_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                deleteStatus: 'ERROR'
            };
        case PROVIDER_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                data: [],
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case PROVIDER_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case PROVIDER_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            };
        case PROVIDER_POST_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'SAVING',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case PROVIDER_POST_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case PROVIDER_POST_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                saveStatus: 'SAVED',
                fieldErrors: action.errors || {}
            };
        case PROVIDER_VALIDATE_REQUEST:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATING'
            };
        case PROVIDER_VALIDATE_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case PROVIDER_VALIDATE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATED'
            };
        case PROVIDER_TEST_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: '',
                testStatus: 'TESTING'
            };
        case PROVIDER_TEST_FAIL:
            return {
                ...state,
                fetching: false,
                testStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case PROVIDER_TEST_SUCCESS:
            return {
                ...state,
                fetching: false,
                testStatus: 'SUCCESS'
            };
        case PROVIDER_CLEAR_FIELD_ERRORS:
            return {
                ...state,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: '',
                deleteStatus: '',
                testStatus: ''
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default provider;
