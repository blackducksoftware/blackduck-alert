export function createReadRequest(url, csrfToken) {
    return fetch(url, {
        credentials: 'same-origin',
        method: 'GET',
        headers: {
            accept: 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createUpdateRequest(url, csrfToken, body) {
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(body),
        headers: {
            accept: 'application/json',
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createPostRequest(url, csrfToken, body) {
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(body),
        headers: {
            accept: 'application/json',
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createDeleteRequest(url, csrfToken) {
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers: {
            accept: 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}
