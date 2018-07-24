import {AUDIT_FETCH_ERROR, AUDIT_FETCHED, AUDIT_FETCHING} from './types';

import {verifyLoginByStatus} from './session';

const FETCH_URL = '/alert/api/audit';

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function fetchingAuditData(items) {
    return {
        type: AUDIT_FETCHING
    };
}

/**
 * Triggers Audit items fetched
 * @returns {{type}}
 */
function auditDataFetched(totalDataCount, items) {
    return {
        type: AUDIT_FETCHED,
        totalDataCount,
        items
    };
}

function auditDataFetchError(message) {
    return {
        type: AUDIT_FETCH_ERROR,
        error: {
            message
        }
    }
}

function createPagedQueryURL(pageNumber, pageSize, searchTerm) {
    // server side is 0 based but UI paging component starts with 1
    const pageNumberParameter = pageNumber - 1;
    return `${FETCH_URL}?pageNumber=${pageNumberParameter}&pageSize=${pageSize}&searchTerm=${searchTerm}`;
}

/**
 * Fetching Audit Data
 * @returns {function(*)}
 */
export function getAuditData(pageNumber, pageSize, searchTerm) {
    return (dispatch, getState) => {
        dispatch(fetchingAuditData());
        const {csrfToken} = getState().session;
        const fetchUrl = createPagedQueryURL(pageNumber, pageSize, searchTerm);
        fetch(fetchUrl, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        }).then((response) => {
            if (response.ok) {
                response.json().then((body) => {
                    dispatch(auditDataFetched(body.totalPages, body.content));
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(dispatch(auditDataFetchError(console.error)));
    };
}
