import {
    ABOUT_INFO_FETCHING,
    ABOUT_INFO_FETCHED,
    ABOUT_INFO_FETCH_ERROR,
    SERIALIZE
} from './types';

import { verifyLoginByStatus } from './session';

const ABOUT_INFO_URL = '/api/alert/about';

function fetchingAboutInfo() {
    return {
        type: ABOUT_INFO_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function aboutInfoFetched(aboutInfo) {
    return {
        type: ABOUT_INFO_FETCHED,
        ...aboutInfo
    };
}

function aboutInfoError(message, errors) {
    return {
        type: ABOUT_INFO_FETCH_ERROR
    };
}

export function getAboutInfo() {
    return (dispatch) => {
        dispatch(fetchingAboutInfo());
        fetch(ABOUT_INFO_URL)
        .then((response) => {
            if(response.ok) {
                response.json().then((body) => {
                    dispatch(aboutInfoFetched(body));
                })
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        })
        .catch(console.error);
    };
}
