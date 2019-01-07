const CONFIG_API_URL = '/alert/api/configuration'


export function createReadAllRequest(csrfToken, context, descriptorName) {
    const queryParams = Object.assign({}, { context, descriptorName });
    const parameters = [];
    for (let key in queryParams) {
        const value = queryParams[key];
        if(value) {
            parameters.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
        }
    }
    const queryString = parameters.join('&');
    const url = `${CONFIG_API_URL}?${queryString}`;
    return fetch(url, {
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createReadRequest(csrfToken, configurationId) {
    const url = `${CONFIG_API_URL}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createNewConfigurationRequest(csrfToken, content) {
    return fetch(CONFIG_API_URL, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(content),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createUpdateRequest(csrfToken, configurationId, content) {
    const url = `${CONFIG_API_URL}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(content),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createDeleteRequest(csrfToken, configurationId) {
    const url = `${CONFIG_API_URL}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createValidateRequest(csrfToken, content) {
    const url = `${CONFIG_API_URL}/validate`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(content),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createTestRequest(csrfToken, content, destination) {
    const url = `${CONFIG_API_URL}/test?destination=${encodeURIComponent(destination)}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(content),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}
