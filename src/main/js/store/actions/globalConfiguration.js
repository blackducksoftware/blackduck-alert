import {
    CONFIG_ALL_FETCHED,
    CONFIG_CLEAR_FIELD_ERRORS,
    CONFIG_DELETE_ERROR,
    CONFIG_DELETED,
    CONFIG_DELETING,
    CONFIG_FETCH_ALL_ERROR,
    CONFIG_FETCH_ERROR,
    CONFIG_FETCHED,
    CONFIG_FETCHING,
    CONFIG_REFRESH,
    CONFIG_REFRESH_ERROR,
    CONFIG_REFRESHING,
    CONFIG_TEST_FAILED,
    CONFIG_TEST_SUCCESS,
    CONFIG_TESTING,
    CONFIG_UPDATE_ERROR,
    CONFIG_UPDATED,
    CONFIG_UPDATING,
    CONFIG_VALIDATE_ERROR,
    CONFIG_VALIDATED,
    CONFIG_VALIDATING
} from 'store/actions/types';

import { unauthorized } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

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
    };
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
function configError({ type = '', message, errors }) {
    return {
        type,
        message,
        errors
    };
}

function configFetchAllError(message) {
    return {
        type: CONFIG_FETCH_ALL_ERROR,
        message
    };
}

function configFetchError(message) {
    return {
        type: CONFIG_FETCH_ERROR,
        message
    };
}

function configRefreshError(message) {
    return {
        type: CONFIG_REFRESH_ERROR,
        message
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

function testFailed({ message, errors }) {
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

function validatingConfig() {
    return {
        type: CONFIG_VALIDATING
    };
}

function validatedConfig() {
    return {
        type: CONFIG_VALIDATED
    };
}

function configValidationError({ message, errors }) {
    return {
        type: CONFIG_VALIDATE_ERROR,
        message,
        errors
    };
}

function createErrorHandler(type, defaultHandler) {
    const errorHandlers = [];
    errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => configError({ type, message: HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION })));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createPreconditionFailedHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));

    return HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
}

function processValidationResponse(dispatch, response, responseData, errorHandler) {
    if (response.ok) {
        if (responseData.hasErrors) {
            dispatch(errorHandler(400));
        } else {
            dispatch(validatedConfig());
        }
    } else {
        dispatch(errorHandler(response.status));
    }
}

export function refreshConfig(id) {
    return (dispatch, getState) => {
        dispatch(refreshingConfig());
        if (!id) {
            dispatch(configRefreshed({}));
            return;
        }
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => configRefreshError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const request = ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        if (responseData) {
                            dispatch(configRefreshed(responseData));
                        } else {
                            dispatch(configRefreshed({}));
                        }
                    } else {
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function getAllConfigs(descriptorName) {
    return (dispatch, getState) => {
        dispatch(fetchingConfig());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => configFetchAllError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, descriptorName);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        if (responseData.length > 0) {
                            dispatch(configAllFetched(responseData));
                        } else {
                            dispatch(configAllFetched({}));
                        }
                    } else {
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function getConfig(descriptorName) {
    return (dispatch, getState) => {
        dispatch(fetchingConfig());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => configFetchError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, descriptorName);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        if (responseData.length > 0) {
                            dispatch(configFetched(responseData[0]));
                        } else {
                            dispatch(configFetched({}));
                        }
                    } else {
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function validateConfig(config) {
    return (dispatch, getState) => {
        dispatch(validatingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    const handler = createErrorHandler(CONFIG_VALIDATE_ERROR, () => configValidationError(responseData));
                    processValidationResponse(dispatch, response, responseData, handler);
                });
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
                if (response.status === 201) {
                    response.json()
                        .then((responseData) => {
                            const newId = responseData.id;
                            const updatedConfig = FieldModelUtilities.updateFieldModelSingleValue(config, 'id', newId);
                            dispatch(configUpdated(updatedConfig));
                            dispatch(refreshConfig(newId));
                        });
                } else {
                    dispatch(refreshConfig(id));
                }
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => configError({
                            type: CONFIG_UPDATE_ERROR,
                            ...responseData
                        });
                        const handler = createErrorHandler(CONFIG_UPDATE_ERROR, defaultHandler());
                        dispatch(handler(response.status));
                    });
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
            response.json()
                .then((responseData) => {
                    const handler = createErrorHandler(CONFIG_TEST_FAILED, () => testFailed(responseData));
                    processValidationResponse(dispatch, response, responseData, handler);
                });
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
                dispatch(configDeleted());
                dispatch(refreshConfig());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => configError({
                            type: CONFIG_DELETE_ERROR,
                            ...responseData
                        });
                        const handler = createErrorHandler(CONFIG_DELETE_ERROR, defaultHandler);
                        dispatch(handler(response.status));
                    });
            }
        }).catch(console.error);
    };
}

export function clearConfigFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
