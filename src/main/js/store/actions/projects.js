import {
    PROJECTS_FETCHING,
    PROJECTS_FETCHED,
    PROJECTS_FETCH_ERROR
} from './types';

import { logout } from './session';

const PROJECTS_URL = '/api/hub/projects';

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
                    switch(response.status) {
                        case 401:
                        case 403:
                            return dispatch(logout());
                        default:
                            return dispatch(projectsError(json.message));
                    }
                } else {
                    const jsonArray = JSON.parse(json.message) || [];
                    const projects = jsonArray.map(({ name, url }) => ({ name, url }));
                    dispatch(projectsFetched(projects));
                }
            });
        }).catch((error) => {
            dispatch(projectsError(`Unable to connect to Server: ${error}`));
            console.error(error);
        });
    };
}
