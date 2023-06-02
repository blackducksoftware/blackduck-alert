import {
    SERIALIZE,
    DISTRIBUTION_GET_REQUEST,
    DISTRIBUTION_GET_SUCCESS,
    DISTRIBUTION_GET_FAIL,
    DISTRIBUTION_DELETE_REQUEST,
    DISTRIBUTION_DELETE_SUCCESS,
    DISTRIBUTION_DELETE_FAIL
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
            sortName: 'jobName',
            sortOrder: 'asc'
        }
    },
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {},
    deleteStatus: ''
};

const distribution = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case DISTRIBUTION_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            };
        case DISTRIBUTION_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case DISTRIBUTION_DELETE_REQUEST:
            return {
                ...state,
                fetching: true,
                deleteStatus: 'DELETING'
            };
        case DISTRIBUTION_DELETE_SUCCESS:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'DELETED',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };
        case DISTRIBUTION_DELETE_FAIL:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default distribution;
