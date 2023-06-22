import {
    AZURE_BOARDS_CLEAR_FIELD_ERRORS,
    AZURE_BOARDS_DELETE_FAIL,
    AZURE_BOARDS_DELETE_REQUEST,
    AZURE_BOARDS_DELETE_SUCCESS,
    AZURE_BOARDS_GET_FAIL,
    AZURE_BOARDS_GET_REQUEST,
    AZURE_BOARDS_GET_SUCCESS,
    AZURE_BOARDS_OAUTH_FAIL,
    AZURE_BOARDS_OAUTH_REQUEST,
    AZURE_BOARDS_OAUTH_SUCCESS,
    AZURE_BOARDS_SAVE_FAIL,
    AZURE_BOARDS_SAVE_REQUEST,
    AZURE_BOARDS_SAVE_SUCCESS,
    AZURE_BOARDS_TEST_FAIL,
    AZURE_BOARDS_TEST_REQUEST,
    AZURE_BOARDS_TEST_SUCCESS,
    AZURE_BOARDS_VALIDATE_FAIL,
    AZURE_BOARDS_VALIDATE_REQUEST,
    AZURE_BOARDS_VALIDATE_SUCCESS
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

function fetchAzureBoardsRequest() {
    return {
        type: AZURE_BOARDS_GET_REQUEST
    };
}

function fetchAzureBoardsSuccess(azureBoards) {
    return {
        type: AZURE_BOARDS_GET_SUCCESS,
        data: azureBoards
    };
}

function fetchAzureBoardsFail(error) {
    return {
        type: AZURE_BOARDS_GET_FAIL,
        error
    };
}

function validateAzureBoardsRequest() {
    return {
        type: AZURE_BOARDS_VALIDATE_REQUEST
    };
}

function validateAzureBoardsSuccess() {
    return {
        type: AZURE_BOARDS_VALIDATE_SUCCESS
    };
}

function validateAzureBoardsFail(message, errors) {
    return {
        type: AZURE_BOARDS_VALIDATE_FAIL,
        message,
        errors
    };
}

function saveAzureBoardsRequest() {
    return {
        type: AZURE_BOARDS_SAVE_REQUEST
    };
}

function saveAzureBoardsSuccess() {
    return {
        type: AZURE_BOARDS_SAVE_SUCCESS
    };
}

function saveAzureBoardsFail({ message, errors }) {
    return {
        type: AZURE_BOARDS_SAVE_FAIL,
        message,
        errors

    };
}

function deleteAzureBoardsRequest() {
    return {
        type: AZURE_BOARDS_DELETE_REQUEST
    };
}

function deleteAzureBoardsSuccess() {
    return {
        type: AZURE_BOARDS_DELETE_SUCCESS
    };
}

function deleteAzureBoardsError(errors) {
    return {
        type: AZURE_BOARDS_DELETE_FAIL,
        errors
    };
}

function testAzureBoardsRequest() {
    return {
        type: AZURE_BOARDS_TEST_REQUEST
    };
}

function testAzureBoardsSuccess() {
    return {
        type: AZURE_BOARDS_TEST_SUCCESS
    };
}

function testAzureBoardsFail(message, errors) {
    return {
        type: AZURE_BOARDS_TEST_FAIL,
        message,
        errors
    };
}

function sendOAuthRequest() {
    return {
        type: AZURE_BOARDS_OAUTH_REQUEST
    };
}

function sendOAuthSuccess(oAuthLink) {
    return {
        type: AZURE_BOARDS_OAUTH_SUCCESS,
        oAuthLink
    };
}

function sendOAuthError() {
    return {
        type: AZURE_BOARDS_OAUTH_FAIL
    };
}

function clearFieldErrors() {
    return {
        type: AZURE_BOARDS_CLEAR_FIELD_ERRORS
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchAzureBoards(requestParams) {
    return (dispatch, getState) => {
        dispatch(fetchAzureBoardsRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchAzureBoardsFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));

        let request;
        if (requestParams) {
            const { pageNumber, pageSize, mutatorData } = requestParams;
            request = ConfigRequestBuilder.createReadPageRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, pageNumber, pageSize, mutatorData);
        } else {
            request = ConfigRequestBuilder.createReadPageRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, 0, 10, {});
        }

        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchAzureBoardsSuccess(responseData));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchAzureBoardsFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchAzureBoardsFail(error));
        });
    };
}

export function validateAzureBoards(azureBoardsModel) {
    return (dispatch, getState) => {
        dispatch(validateAzureBoardsRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => validateAzureBoardsFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const validateRequest = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, azureBoardsModel);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        // FIXME figure out the best way to handle warning statuses
                        if (!Object.keys(validationResponse.errors).length) {
                            dispatch(validateAzureBoardsSuccess());
                        } else {
                            handleValidationError(dispatch, errorHandlers, response.status, () => validateAzureBoardsFail(validationResponse.message, validationResponse.errors));
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => validateAzureBoardsFail(response.message, HTTPErrorUtils.createEmptyErrorObject()));
            }
        }).catch(console.error);
    };
}

export function saveAzureBoards(azureBoards) {
    return (dispatch, getState) => {
        dispatch(saveAzureBoardsRequest());
        const { id } = azureBoards;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveAzureBoardsFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        let saveRequest;
        if (id) {
            saveRequest = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, id, azureBoards);
        } else {
            saveRequest = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, azureBoards);
        }
        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(saveAzureBoardsSuccess());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => saveAzureBoardsFail(responseData);
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
export function deleteAzureBoards(azureBoards) {
    return (dispatch, getState) => {
        dispatch(deleteAzureBoardsRequest());
        const { csrfToken } = getState().session;

        Promise.all(azureBoards.map((board) => { // eslint-disable-line
            return ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, board.id);
        })).catch((error) => {
            dispatch(deleteAzureBoardsError(error));
            console.error; // eslint-disable-line
        }).then((response) => {
            if (response) {
                dispatch(deleteAzureBoardsSuccess());
            }
        });
    };
}

export function testAzureBoards(azureBoards) {
    return (dispatch, getState) => {
        dispatch(testAzureBoardsRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => testAzureBoardsFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION, {})));
        const testRequest = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, azureBoards);
        testRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((testResponse) => {
                        if (testResponse.hasErrors) {
                            handleValidationError(dispatch, errorHandlers, response.status, () => testAzureBoardsFail(testResponse.message, testResponse.errors));
                        } else {
                            dispatch(testAzureBoardsSuccess());
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => testAzureBoardsFail(response.message, response.errors));
            }
        }).catch(console.error);
    };
}

export function sendOAuth(azureBoards) {
    return (dispatch, getState) => {
        dispatch(sendOAuthRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveAzureBoardsFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const oAuthRequest = ConfigRequestBuilder.createNewConfigurationRequest('/alert/api/configuration/azure-boards/oauth/authenticate', csrfToken, azureBoards);
        oAuthRequest.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(sendOAuthSuccess(responseData.authorizationUrl));
                    } else {
                        const defaultHandler = () => sendOAuthError(responseData);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function clearAzureBoardsFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
