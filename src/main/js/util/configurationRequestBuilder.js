const CONFIG_API_URL = '/alert/api/configuration'

export function createReadAllRequest(csrfToken, context, descriptorName) {
    const queryParams = Object.assign({}, { context, descriptorName });
    const parameters = [];
    Object.keys(queryParams).forEach((key) => {
        const value = queryParams[key];
        if (value) {
            const parameterString = `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
            parameters.push(parameterString);
        }
    });
    const queryString = parameters.join('&');
    const url = `${CONFIG_API_URL}?${queryString}`;
    return fetch(url, {
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createReadAllGlobalContextRequest(csrfToken, descriptorName) {
    return createReadAllRequest(csrfToken, 'GLOBAL', descriptorName);
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

export function createNewConfigurationRequest(csrfToken, fieldModel) {
    return fetch(CONFIG_API_URL, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createUpdateRequest(csrfToken, configurationId, fieldModel) {
    const url = `${CONFIG_API_URL}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(fieldModel),
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

export function createValidateRequest(csrfToken, fieldModel) {
    const url = `${CONFIG_API_URL}/validate`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createTestRequest(csrfToken, fieldModel, destination) {
    let url = `${CONFIG_API_URL}/test`
    if (destination) {
        url += `?destination=${encodeURIComponent(destination)}`;
    }
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}
