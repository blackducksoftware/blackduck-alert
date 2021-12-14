import { SERIALIZE, TASKS_FETCH_ERROR_ALL, TASKS_FETCHED_ALL, TASKS_FETCHING_ALL } from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    data: []
};

const tasks = (state = initialState, action) => {
    switch (action.type) {
        case TASKS_FETCH_ERROR_ALL:
            return {
                ...state,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false
            };
        case TASKS_FETCHED_ALL:
            return {
                ...state,
                data: action.tasks,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fetching: false
            };
        case TASKS_FETCHING_ALL:
            return {
                ...state,
                data: [],
                fetching: true
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default tasks;
