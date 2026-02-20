import {
    SYSTEM_DIAGNOSTICS_GET_FAIL,
    SYSTEM_DIAGNOSTICS_GET_REQUEST,
    SYSTEM_DIAGNOSTICS_GET_SUCCESS,
    SERIALIZE
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
        case SYSTEM_DIAGNOSTICS_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                fieldErrors: action.errors || {},
            };
        case SYSTEM_DIAGNOSTICS_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
            };
        case SYSTEM_DIAGNOSTICS_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {},
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default provider;
