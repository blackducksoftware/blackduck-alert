import {
    AUDIT_FETCHING,
    AUDIT_FETCHED
} from './types';

const FETCH_URL = '/api/audit';

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

function createPagedQueryURL(pageNumber, pageSize) {
    // server side is 0 based but UI paging component starts with 1
    const pageNumberParameter = pageNumber - 1;
    return `${FETCH_URL}?pageNumber=${pageNumberParameter}&pageSize=${pageSize}`;
}

/**
 * Fetching Audit Data
 * @returns {function(*)}
 */
export function getAuditData(pageNumber, pageSize) {
    return (dispatch, getState) => {
        dispatch(fetchingAuditData());
        const { csrfToken } = getState().session;
        const fetchUrl = createPagedQueryURL(pageNumber, pageSize);
        fetch(fetchUrl, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        }).then(response => response.json()).then((body) => {
            dispatch(auditDataFetched(body.totalPages, body.content));
            // getPagedAuditData(dispatch, getState, body.currentPage+1, body.pageSize, body.totalPages, contentList);
        }).catch(console.error);
    };
}

function getPagedAuditData(dispatch, getState, currentPage, pageSize, totalPages, currentContentList) {
    const { csrfToken } = getState().session;
    if (currentPage <= totalPages) {
        const fetchUrl = createPagedQueryURL(currentPage, pageSize);
        fetch(fetchUrl, {
            credentials: 'include',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        }).then(response => response.json()).then((body) => {
            const contentList = currentContentList.concat(body.content);
            if (body.currentPage < body.totalPages) {
                dispatch(fetchingAuditData(contentList));
                getPagedAuditData(dispatch, getState, body.currentPage + 1, body.pageSize, body.totalPages, contentList);
            } else {
                console.log('Finished Fetching Audit Data');
                dispatch(auditDataFetched(contentList));
            }
        }).catch(console.error);
    }
}
