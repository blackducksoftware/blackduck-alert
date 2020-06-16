import { SERIALIZE, TASKS_FETCH_ERROR_ALL, TASKS_FETCHING_ALL } from 'store/actions/types';
import { TASKS_FETCHED_ALL } from "../actions/types";

const initialState = {
    fetching: false,
    data: []
};

const tasks = (state = initialState, action) => {
    switch (action.type) {
        case TASKS_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                tasksFetchError: action.tasksFetchError,
                fetching: false
            });
        case TASKS_FETCHED_ALL:
            return Object.assign({}, state, {
                data: action.tasks,
                fetching: false
            });
        case TASKS_FETCHING_ALL:
            return Object.assign({}, state, {
                data: [],
                fetching: true
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default tasks;
