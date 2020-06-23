import { ABOUT_INFO_FETCH_ERROR, ABOUT_INFO_FETCHED, ABOUT_INFO_FETCHING } from 'store/actions/types';

import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from '../../util/httpErrorUtilities';

const ABOUT_INFO_URL = '/alert/api/about';

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
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(unauthorized));
        fetch(ABOUT_INFO_URL)
        .then((response) => {
            if (response.ok) {
                response.json()
                .then((body) => {
                    dispatch(aboutInfoFetched(body));
                });
            } else {
                const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                dispatch(handler.call(response.status));
            }
        })
        .catch(console.error);
    };
}
