import {
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_UPDATE_ERROR,
    EMAIL_CONFIG_UPDATING,
    EMAIL_CONFIG_UPDATED,
    EMAIL_CONFIG_SHOW_ADVANCED,
    EMAIL_CONFIG_HIDE_ADVANCED
} from './types';

const CONFIG_URL = '/api/configuration/channel/email';

function scrubConfig(config) {
    return {
        mailSmtpHost: config.mailSmtpHost,
        mailSmtpFrom: config.mailSmtpFrom,
        mailSmtpAuth: config.mailSmtpAuth,
        mailSmtpUser: config.mailSmtpUser,
        mailSmtpPassword: config.mailSmtpPassword,
        mailSmtpPasswordIsSet: config.mailSmtpPasswordIsSet,
        mailSmtpPort: config.mailSmtpPort,
        mailSmtpConnectionTimeout: config.mailSmtpConnectionTimeout,
        mailSmtpTimeout: config.mailSmtpTimeout,
        mailSmtpLocalhost: config.mailSmtpLocalhost,
        mailSmtpEhlo: config.mailSmtpEhlo,
        mailSmtpDnsNotify: config.mailSmtpDnsNotify,
        mailSmtpDnsRet: config.mailSmtpDnsRet,
        mailSmtpAllow8bitmime: config.mailSmtpAllow8bitmime,
        mailSmtpSendPartial: config.mailSmtpSendPartial,
        id: config.id
    };
}

/**
 * Triggers Email Config Fetching reducer
 * @returns {{type}}
 */
function fetchingEmailConfig() {
    return {
        type: EMAIL_CONFIG_FETCHING
    };
}

/**
 * Triggers Email Config Fetched Reducer
 * @returns {{type}}
 */
function emailConfigFetched(config) {
    return {
        type: EMAIL_CONFIG_FETCHED,
        config: { ...scrubConfig(config) }
    };
}

/**
 * Triggers Config Error
 * @returns {{type}}
 */
function configError(message, errors) {
    return {
        type: EMAIL_CONFIG_UPDATE_ERROR,
        message,
        errors
    };
}

/**
 * Triggers Email Config Fetching reducer
 * @returns {{type}}
 */
function updatingEmailConfig() {
    return {
        type: EMAIL_CONFIG_UPDATING
    };
}

/**
 * Triggers Email Config Fetched Reducer
 * @returns {{type}}
 */
function emailConfigUpdated(config) {
    return {
        type: EMAIL_CONFIG_UPDATED,
        config: { ...scrubConfig(config) }
    };
}

export function toggleAdvancedEmailOptions(toggle) {
    if(toggle) {
        return { type: EMAIL_CONFIG_SHOW_ADVANCED };
    } else {
        return { type: EMAIL_CONFIG_HIDE_ADVANCED };
    }
}

export function getEmailConfig() {
    return (dispatch, getState) => {
        dispatch(fetchingEmailConfig());
        const csrfToken = getState().session.csrfToken;
        fetch(CONFIG_URL, {
            credentials: 'include',
            headers: {
              'X-CSRF-TOKEN': csrfToken
            }
        })
        .then((response) => response.json())
        .then((body) => {
            dispatch(emailConfigFetched(body[0])); })
        .catch(console.error);
    }
};

export function updateEmailConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingEmailConfig());
        const method = config.id ? 'PUT' : 'POST';
        const body = scrubConfig(config);
        const csrfToken = getState().session.csrfToken;
        fetch(CONFIG_URL, {
            credentials: 'include',
            method,
            body: JSON.stringify(body),
            headers: {
                'content-type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then((response) => {
            if(response.ok) {
                response.json().then((body) => {dispatch(emailConfigUpdated({...config}))});
            } else {
                response.json()
                    .then((data) => {
                        console.log('data', data.message);
                        switch(response.status) {
                            case 400:
                                return dispatch(configError(data.message, data.errors));
                            case 412:
                                return dispatch(configError(data.message, data.errors));
                            default:
                                dispatch(configError(data.message, null));
                        }
                    });
            }
        })
        .catch(console.error);
    }
};
