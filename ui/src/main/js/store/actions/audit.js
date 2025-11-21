import {
    AUDIT_RESEND_ERROR,
    AUDIT_GET_REQUEST,
    AUDIT_GET_SUCCESS,
    AUDIT_GET_FAIL,
    AUDIT_NOTIFICATION_PUT_REQUEST,
    AUDIT_NOTIFICATION_PUT_SUCCESS,
    AUDIT_NOTIFICATION_PUT_FAIL,
    AUDIT_JOB_PUT_REQUEST,
    AUDIT_JOB_PUT_SUCCESS,
    AUDIT_JOB_PUT_FAIL
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import HeaderUtilities from 'common/util/HeaderUtilities';
import { AUDIT_URLS } from 'page/audit/AuditModel';

function fetchingAuditRequest() {
    return {
        type: AUDIT_GET_REQUEST
    };
}

function fetchingAuditSuccess(audit) {
    return {
        type: AUDIT_GET_SUCCESS,
        data: audit
    };
}

function fetchingAuditFail(error) {
    return {
        type: AUDIT_GET_FAIL,
        error
    };
}

function sendNotificationRequest() {
    return {
        type: AUDIT_NOTIFICATION_PUT_REQUEST
    };
}

function sendNotificationSuccess() {
    return {
        type: AUDIT_NOTIFICATION_PUT_SUCCESS
    };
}

function sendNotificationFail(error) {
    return {
        type: AUDIT_NOTIFICATION_PUT_FAIL,
        error
    };
}
function sendJobRequest() {
    return {
        type: AUDIT_JOB_PUT_REQUEST
    };
}

function sendJobSuccess() {
    return {
        type: AUDIT_JOB_PUT_SUCCESS
    };
}

function sendJobFail(error) {
    return {
        type: AUDIT_JOB_PUT_FAIL,
        error
    };
}

function auditResendError(message) {
    return {
        type: AUDIT_RESEND_ERROR,
        message
    };
}

function createPagedQueryURL(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications) {
    const encodedSearchTerm = encodeURIComponent(searchTerm);
    return `${AUDIT_URLS.audit}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${encodedSearchTerm}&sortField=${sortField}&sortOrder=${sortOrder}&onlyShowSentNotifications=${onlyShowSentNotifications}`;
}

export function fetchAuditData(requestParams) {
    return (dispatch, getState) => {
        dispatch(fetchingAuditRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAuditFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));

        const { pageNumber, pageSize, mutatorData } = requestParams;
        const fetchUrl = createPagedQueryURL(pageNumber, pageSize, mutatorData.searchTerm, mutatorData.sortName, mutatorData.sortOrder, true);

        const headersUtil = new HeaderUtilities();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(fetchUrl, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders(),
            redirect: 'manual'
        }).then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchingAuditSuccess(responseData));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchingAuditFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchingAuditFail(error));
        });
    };
}

export function sendNotification(notificationId, requestParams) {
    return (dispatch, getState) => {
        dispatch(sendNotificationRequest());
        const resendUrl = `${AUDIT_URLS.resend}${notificationId}/`;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => auditResendError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(resendUrl, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                if (response.ok) {
                    dispatch(sendNotificationSuccess());
                    dispatch(fetchAuditData(requestParams));
                } else {
                    dispatch(sendNotificationFail(response));
                }
            }).catch(console.error);
    };
}

export function sendJob(notificationId, jobId) {
    return (dispatch, getState) => {
        dispatch(sendJobRequest());
        const resendUrl = `${AUDIT_URLS.resend}${notificationId}/job/${jobId}`;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => auditResendError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(resendUrl, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                if (response.ok) {
                    dispatch(sendJobSuccess());
                } else {
                    dispatch(sendJobFail(response));
                }
            }).catch(console.error);
    };
}
