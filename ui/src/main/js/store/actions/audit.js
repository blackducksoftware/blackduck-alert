import { AUDIT_FETCH_ERROR, AUDIT_FETCHED, AUDIT_FETCHING, AUDIT_RESEND_COMPLETE, AUDIT_RESEND_ERROR, AUDIT_RESEND_START } from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import HeaderUtilities from 'common/util/HeaderUtilities';

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
    const encodedSearchTerm = encodeURIComponent(searchTerm);
    return `${FETCH_URL}?pageNumber=${pageNumberParameter}&pageSize=${pageSize}&searchTerm=${encodedSearchTerm}&sortField=${sortField}&sortOrder=${sortOrder}&onlyShowSentNotifications=${onlyShowSentNotifications}`;
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
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => auditDataFetchError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(fetchUrl, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(auditDataFetched(responseData.totalPages, responseData.content));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => auditDataFetchError(responseData.message)));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch((error) => {
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
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => auditResendError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(resendUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(auditResentSuccessfully());
                            dispatch(getAuditData(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => auditResendError(responseData.message)));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            }).catch(console.error);
    };
}
