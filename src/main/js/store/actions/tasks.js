import { TASKS_FETCH_ERROR_ALL, TASKS_FETCHED_ALL, TASKS_FETCHING_ALL } from 'store/actions/types';
import * as RequestUtilities from 'util/RequestUtilities';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

const TASKS_API_URL = `/alert/api/task`;

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
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAllTasksError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const request = RequestUtilities.createReadRequest(TASKS_API_URL, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json()
                .then((jsonArray) => {
                    dispatch(fetchedAllTasks(jsonArray));
                });
            } else {
                errorHandlers.push(HttpErrorUtils.createDefaultHandler(() => {
                    response.json()
                    .then((json) => {
                        let message = '';
                        if (json && json.message) {
                            // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                            message = json.message.toString();
                        }
                        dispatch(fetchingAllTasksError(message));
                    });
                }));
                const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                dispatch(handler.call(response.status));
            }
        })
        .catch((error) => {
            console.log(error);
            dispatch(fetchingAllTasksError(error));
        });
    };
}
