import { SYSTEM_LATEST_MESSAGES_FETCHED, SYSTEM_LATEST_MESSAGES_FETCHING } from 'store/actions/types';
import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const LATEST_MESSAGES_URL = '/alert/api/system/messages/latest';

function fetchingLatestSystemMessages() {
    return {
        type: SYSTEM_LATEST_MESSAGES_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function latestSystemMessagesFetched(latestMessages) {
    return {
        type: SYSTEM_LATEST_MESSAGES_FETCHED,
        latestMessages
    };
}

export function getLatestMessages() {
    const errorHandlers = [];
    errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    return (dispatch) => {
        dispatch(fetchingLatestSystemMessages());
        fetch(LATEST_MESSAGES_URL)
        .then((response) => {
            if (response.ok) {
                response.json()
                .then((body) => {
                    dispatch(latestSystemMessagesFetched(body));
                });
            } else {
                const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                dispatch(handler.call(response.status));
            }
        })
            .catch(console.error);
    };
}
