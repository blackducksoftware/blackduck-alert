import { ABOUT_INFO_FETCH_ERROR, ABOUT_INFO_FETCHED, ABOUT_INFO_FETCHING } from 'store/actions/types';

import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import HeaderUtilities from 'common/util/HeaderUtilities';

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
    return (dispatch, getState) => {
        dispatch(fetchingAboutInfo());
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(unauthorized));

        const { csrfToken } = getState().session;

        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);

        fetch(ABOUT_INFO_URL, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders(),
            redirect: 'manual'
        }).then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(aboutInfoFetched(responseData));
                        } else {
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch(console.error);
    };
}
