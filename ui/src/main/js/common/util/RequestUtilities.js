import HeaderUtilities from 'common/util/HeaderUtilities';

export function createReadRequest(url, csrfToken) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addAccept();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'GET',
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
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

export function createRequestUrl(baseUrl, context, descriptorName) {
    const queryParams = {
        context,
        descriptorName
    };
    const parameters = [];
    Object.keys(queryParams)
        .forEach((key) => {
            const value = queryParams[key];
            if (value) {
                const parameterString = `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
                parameters.push(parameterString);
            }
        });
    const queryString = parameters.join('&');
    return `${baseUrl}?${queryString}`;
}
