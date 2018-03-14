import {
    CONFIG_FETCHING,
    CONFIG_FETCHED,
    CONFIG_UPDATE_ERROR,
    CONFIG_UPDATING,
    CONFIG_UPDATED,
    CONFIG_TESTING,
    CONFIG_TEST_SUCCESS,
    CONFIG_TEST_FAILED
} from './types';

const CONFIG_URL = '/api/configuration/provider/hub';
const TEST_URL = '/api/configuration/provider/hub/test';

function scrubConfig(config) {
    return {
        hubApiKey: config.hubApiKey,
        hubApiKeyIsSet: config.hubApiKeyIsSet,
        hubProxyHost: config.hubProxyHost,
        hubProxyPassword: config.hubProxyPassword,
        hubProxyPasswordIsSet: config.hubProxyPasswordIsSet,
        hubProxyPort: config.hubProxyPort,
        hubProxyUsername: config.hubProxyUsername,
        hubTimeout: config.hubTimeout,
        hubUrl: config.hubUrl,
        id: (config.id?''+config.id: '')
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
        const csrfToken = getState().session.csrfToken;
        fetch(CONFIG_URL, {
            credentials: 'include',
            headers: {
              'X-CSRF-TOKEN': csrfToken
            }
        })
            .then(response => response.json().then((body) => {
                if (body.length > 0) {
                    dispatch(configFetched(body[0]));
                } else {
                    dispatch(configFetched({}));
                }
            }))
            .catch(console.error);
    };
}

export function updateConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingConfig());
        const csrfToken = getState().session.csrfToken;
        const method = config.id ? 'PUT' : 'POST';
        const body = scrubConfig(config);

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
                if (response.ok) {
                    response.json().then(data => dispatch(configUpdated({ ...config, id: data.id })));
                } else {
                    response.json()
                        .then((data) => {
                            console.log('data', data.message);
                            switch (response.status) {
                                case 400:
                                    return dispatch(configError(data.message, data.errors));
                                case 401:
                                    return dispatch(configError('API Key isn\'t valid, try a different one'));
                                case 412:
                                    return dispatch(configError(data.message, data.errors));
                                default:
                                    return dispatch(configError(data.message));
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
        const csrfToken = getState().session.csrfToken;
        fetch(TEST_URL, {
            credentials: 'include',
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
                            console.log('data', data.message);
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
