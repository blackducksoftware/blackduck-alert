import {
    HIPCHAT_CONFIG_FETCHED,
    HIPCHAT_CONFIG_FETCHING,
    HIPCHAT_CONFIG_HIDE_TEST_MODAL,
    HIPCHAT_CONFIG_SHOW_TEST_MODAL,
    HIPCHAT_CONFIG_TEST_FAILED,
    HIPCHAT_CONFIG_TEST_SUCCESS,
    HIPCHAT_CONFIG_TESTING,
    HIPCHAT_CONFIG_UPDATE_ERROR,
    HIPCHAT_CONFIG_UPDATED,
    HIPCHAT_CONFIG_UPDATING
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

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
        config
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
function configUpdated(config, message) {
    return {
        type: HIPCHAT_CONFIG_UPDATED,
        message,
        config
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

export function getConfig() {
    return (dispatch, getState) => {
        dispatch(fetchingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_HIPCHAT);
        request.then((response) => {
            if (response.ok) {
                response.json().then((body) => {
                    if (body.length > 0) {
                        dispatch(configFetched(body[0]));
                    } else {
                        dispatch(configFetched({}));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    };
}

export function updateConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingConfig());
        const { csrfToken } = getState().session;
        let request;
        if (config.id) {
            request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config.id, config);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config);
        }
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const updatedConfig = FieldModelUtilities.updateFieldModelSingleValue(config, 'id', data.id);
                    dispatch(configUpdated(updatedConfig, data.message));
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
        }).catch(console.error);
    };
}

export function testConfig(config, destination) {
    return (dispatch, getState) => {
        dispatch(testingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config, destination);
        request.then((response) => {
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
