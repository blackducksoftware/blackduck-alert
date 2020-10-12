export const ALERT_API_URL = '/alert/api';
export const CONFIG_API_URL = `${ALERT_API_URL}/configuration`;
export const JOB_API_URL = `${ALERT_API_URL}/configuration/job`;
export const ROLE_API_URL = `${ALERT_API_URL}/configuration/role`;
export const USER_API_URL = `${ALERT_API_URL}/configuration/user`;

export function createReadAllRequest(apiUrl, csrfToken, context, descriptorName) {
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
    const url = `${apiUrl}?${queryString}`;
    const headers = new Headers();
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        headers
    });
}

export function createReadAllGlobalContextRequest(csrfToken, descriptorName) {
    return createReadAllRequest(CONFIG_API_URL, csrfToken, 'GLOBAL', descriptorName);
}

export function createReadRequest(apiUrl, csrfToken, configurationId = null) {
    let url = apiUrl;
    if (configurationId) {
        url = url.concat(`/${configurationId}`);
    }
    const headers = new Headers();
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        headers
    });
}

export function createNewConfigurationRequest(apiUrl, csrfToken, fieldModel) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(apiUrl, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers
    });
}

export function createUpdateRequest(apiUrl, csrfToken, configurationId, fieldModel) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    const url = `${apiUrl}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(fieldModel),
        headers
    });
}

export function createUpdateWithoutIdRequest(apiUrl, csrfToken, model) {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    const url = `${apiUrl}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(model),
        headers
    });
}

export function createDeleteRequest(apiUrl, csrfToken, configurationId = null) {
    let url = apiUrl;
    if (configurationId) {
        url = url.concat(`/${configurationId}`);
    }

    const headers = new Headers();
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers
    });
}

export function createValidateRequest(apiUrl, csrfToken, fieldModel) {
    const url = `${apiUrl}/validate`;
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers
    });
}

export function createTestRequest(apiUrl, csrfToken, fieldModel) {
    const url = `${apiUrl}/test`;
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers
    });
}

export function createFileUploadRequest(apiUrl, csrfToken, fieldName, files) {
    const fileData = new FormData();
    if (files && files.length) {
        for (const file of files) {
            fileData.append(`${fieldName}`, file);
        }
    }
    const headers = new Headers();
    headers.append('X-CSRF-TOKEN', csrfToken);
    return fetch(apiUrl, {
        credentials: 'same-origin',
        method: 'POST',
        body: fileData,
        headers
    });
}
