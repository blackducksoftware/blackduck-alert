import {CONFIG_FETCHED, CONFIG_FETCHING, CONFIG_TEST_FAILED, CONFIG_TEST_SUCCESS, CONFIG_TESTING, CONFIG_UPDATE_ERROR, CONFIG_UPDATED, CONFIG_UPDATING} from './types';

import {verifyLoginByStatus} from './session';

const CONFIG_URL = '/alert/api/configuration/provider/provider_blackduck';
const TEST_URL = '/alert/api/configuration/provider/provider_blackduck/test';

function scrubConfig(config) {
    return {
        blackDuckApiKey: config.blackDuckApiKey,
        blackDuckApiKeyIsSet: config.blackDuckApiKeyIsSet,
        blackDuckProxyHost: config.blackDuckProxyHost,
        blackDuckProxyPassword: config.blackDuckProxyPassword,
        blackDuckProxyPasswordIsSet: config.blackDuckProxyPasswordIsSet,
        blackDuckProxyPort: config.blackDuckProxyPort,
        blackDuckProxyUsername: config.blackDuckProxyUsername,
        blackDuckTimeout: config.blackDuckTimeout,
        blackDuckUrl: config.blackDuckUrl,
        id: (config.id ? `${config.id}` : '')
    };
}

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function fetchingConfig() {
    return {
        type: CONFIG_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function configFetched(config) {
    return {
        type: CONFIG_FETCHED,
        ...scrubConfig(config)
    };
}

function updatingConfig() {
    return {
        type: CONFIG_UPDATING
    };
}

/**
 * Triggers Confirm config was updated
 * @returns {{type}}
 */
function configUpdated(config) {
    return {
        type: CONFIG_UPDATED,
        ...scrubConfig(config)
    };
}

/**
 * Triggers Scheduling Config Error
 * @returns {{type}}
 */
function configError(message, errors) {
    return {
        type: CONFIG_UPDATE_ERROR,
        message,
        errors
    };
}

function testingConfig() {
    return {
        type: CONFIG_TESTING
    };
}

function testSuccess() {
    return {
        type: CONFIG_TEST_SUCCESS
    };
}

function testFailed(message, errors) {
    return {
        type: CONFIG_TEST_FAILED,
        message,
        errors

    };
}

export function getConfig() {
    return (dispatch, getState) => {
        dispatch(fetchingConfig());
        const {csrfToken} = getState().session;
        fetch(CONFIG_URL, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
            .then((response) => {
                if (response.ok) {
                    response.json().then((body) => {
                        if (body.length > 0) {
                            dispatch(configFetched(body[0]));
                        } else {
                            dispatch(configFetched({}));
                        }
                    })
                } else {
                    dispatch(verifyLoginByStatus(response.status));
                }
            })
            .catch(console.error);
    };
}

export function updateConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingConfig());
        const {csrfToken} = getState().session;
        const method = config.id ? 'PUT' : 'POST';
        const body = scrubConfig(config);

        fetch(CONFIG_URL, {
            credentials: 'same-origin',
            method,
            body: JSON.stringify(body),
            headers: {
                'content-type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        })
            .then((response) => {
                if (response.ok) {
                    response.json().then((data) => {
                        dispatch(configUpdated({...config, id: data.id}));
                    }).then(() => dispatch(getConfig()));
                } else {
                    response.json().then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(configError(data.message, data.errors));
                            case 412:
                                return dispatch(configError(data.message, data.errors));
                            default: {
                                dispatch(configError(data.message));
                                return dispatch(verifyLoginByStatus(response.status))
                            }
                        }
                    });
                }
            })
            .catch(console.error);
    };
}


export function testConfig(config) {
    return (dispatch, getState) => {
        dispatch(testingConfig());
        const {csrfToken} = getState().session;
        fetch(TEST_URL, {
            credentials: 'same-origin',
            method: 'POST',
            body: JSON.stringify(scrubConfig(config)),
            headers: {
                'content-type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        })
        // Refactor this response handler out
            .then((response) => {
                if (response.ok) {
                    dispatch(testSuccess());
                } else {
                    response.json()
                        .then((data) => {
                            switch (response.status) {
                                case 400:
                                    return dispatch(testFailed(data.message, data.errors));
                                case 401:
                                    return dispatch(testFailed('API Key isn\'t valid, try a different one'));
                                default:
                                    return dispatch(testFailed(data.message));
                            }
                        });
                }
            })
            .catch(console.error);
    };
}
