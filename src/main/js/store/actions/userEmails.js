import { USER_EMAIL_FETCH_ERROR, USER_EMAIL_FETCHED, USER_EMAIL_FETCHING } from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

const PROVIDER_PROJECTS_URL_PREFIX = '/alert/api';

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function fetchingUserEmails() {
    return {
        type: USER_EMAIL_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function userEmailsFetched(userEmails) {
    return {
        type: USER_EMAIL_FETCHED,
        userEmails
    };
}

/**
 * Triggers Config Error
 * @returns {{type}}
 */
function userEmailsError(message) {
    return {
        type: USER_EMAIL_FETCH_ERROR,
        message
    };
}

export function getUserEmails(endpoint, providerName) {
    return (dispatch) => {
        dispatch(fetchingUserEmails());
        const resolvedEndpoint = endpoint.replace('{provider}', providerName);
        const requestUrl = `${PROVIDER_PROJECTS_URL_PREFIX}${resolvedEndpoint}`;
        fetch(requestUrl, {
            credentials: 'same-origin'
        })
            .then((response) => {
                response.json()
                    .then((json) => {
                        if (!response.ok) {
                            dispatch(userEmailsError(json.message));
                            dispatch(verifyLoginByStatus(response.status));
                        } else {
                            const projects = json.map(({ name, description, url }) => ({
                                name,
                                description,
                                url
                            }));
                            dispatch(userEmailsFetched(projects));
                        }
                    });
            })
            .catch((error) => {
                dispatch(userEmailsError(`Unable to connect to Server: ${error}`));
                console.error(error);
            });
    };
}
