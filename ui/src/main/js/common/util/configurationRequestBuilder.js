import HeaderUtilities from 'common/util/HeaderUtilities';

export const ALERT_API_URL = '/alert/api';
export const AUDIT_API_URL = `${ALERT_API_URL}/audit/failed`;
export const AUTHENTICATION_LDAP_API_URL = `${ALERT_API_URL}/authentication/ldap`;
export const AUTHENTICATION_SAML_API_URL = `${ALERT_API_URL}/authentication/saml`;
export const AZURE_BOARDS_API_URL = `${ALERT_API_URL}/configuration/azure-boards`;
export const CLIENT_CERTIFICATE_URL = `${ALERT_API_URL}/certificates/mtls/client`;
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
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
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
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
    });
}

export function createReadPageRequest(apiUrl, csrfToken, currentPage, pageSize, mutatorData) {
    const parameters = [
        `${encodeURIComponent('pageNumber')}=${encodeURIComponent(currentPage)}`,
        `${encodeURIComponent('pageSize')}=${encodeURIComponent(pageSize)}`
    ];
    Object.keys(mutatorData)
        .forEach((key) => {
            const value = mutatorData[key];
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
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
    });
}

export function createNewConfigurationRequest(apiUrl, csrfToken, fieldModel) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(apiUrl, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
    });
}

export function createUpdateRequest(apiUrl, csrfToken, configurationId, fieldModel) {
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    const url = configurationId ? `${apiUrl}/${configurationId}` : apiUrl;
    return fetch(url, {
        credentials: 'same-origin',
        method: 'PUT',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
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
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
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
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
    });
}

// Params for bulk delete:
//      baseUrl: request url without specifics (i.e. /api/configuration/role/)
//      configurationIdArray: array of id's staged for delete (i.e. ['1', '2', '3'])
export function createMultiDeleteRequest(baseUrl, csrfToken, configurationIdArray = null, onSuccess, onFail) {
    const stagedDeleteUrls = configurationIdArray.map((configId) => baseUrl.concat(`/${configId}`));
    const headersUtil = new HeaderUtilities();
    headersUtil.addXCsrfToken(csrfToken);

    return Promise.all(stagedDeleteUrls.map((url) => (
        fetch(url, {
            credentials: 'same-origin',
            method: 'DELETE',
            headers: headersUtil.getHeaders(),
            redirect: 'manual'
        }))
    ));
}

export function createValidateRequest(apiUrl, csrfToken, fieldModel) {
    const url = `${apiUrl}/validate`;
    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
    });
}

export function createTestRequest(apiUrl, csrfToken, fieldModel, queryParamKey, queryParamValue) {
    let url = `${apiUrl}/test`;
    if (queryParamKey) {
        url = url.concat(`?${encodeURIComponent(queryParamKey)}=${encodeURIComponent(queryParamValue)}`);
    }

    const headersUtil = new HeaderUtilities();
    headersUtil.addDefaultHeaders(csrfToken);
    return fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: JSON.stringify(fieldModel),
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
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
        headers: headersUtil.getHeaders(),
        redirect: 'manual'
    });
}
