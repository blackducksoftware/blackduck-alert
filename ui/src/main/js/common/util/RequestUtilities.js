import HeaderUtilities from 'common/util/HeaderUtilities';

export function createReadRequest(url, csrfToken) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addAccept();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'GET',
        headers: headersUtil.getHeaders()
    });
}

export function createUpdateRequest(url, csrfToken, body) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(body),
        headers: headersUtil.getHeaders()
    });
}

export function createPostRequest(url, csrfToken, body) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(body),
        headers: headersUtil.getHeaders()
    });
}

export function createDeleteRequest(url, csrfToken) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addAccept();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers: headersUtil.getHeaders()
    });
}
