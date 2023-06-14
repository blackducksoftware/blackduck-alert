import { SERIALIZE, TASKS_GET_FAIL, TASKS_GET_REQUEST, TASKS_GET_SUCCESS } from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    data: []
};

const tasks = (state = initialState, action) => {
    switch (action.type) {
        case TASKS_GET_FAIL:
            return {
                ...state,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false
            };
        case TASKS_GET_SUCCESS:
            return {
                ...state,
                data: action.tasks,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fetching: false
            };
        case TASKS_GET_REQUEST:
            return {
                ...state,
                fetching: true
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default tasks;
