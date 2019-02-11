export const CONFIG_API_URL = '/alert/api/configuration';
export const JOB_API_URL = '/alert/api/configuration/job';

export function createReadAllRequest(apiUrl, csrfToken, context, descriptorName) {
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
    const url = `${apiUrl}?${queryString}`;
    return fetch(url, {
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createReadAllGlobalContextRequest(csrfToken, descriptorName) {
    return createReadAllRequest(CONFIG_API_URL, csrfToken, 'GLOBAL', descriptorName);
}

export function createReadRequest(apiUrl, csrfToken, configurationId) {
    const url = `${apiUrl}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createNewConfigurationRequest(apiUrl, csrfToken, fieldModel) {
    return fetch(apiUrl, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createUpdateRequest(apiUrl, csrfToken, configurationId, fieldModel) {
    const url = `${apiUrl}/${configurationId}`;
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

export function createDeleteRequest(apiUrl, csrfToken, configurationId) {
    const url = `${apiUrl}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
}

export function createValidateRequest(apiUrl, csrfToken, fieldModel) {
    const url = `${apiUrl}/validate`;
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

export function createTestRequest(apiUrl, csrfToken, fieldModel, destination) {
    let url = `${apiUrl}/test`;
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
