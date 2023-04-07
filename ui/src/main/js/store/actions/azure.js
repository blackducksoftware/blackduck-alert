import {
    AZURE_GET_REQUEST,
    AZURE_GET_SUCCESS,
    AZURE_GET_FAIL,
    AZURE_VALIDATE_REQUEST,
    AZURE_VALIDATE_SUCCESS,
    AZURE_VALIDATE_FAIL,
    AZURE_SAVE_REQUEST,
    AZURE_SAVE_SUCCESS,
    AZURE_SAVE_FAIL,
    AZURE_DELETE_REQUEST,
    AZURE_DELETE_SUCCESS,
    AZURE_DELETE_FAIL,
    AZURE_OAUTH_REQUEST,
    AZURE_OAUTH_SUCCESS,
    AZURE_OAUTH_FAIL,
    AZURE_CLEAR_FIELD_ERRORS
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

function fetchAzureRequest() {
    return {
        type: AZURE_GET_REQUEST
    };
}

function fetchAzureSuccess(azure) {
    return {
        type: AZURE_GET_SUCCESS,
        data: azure
    };
}

function fetchAzureFail(error) {
    return {
        type: AZURE_GET_FAIL,
        error
    };
}

function validateAzureRequest() {
    return {
        type: AZURE_VALIDATE_REQUEST
    };
}

function validateAzureSuccess() {
    return {
        type: AZURE_VALIDATE_SUCCESS
    };
}

function validateAzureFail(message, errors) {
    return {
        type: AZURE_VALIDATE_FAIL,
        message,
        errors
    };
}

function saveAzureRequest() {
    return {
        type: AZURE_SAVE_REQUEST
    };
}

function saveAzureSuccess() {
    return {
        type: AZURE_SAVE_SUCCESS
    };
}

function saveAzureFail({ message, errors }) {
    return {
        type: AZURE_SAVE_FAIL,
        message,
        errors

    };
}

function deleteProvidersRequest() {
    return {
        type: AZURE_DELETE_REQUEST
    };
}

function deleteProvidersSuccess() {
    return {
        type: AZURE_DELETE_SUCCESS
    };
}

function deleteProvidersError(errors) {
    return {
        type: AZURE_DELETE_FAIL,
        errors
    };
}

function sendOAuthRequest() {
    return {
        type: AZURE_OAUTH_REQUEST
    };
}

function sendOAuthSuccess(oAuthLink) {
    return {
        type: AZURE_OAUTH_SUCCESS,
        oAuthLink
    };
}

function sendOAuthError() {
    return {
        type: AZURE_OAUTH_FAIL
    };
}

function clearFieldErrors() {
    return {
        type: AZURE_CLEAR_FIELD_ERRORS
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchAzure(requestParams) {
    return (dispatch, getState) => {
        dispatch(fetchAzureRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchAzureFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));

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
                        dispatch(fetchAzureSuccess(responseData));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchAzureFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchAzureFail(error));
        });
    };
}

export function validateAzure(azureModel) {
    return (dispatch, getState) => {
        dispatch(validateAzureRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => validateAzureFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const validateRequest = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, azureModel);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        // FIXME figure out the best way to handle warning statuses
                        if (!Object.keys(validationResponse.errors).length) {
                            dispatch(validateAzureSuccess());
                        } else {
                            handleValidationError(dispatch, errorHandlers, response.status, () => validateAzureFail(validationResponse.message, validationResponse.errors));
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => validateAzureFail(response.message, HTTPErrorUtils.createEmptyErrorObject()));
            }
        }).catch(console.error);
    };
}

export function saveAzureBoard(azureBoard) {
    return (dispatch, getState) => {
        dispatch(saveAzureRequest());
        const { id } = azureBoard;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveAzureFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        let saveRequest;
        if (id) {
            saveRequest = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, id, azureBoard);
        } else {
            saveRequest = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, azureBoard);
        }
        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(saveAzureSuccess());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => saveAzureFail(responseData);
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
        dispatch(deleteProvidersRequest());
        const { csrfToken } = getState().session;

        Promise.all(azureBoards.map((board) => { // eslint-disable-line
            return ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.AZURE_BOARDS_API_URL, csrfToken, board.id);
        })).catch((error) => {
            dispatch(deleteProvidersError(error));
            console.error; // eslint-disable-line
        }).then((response) => {
            if (response) {
                dispatch(deleteProvidersSuccess());
            }
        });
    };
}

export function sendOAuth(azureBoard) {
    return (dispatch, getState) => {
        dispatch(sendOAuthRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveAzureFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const oAuthRequest = ConfigRequestBuilder.createNewConfigurationRequest('/alert/api/configuration/azure-boards/oauth/authenticate', csrfToken, azureBoard);
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

export function clearAzureFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
