import {
    SERIALIZE,
    GET_PROVIDER_REQUEST,
    GET_PROVIDER_FAIL,
    GET_PROVIDER_SUCCESS,

    // USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS,
    // USER_MANAGEMENT_USER_BULK_DELETE_FETCH,
    // USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS,
    // USER_MANAGEMENT_USER_BULK_DELETE_FAIL,
    // USER_MANAGEMENT_USER_DELETE_ERROR,
    // USER_MANAGEMENT_USER_DELETED,
    // USER_MANAGEMENT_USER_DELETING,
    // USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
    // USER_MANAGEMENT_USER_FETCHED_ALL,
    // USER_MANAGEMENT_USER_FETCHING_ALL,
    // USER_MANAGEMENT_USER_SAVE_ERROR,
    // USER_MANAGEMENT_USER_SAVED,
    // USER_MANAGEMENT_USER_SAVING,
    // USER_MANAGEMENT_USER_VALIDATE_ERROR,
    // USER_MANAGEMENT_USER_VALIDATED,
    // USER_MANAGEMENT_USER_VALIDATING
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    data: [],
    fetching: false,
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
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default provider;
