import {
    AUDIT_FETCHING,
    AUDIT_FETCHED
} from './types';

const FETCH_URL = '/api/audit';

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function fetchingAuditData() {
    return {
        type: AUDIT_FETCHING
    };
}

/**
 * Triggers Audit items fetched
 * @returns {{type}}
 */
function auditDataFetched(items) {
    return {
        type: AUDIT_FETCHED,
        items
    };
}

/**
 * Fetching Audit Data
 * @returns {function(*)}
 */
export function getAuditData() {
    return (dispatch, getState) => {
        dispatch(fetchingAuditData());
        const csrfToken = getState().session.csrfToken;
        fetch(FETCH_URL, {
            credentials: 'include',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
            .then(response => response.json())
            .then((body) => { dispatch(auditDataFetched(body)); })
            .catch((error) => {
                console.error(error);
            });
    };
}
