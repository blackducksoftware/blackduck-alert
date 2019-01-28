import { CONFIG_FETCHED, CONFIG_FETCHING, CONFIG_TEST_FAILED, CONFIG_TEST_SUCCESS, CONFIG_TESTING, CONFIG_UPDATE_ERROR, CONFIG_UPDATED, CONFIG_UPDATING } from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtil from 'util/fieldModelUtilities';

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
        config
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
        config
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
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, 'provider_blackduck');
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
        })
            .catch(console.error);
    };
}

export function updateConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingConfig());
        const { csrfToken } = getState().session;
        let request;
        if (config.id) {
            request = ConfigRequestBuilder.createUpdateRequest(csrfToken, config.id, config);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(csrfToken, config);
        }
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const updatedConfig = FieldModelUtil.updateFieldModelSingleValue(config, 'id', data.id);
                    dispatch(configUpdated(updatedConfig));
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
                            return dispatch(verifyLoginByStatus(response.status));
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
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createTestRequest(csrfToken, config, '');
        request.then((response) => {
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
