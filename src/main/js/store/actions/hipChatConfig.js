import {
    HIPCHAT_CONFIG_FETCHING,
    HIPCHAT_CONFIG_FETCHED,
    HIPCHAT_CONFIG_UPDATE_ERROR,
    HIPCHAT_CONFIG_UPDATING,
    HIPCHAT_CONFIG_UPDATED,
    HIPCHAT_CONFIG_TESTING,
    HIPCHAT_CONFIG_TEST_SUCCESS,
    HIPCHAT_CONFIG_TEST_FAILED
} from './types';

const CONFIG_URL = '/api/configuration/channel/hipchat';
const TEST_URL = '/api/configuration/channel/hipchat/test';

function scrubConfig(config) {
    return {
        apiKeyIsSet: config.apiKeyIsSet,
        apiKey: config.apiKey,
        id: config.id || ''
    };
}

/**
 * Triggers Config Fetching reducer
 * @returns {{type}}
 */
function fetchingConfig() {
    return {
        type: HIPCHAT_CONFIG_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function configFetched(config) {
    return {
        type: HIPCHAT_CONFIG_FETCHED,
        ...scrubConfig(config)
    };
}

/**
 * Triggers Config Error
 * @returns {{type}}
 */
function configError(message, errors) {
    return {
        type: HIPCHAT_CONFIG_UPDATE_ERROR,
        message,
        errors
    };
}

function updatingConfig() {
    return {
        type: HIPCHAT_CONFIG_UPDATING
    };
}

/**
 * Triggers Confirm config was updated
 * @returns {{type}}
 */
function configUpdated(config) {
    return {
        type: HIPCHAT_CONFIG_UPDATED,
        ...scrubConfig(config)
    };
}

function testingConfig() {
    return {
        type: HIPCHAT_CONFIG_TESTING
    };
}

function testSuccess() {
    return {
        type: HIPCHAT_CONFIG_TEST_SUCCESS
    };
}

function testFailed(message, errors) {
    return {
        type: HIPCHAT_CONFIG_TEST_FAILED,
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
            .then(response =>
                response.json().then((body) => {
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
                                    return dispatch(configError('API Key isn\'t valid, try a different one', null));
                                case 412:
                                    return dispatch(configError(data.message, data.errors));
                                default:
                                    return dispatch(configError(data.message, null));
                            }
                        });
                }
            })
            .then(() => {
                dispatch(getConfig());
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
