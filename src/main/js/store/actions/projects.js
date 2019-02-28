import { PROJECTS_FETCH_ERROR, PROJECTS_FETCHED, PROJECTS_FETCHING } from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

const PROVIDER_PROJECTS_URL_PREFIX = '/alert/api/provider/';
const PROVIDER_PROJECTS_URL_SUFFIX = '/projects';

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function fetchingProjects() {
    return {
        type: PROJECTS_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function projectsFetched(projects) {
    return {
        type: PROJECTS_FETCHED,
        projects
    };
}

/**
 * Triggers Config Error
 * @returns {{type}}
 */
function projectsError(message) {
    return {
        type: PROJECTS_FETCH_ERROR,
        message
    };
}

export function getProjects(providerName) {
    return (dispatch) => {
        dispatch(fetchingProjects());
        const requestUrl = `${PROVIDER_PROJECTS_URL_PREFIX}${providerName}${PROVIDER_PROJECTS_URL_SUFFIX}`;
        fetch(requestUrl, {
            credentials: 'same-origin'
        })
            .then((response) => {
                response.json()
                    .then((json) => {
                        if (!response.ok) {
                            dispatch(projectsError(json.message));
                            dispatch(verifyLoginByStatus(response.status));
                        } else {
                            const projects = json.map(({ name, description, url }) => ({
                                name,
                                description,
                                url
                            }));
                            dispatch(projectsFetched(projects));
                        }
                    });
            })
            .catch((error) => {
                dispatch(projectsError(`Unable to connect to Server: ${error}`));
                console.error(error);
            });
    };
}
