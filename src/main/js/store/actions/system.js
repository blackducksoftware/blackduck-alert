import {SYSTEM_LATEST_MESSAGES_FETCH_ERROR, SYSTEM_LATEST_MESSAGES_FETCHED, SYSTEM_LATEST_MESSAGES_FETCHING} from './types';
import {verifyLoginByStatus} from "./session";

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

function latestSystemMessagesError() {
    return {
        type: SYSTEM_LATEST_MESSAGES_FETCH_ERROR
    };
}

export function getLatestMessages() {
    return (dispatch) => {
        dispatch(fetchingLatestSystemMessages());
        fetch(LATEST_MESSAGES_URL)
            .then((response) => {
                if (response.ok) {
                    response.json().then((body) => {
                        dispatch(latestSystemMessagesFetched(body));
                    })
                } else {
                    dispatch(verifyLoginByStatus(response.status));
                }
            })
            .catch(console.error);
    };
}
