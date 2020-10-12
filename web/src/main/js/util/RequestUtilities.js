export function createReadRequest(url, csrfToken) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'GET',
        headers
    });
}

export function createUpdateRequest(url, csrfToken, body) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(body),
        headers
    });
}

export function createPostRequest(url, csrfToken, body) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(body),
        headers
    });
}

export function createDeleteRequest(url, csrfToken) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers
    });
}
