import { TASKS_GET_FAIL, TASKS_GET_REQUEST, TASKS_GET_SUCCESS } from 'store/actions/types';
import * as RequestUtilities from 'common/util/RequestUtilities';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

const TASKS_API_URL = '/alert/api/task';

function fetchingAllTasks() {
    return {
        type: TASKS_GET_REQUEST
    };
}

function fetchedAllTasks(tasks) {
    return {
        type: TASKS_GET_SUCCESS,
        tasks
    };
}

function fetchingAllTasksError(message) {
    return {
        type: TASKS_GET_FAIL,
        message
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
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchedAllTasks(responseData.tasks));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchingAllTasksError(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        })
            .catch((error) => {
                console.log(error);
                dispatch(fetchingAllTasksError(error));
            });
    };
}
