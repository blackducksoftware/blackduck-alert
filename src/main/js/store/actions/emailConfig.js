import {
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_UPDATING,
    EMAIL_CONFIG_UPDATED,
    EMAIL_CONFIG_SHOW_ADVANCED,
    EMAIL_CONFIG_HIDE_ADVANCED
} from './types';

const CONFIG_URL = '/api/configuration/global/email';

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
        config
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
        config
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
    return (dispatch) => {
        dispatch(fetchingEmailConfig());

        fetch(CONFIG_URL, {
            credentials: 'include'
        })
        .then((response) => response.json())
        .then((body) => { dispatch(emailConfigFetched(body[0])); })
        .catch(console.error);
    }
};

export function updateEmailConfig(config) {
    return (dispatch) => {
        dispatch(updatingEmailConfig());

        fetch(CONFIG_URL, {
            method: 'POST',
            credentials: 'include'
        })
            .then((response) => response.json())
            .then((body) => { dispatch(emailConfigUpdated(body[0])); })
            .catch(console.error);
    }
};