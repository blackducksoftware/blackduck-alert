import { PROJECTS_FETCH_ERROR, PROJECTS_FETCHED, PROJECTS_FETCHING } from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

const PROJECTS_URL = '/alert/api/blackduck/projects';

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

export function getProjects() {
    return (dispatch) => {
        dispatch(fetchingProjects());
        fetch(PROJECTS_URL, {
            credentials: 'same-origin'
        }).then((response) => {
            response.json().then((json) => {
                if (!response.ok) {
                    dispatch(projectsError(json.message));
                    dispatch(verifyLoginByStatus(response.status));
                } else {
                    const jsonArray = JSON.parse(json.message) || [];
                    const projects = jsonArray.map(({ name, description, url }) => ({ name, description, url }));
                    dispatch(projectsFetched(projects));
                }
            });
        }).catch((error) => {
            dispatch(projectsError(`Unable to connect to Server: ${error}`));
            console.error(error);
        });
    };
}
