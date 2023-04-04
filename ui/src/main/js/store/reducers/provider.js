import {
    SERIALIZE,
    GET_PROVIDER_REQUEST,
    GET_PROVIDER_FAIL,
    GET_PROVIDER_SUCCESS,
    POST_PROVIDER_REQUEST,
    POST_PROVIDER_FAIL,
    POST_PROVIDER_SUCCESS,
    VALIDATE_PROVIDER_REQUEST,
    VALIDATE_PROVIDER_FAIL,
    VALIDATE_PROVIDER_SUCCESS,
    CLEAR_PROVIDER_FIELD_ERRORS
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    data: [],
    fetching: false,
    saveStatus: '',
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {}
};

const provider = (state = initialState, action) => {
    switch (action.type) {
        case GET_PROVIDER_REQUEST:
            return {
                ...state,
                fetching: true,
                data: [],
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case GET_PROVIDER_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case GET_PROVIDER_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            }

        case POST_PROVIDER_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'SAVING',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case POST_PROVIDER_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            }
        case POST_PROVIDER_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                saveStatus: 'SAVED',
                fieldErrors: action.errors || {}
            }

        case VALIDATE_PROVIDER_REQUEST:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATING'
            }
        case VALIDATE_PROVIDER_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            }
        case VALIDATE_PROVIDER_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATED'
            }
        case CLEAR_PROVIDER_FIELD_ERRORS:
            return {
                ...state,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: ''
            }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default provider;
