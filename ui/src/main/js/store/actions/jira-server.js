import {
    JIRA_SERVER_CLEAR_FIELD_ERRORS,
    JIRA_SERVER_DELETE_FAIL,
    JIRA_SERVER_DELETE_REQUEST,
    JIRA_SERVER_DELETE_SUCCESS,
    JIRA_SERVER_GET_FAIL,
    JIRA_SERVER_GET_REQUEST,
    JIRA_SERVER_GET_SUCCESS,
    JIRA_SERVER_PLUGIN_FAIL,
    JIRA_SERVER_PLUGIN_REQUEST,
    JIRA_SERVER_PLUGIN_SUCCESS,
    JIRA_SERVER_SAVE_FAIL,
    JIRA_SERVER_SAVE_REQUEST,
    JIRA_SERVER_SAVE_SUCCESS,
    JIRA_SERVER_TEST_FAIL,
    JIRA_SERVER_TEST_REQUEST,
    JIRA_SERVER_TEST_SUCCESS,
    JIRA_SERVER_VALIDATE_FAIL,
    JIRA_SERVER_VALIDATE_REQUEST,
    JIRA_SERVER_VALIDATE_SUCCESS
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import { JIRA_SERVER_URLS } from 'page/channel/jira/server/JiraServerModel';

function fetchJiraServerRequest() {
    return {
        type: JIRA_SERVER_GET_REQUEST
    };
}

function fetchJiraServerSuccess(data) {
    return {
        type: JIRA_SERVER_GET_SUCCESS,
        data
    };
}

function fetchJiraServerFail(error) {
    return {
        type: JIRA_SERVER_GET_FAIL,
        error
    };
}

function validateJiraServerRequest() {
    return {
        type: JIRA_SERVER_VALIDATE_REQUEST
    };
}

function validateJiraServerSuccess() {
    return {
        type: JIRA_SERVER_VALIDATE_SUCCESS
    };
}

function validateJiraServerFail(message, errors) {
    return {
        type: JIRA_SERVER_VALIDATE_FAIL,
        message,
        errors
    };
}

function saveJiraServerRequest() {
    return {
        type: JIRA_SERVER_SAVE_REQUEST
    };
}

function saveJiraServerSuccess() {
    return {
        type: JIRA_SERVER_SAVE_SUCCESS
    };
}

function saveJiraServerFail({ message, errors }) {
    return {
        type: JIRA_SERVER_SAVE_FAIL,
        message,
        errors

    };
}

function deleteJiraServerRequest() {
    return {
        type: JIRA_SERVER_DELETE_REQUEST
    };
}

function deleteJiraServerSuccess() {
    return {
        type: JIRA_SERVER_DELETE_SUCCESS
    };
}

function deleteJiraServerError(errors) {
    return {
        type: JIRA_SERVER_DELETE_FAIL,
        errors
    };
}

function testJiraServerRequest() {
    return {
        type: JIRA_SERVER_TEST_REQUEST
    };
}

function testJiraServerSuccess() {
    return {
        type: JIRA_SERVER_TEST_SUCCESS
    };
}

function testJiraServerrFail(message, errors) {
    return {
        type: JIRA_SERVER_TEST_FAIL,
        message,
        errors
    };
}

function sendJiraServerPluginRequest() {
    return {
        type: JIRA_SERVER_PLUGIN_REQUEST
    };
}

function sendJiraServerPluginSuccess(message) {
    return {
        type: JIRA_SERVER_PLUGIN_SUCCESS,
        message
    };
}

function sendJiraServerPluginError(message, errors) {
    return {
        type: JIRA_SERVER_PLUGIN_FAIL,
        message,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: JIRA_SERVER_CLEAR_FIELD_ERRORS
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchJiraServer(requestParams) {
    return (dispatch, getState) => {
        dispatch(fetchJiraServerRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchJiraServerFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));

        let request;
        if (requestParams) {
            const { pageNumber, pageSize, mutatorData } = requestParams;
            request = ConfigRequestBuilder.createReadPageRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, pageNumber, pageSize, mutatorData);
        } else {
            request = ConfigRequestBuilder.createReadPageRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, 0, 10, {});
        }

        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchJiraServerSuccess(responseData));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchJiraServerFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchJiraServerFail(error));
        });
    };
}

