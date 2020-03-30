import { TASKS_FETCH_ERROR_ALL, TASKS_FETCHED_ALL, TASKS_FETCHING_ALL } from 'store/actions/types';
import * as RequestUtilities from "../../util/RequestUtilities";
import { verifyLoginByStatus } from "./session";

const TASKS_API_URL = `/alert/api/tasks`;

function fetchingAllTasks() {
    return {
        type: TASKS_FETCHING_ALL
    };
}

function fetchedAllTasks(tasks) {
    return {
        type: TASKS_FETCHED_ALL,
        tasks
    };
}

function fetchingAllTasksError(message) {
    return {
        type: TASKS_FETCH_ERROR_ALL,
        tasksFetchError: message
    };
}

export function fetchTasks() {
    return (dispatch, getState) => {
        dispatch(fetchingAllTasks());
        const { csrfToken } = getState().session;
        const request = RequestUtilities.createReadRequest(TASKS_API_URL, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json()
                .then((jsonArray) => {
                    dispatch(fetchedAllTasks(jsonArray));
                });
            } else {
                switch (response.status) {
                    case 401:
                        dispatch(verifyLoginByStatus(response.status));
                        break;
                    case 403:
                        dispatch(fetchingAllTasksError('You are not permitted to view this information.'));
                        break;
                    default:
                        response.json()
                        .then((json) => {
                            let message = '';
                            if (json && json.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = json.message.toString();
                            }
                            dispatch(fetchingAllTasksError(message));
                        });
                }
            }
        })
        .catch((error) => {
            console.log(error);
            dispatch(fetchingAllTasksError(error));
        });
    };
}
