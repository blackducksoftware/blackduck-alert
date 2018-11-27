import {
    HIPCHAT_CONFIG_FETCHED,
    HIPCHAT_CONFIG_FETCHING,
    HIPCHAT_CONFIG_HIDE_HOST_SERVER,
    HIPCHAT_CONFIG_HIDE_TEST_MODAL,
    HIPCHAT_CONFIG_SHOW_HOST_SERVER,
    HIPCHAT_CONFIG_SHOW_TEST_MODAL,
    HIPCHAT_CONFIG_TEST_FAILED,
    HIPCHAT_CONFIG_TEST_SUCCESS,
    HIPCHAT_CONFIG_TESTING,
    HIPCHAT_CONFIG_UPDATE_ERROR,
    HIPCHAT_CONFIG_UPDATED,
    HIPCHAT_CONFIG_UPDATING
} from './types';

import {verifyLoginByStatus} from './session';

const CONFIG_URL = '/alert/api/configuration/channel/global/channel_hipchat';
const TEST_URL = `${CONFIG_URL}/test`;

function scrubConfig(config) {
    return {
        apiKeyIsSet: config.apiKeyIsSet,
        apiKey: config.apiKey,
        hostServer: config.hostServer,
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

export function openHipChatConfigTest() {
    return {
        type: HIPCHAT_CONFIG_SHOW_TEST_MODAL
    };
}

export function closeHipChatConfigTest() {
    return {
        type: HIPCHAT_CONFIG_HIDE_TEST_MODAL
    };
}

export function toggleShowHostServer(toggle) {
    if (toggle) {
        return {type: HIPCHAT_CONFIG_SHOW_HOST_SERVER};
    }
    return {type: HIPCHAT_CONFIG_HIDE_HOST_SERVER};
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

        const method = config.id ? 'PUT' : 'POST';
        const body = scrubConfig(config);
        const {csrfToken} = getState().session;
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
                    }).then(() => {
                        dispatch(getConfig());
                    });
                } else {
                    response.json()
                        .then((data) => {
                            switch (response.status) {
                                case 400:
                                    return dispatch(configError(data.message, data.errors));
                                case 412:
                                    return dispatch(configError(data.message, data.errors));
                                default: {
                                    dispatch(configError(data.message, null));
                                    return dispatch(verifyLoginByStatus(response.status));
                                }
                            }
                        });
                }
            })
            .catch(console.error);
    };
}

export function testConfig(config, destination) {
    return (dispatch, getState) => {
        dispatch(testingConfig());
        const {csrfToken} = getState().session;
        const requestUrl = `${TEST_URL}?destination=${destination}`;
        fetch(requestUrl, {
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
                dispatch(closeHipChatConfigTest());
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