export function validateJiraServer(jiraServerModel) {
    return (dispatch, getState) => {
        dispatch(validateJiraServerRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => validateJiraServerFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const validateRequest = ConfigRequestBuilder.createValidateRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, jiraServerModel);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        // FIXME figure out the best way to handle warning statuses
                        if (!Object.keys(validationResponse.errors).length) {
                            dispatch(validateJiraServerSuccess());
                        } else {
                            handleValidationError(dispatch, errorHandlers, response.status, () => validateJiraServerFail(validationResponse.message, validationResponse.errors));
                        }
                    });
            } else {
                response.json().then((responseData) => {
                    if (responseData && responseData.status === 400) {
                        handleValidationError(dispatch, errorHandlers, response.status, () => validateJiraServerFail({...responseData, isBadRequest: true}, HTTPErrorUtils.createEmptyErrorObject()));
                    } else {
                        handleValidationError(dispatch, errorHandlers, response.status, () => validateJiraServerFail(response.message, HTTPErrorUtils.createEmptyErrorObject()));
                    }
                });
            }
        })
            .catch(console.error);
    };
}

export function saveJiraServer(jiraServerModel) {
    return (dispatch, getState) => {
        dispatch(saveJiraServerRequest());
        const { id } = jiraServerModel;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveJiraServerFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        let saveRequest;
        if (id) {
            saveRequest = ConfigRequestBuilder.createUpdateRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, id, jiraServerModel);
        } else {
            saveRequest = ConfigRequestBuilder.createNewConfigurationRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, jiraServerModel);
        }
        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(saveJiraServerSuccess());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => saveJiraServerFail(responseData);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    });
            }
        })
            .catch(console.error);
    };
}

export function deleteJiraServer(jiraServerModels) {
    return (dispatch, getState) => {
        dispatch(deleteJiraServerRequest());
        const { csrfToken } = getState().session;

        Promise.all(jiraServerModels.map((server) => { // eslint-disable-line
            return ConfigRequestBuilder.createDeleteRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, server.id);
        })).catch((error) => {
            dispatch(deleteJiraServerError(error));
            console.error; // eslint-disable-line
        }).then((response) => {
            if (response) {
                dispatch(deleteJiraServerSuccess());
            }
        });
    };
}

export function testJiraServer(jiraServerModel) {
    return (dispatch, getState) => {
        dispatch(testJiraServerRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => testJiraServerrFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION, {})));
        const testRequest = ConfigRequestBuilder.createTestRequest(JIRA_SERVER_URLS.jiraServerConfigUrl, csrfToken, jiraServerModel);
        testRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((testResponse) => {
                        if (testResponse.hasErrors) {
                            handleValidationError(dispatch, errorHandlers, response.status, () => testJiraServerrFail(testResponse.message, testResponse.errors));
                        } else {
                            dispatch(testJiraServerSuccess());
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => testJiraServerrFail(response.message, response.errors));
            }
        })
            .catch(console.error);
    };
}

export function installJiraServerPlugin(jiraServerModel) {
    return (dispatch, getState) => {
        dispatch(sendJiraServerPluginRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveJiraServerFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const request = ConfigRequestBuilder.createNewConfigurationRequest(JIRA_SERVER_URLS.jiraServerPluginUrl, csrfToken, jiraServerModel);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(sendJiraServerPluginSuccess(responseData.message));
                    } else {
                        const defaultHandler = () => sendJiraServerPluginError(responseData.message, responseData.error);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function clearJiraServerFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
