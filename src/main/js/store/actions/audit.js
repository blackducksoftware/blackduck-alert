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

/**
 * Fetching Audit Data
 * @returns {function(*)}
 */
export function getAuditData(pageNumber,pageSize) {
    return (dispatch, getState) => {
        dispatch(fetchingAuditData());
        const csrfToken = getState().session.csrfToken;
        const fetchUrl = createPagedQueryURL(pageNumber,pageSize);
        fetch(fetchUrl, {
            credentials: 'include',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then(response => response.json())
        .then((body) => {
            dispatch(auditDataFetched(body.totalPages, body.content));
            //getPagedAuditData(dispatch, getState, body.currentPage+1, body.pageSize, body.totalPages, contentList);
        })
        .catch((error) => {
            console.error(error);
        });
    };
}

function createPagedQueryURL(pageNumber, pageSize) {
    return FETCH_URL +"?pageNumber="+pageNumber+"&pageSize="+pageSize;
}

function getPagedAuditData(dispatch, getState, currentPage, pageSize, totalPages, currentContentList) {
    const csrfToken = getState().session.csrfToken;
    if(currentPage <= totalPages) {
        const fetchUrl = createPagedQueryURL(currentPage, pageSize);
        fetch(fetchUrl, {
            credentials: 'include',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then(response => response.json())
        .then((body) => {
            const contentList = currentContentList.concat(body.content);
            if(body.currentPage < body.totalPages) {
                dispatch(fetchingAuditData(contentList));
                getPagedAuditData(dispatch, getState, body.currentPage+1, body.pageSize, body.totalPages, contentList);
            } else {
                console.log("Finished Fetching Audit Data");
                dispatch(auditDataFetched(contentList));
            }
        })
        .catch((error) => {
            console.error(error);
        });
    }
}
