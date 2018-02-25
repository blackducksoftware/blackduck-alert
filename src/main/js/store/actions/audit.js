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
    return (dispatch) => {
        dispatch(fetchingAuditData());

        fetch(FETCH_URL, {
            credentials: 'include'
        })
        .then((response) => response.json())
        .then((body) => { dispatch(auditDataFetched(body)) })
        .catch(function(error) {
            console.error(error);
        });
    }
};
