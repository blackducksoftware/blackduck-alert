import { AUDIT_FETCH_ERROR, AUDIT_FETCHED, AUDIT_FETCHING, AUDIT_RESEND_COMPLETE, AUDIT_RESEND_ERROR, AUDIT_RESEND_START } from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

const FETCH_URL = '/alert/api/audit';

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
function auditDataFetched(totalPageCount, items) {
    return {
        type: AUDIT_FETCHED,
        totalPageCount,
        items
    };
}

function auditDataFetchError(message) {
    console.log('Logging audit error message');
    console.log(message);
    return {
        type: AUDIT_FETCH_ERROR,
        message
    };
}

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function startingAuditResend() {
    return {
        type: AUDIT_RESEND_START
    };
}

/**
 * Triggers Audit items fetched
 * @returns {{type}}
 */
function auditResentSuccessfully() {
    return {
        type: AUDIT_RESEND_COMPLETE
    };
}

function auditResendError(message) {
    return {
        type: AUDIT_RESEND_ERROR,
        message
    };
}

function createPagedQueryURL(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) {
    // server side is 0 based but UI paging component starts with 1
    const pageNumberParameter = pageNumber - 1;
    return `${FETCH_URL}?pageNumber=${pageNumberParameter}&pageSize=${pageSize}&searchTerm=${searchTerm}&sortField=${sortField}&sortOrder=${sortOrder}&onlyShowSentNotifications=${onlyShowSentNotifications}`;
}

/**
 * Fetching Audit Data
 * @returns {function(*)}
 */
export function getAuditData(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) {
    return (dispatch, getState) => {
        dispatch(fetchingAuditData());
        const { csrfToken } = getState().session;
        const fetchUrl = createPagedQueryURL(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications);
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
        }).catch((error) => {
            dispatch(auditDataFetchError(error));
        });
    };
}

export function resendNotification(notificationId, commonConfigId, pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) {
    return (dispatch, getState) => {
        dispatch(startingAuditResend());
        let resendUrl = `/alert/api/audit/resend/${notificationId}/`;
        if (commonConfigId) {
            resendUrl += `job/${commonConfigId}/`;
        }
        const { csrfToken } = getState().session;

        fetch(resendUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        }).then((response) => {
            if (!response.ok) {
                switch (response.status) {
                    case 401:
                        dispatch(verifyLoginByStatus(response.status));
                        break;
                    default:
                        response.json().then((json) => {
                            dispatch(auditResendError(json.message));
                        });
                }
            }
            dispatch(auditResentSuccessfully());
            dispatch(getAuditData(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications));
        }).catch(console.error);
    };
}
