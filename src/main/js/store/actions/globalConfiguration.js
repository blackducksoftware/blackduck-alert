import {
    CONFIG_ALL_FETCHED,
    CONFIG_CLEAR_FIELD_ERRORS,
    CONFIG_DELETED,
    CONFIG_DELETING,
    CONFIG_FETCHED,
    CONFIG_FETCHING,
    CONFIG_REFRESH,
    CONFIG_REFRESHING,
    CONFIG_TEST_FAILED,
    CONFIG_TEST_SUCCESS,
    CONFIG_TESTING,
    CONFIG_UPDATE_ERROR,
    CONFIG_UPDATED,
    CONFIG_UPDATING
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

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

function configAllFetched(config) {
    return {
        type: CONFIG_ALL_FETCHED,
        config
    }
}

function configRefreshed(config) {
    return {
        type: CONFIG_REFRESH,
        config
    };
}

function refreshingConfig() {
    return {
        type: CONFIG_REFRESHING
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

function deletingConfig() {
    return {
        type: CONFIG_DELETING
    };
}

function configDeleted() {
    return {
        type: CONFIG_DELETED
    };
}

function clearFieldErrors() {
    return {
        type: CONFIG_CLEAR_FIELD_ERRORS
    };
}

function handleFailureResponse(dispatch, response) {
    response.json().then((data) => {
        switch (response.status) {
            case 400:
            case 412:
                return dispatch(configError(data.message, data.errors));
            default: {
                dispatch(configError(data.message, null));
                return dispatch(verifyLoginByStatus(response.status));
            }
        }
    });
}

export function refreshConfig(id) {
    return (dispatch, getState) => {
        dispatch(refreshingConfig());
        if (!id) {
            dispatch(configRefreshed({}));
            return;
        }
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id);
        request.then((response) => {
            if (response.ok) {
                response.json().then((body) => {
                    if (body) {
                        dispatch(configRefreshed(body));
                    } else {
                        dispatch(configRefreshed({}));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    };
}

export function getAllConfigs(descriptorName) {
    return (dispatch, getState) => {
        dispatch(fetchingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, descriptorName);
        request.then((response) => {
            if (response.ok) {
                response.json().then((body) => {
                    if (body.length > 0) {
                        dispatch(configAllFetched(body));
                    } else {
                        dispatch(configAllFetched({}));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    };
}

export function getConfig(descriptorName) {
    return (dispatch, getState) => {
        dispatch(fetchingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, descriptorName);
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
        const id = FieldModelUtilities.getFieldModelId(config);
        if (id) {
            request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id, config);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config);
        }
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const newId = data.id;
                    const updatedConfig = FieldModelUtilities.updateFieldModelSingleValue(config, 'id', newId);
                    dispatch(configUpdated(updatedConfig));
                    dispatch(refreshConfig(newId));
                    return newId;
                });
            } else {
                handleFailureResponse(dispatch, response);
            }
        }).catch(console.error);
    };
}


export function testConfig(config) {
    return (dispatch, getState) => {
        dispatch(testingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config);
        request.then((response) => {
            if (response.ok) {
                dispatch(testSuccess());
            } else {
                response.json().then((data) => {
                    switch (response.status) {
                        case 400:
                        case 401:
                            let { message } = data;
                            if (!data.message) {
                                message = 'There was a problem testing your configuration.';
                            }
                            return dispatch(testFailed(message, data.errors));
                        default:
                            return dispatch(testFailed(data.message));
                    }
                });
            }
        }).catch(console.error);
    };
}

export function deleteConfig(id) {
    return (dispatch, getState) => {
        dispatch(deletingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id);
        request.then((response) => {
            if (response.ok) {
                response.json().then(() => {
                    dispatch(configDeleted());
                });
            } else {
                handleFailureResponse(dispatch, response);
            }
        }).catch(console.error);
    };
}

export function clearConfigFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
