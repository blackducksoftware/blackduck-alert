import HeaderUtilities from 'common/util/HeaderUtilities';

export const ALERT_API_URL = '/alert/api';
export const CONFIG_API_URL = `${ALERT_API_URL}/configuration`;
export const JOB_API_URL = `${ALERT_API_URL}/configuration/job`;
export const JOB_AUDIT_API_URL = `${ALERT_API_URL}/distribution/audit-statuses`;
export const ROLE_API_URL = `${ALERT_API_URL}/configuration/role`;
export const USER_API_URL = `${ALERT_API_URL}/configuration/user`;
export const SETTINGS_API_URL = `${ALERT_API_URL}/settings`;
export const ENCRYPTION_API_URL = `${SETTINGS_API_URL}/encryption`;
export const PROXY_API_URL = `${SETTINGS_API_URL}/proxy`;

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
    const headersUtil = new HeaderUtilities();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        headers: headersUtil.getHeaders()
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
    const headersUtil = new HeaderUtilities();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        headers: headersUtil.getHeaders()
    });
}

export function createNewConfigurationRequest(apiUrl, csrfToken, fieldModel) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(apiUrl, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders()
    });
}

export function createUpdateRequest(apiUrl, csrfToken, configurationId, fieldModel) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    const url = `${apiUrl}/${configurationId}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders()
    });
}

export function createUpdateWithoutIdRequest(apiUrl, csrfToken, model) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    const url = `${apiUrl}`;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(model),
        headers: headersUtil.getHeaders()
    });
}

export function createDeleteRequest(apiUrl, csrfToken, configurationId = null) {
    let url = apiUrl;
    if (configurationId) {
        url = url.concat(`/${configurationId}`);
    }

    const headersUtil = new HeaderUtilities();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'DELETE',
        headers: headersUtil.getHeaders()
    });
}

export function createValidateRequest(apiUrl, csrfToken, fieldModel) {
    const url = `${apiUrl}/validate`;
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders()
    });
}

export function createTestRequest(apiUrl, csrfToken, fieldModel, queryParam) {
    let url = `${apiUrl}/test`;
    if (queryParam) {
        url = url.concat(`?${queryParam}`);
    }

    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders()
    });
}

export function createFileUploadRequest(apiUrl, csrfToken, fieldName, files) {
    const fileData = new FormData();
    if (files && files.length) {
        for (const file of files) {
            fileData.append(`${fieldName}`, file);
        }
    }
    const headersUtil = new HeaderUtilities();
    headersUtil.addXCsrfToken(csrfToken);
    return fetch(apiUrl, {
        credentials: 'same-origin',
        method: 'POST',
        body: fileData,
        headers: headersUtil.getHeaders()
    });
}
