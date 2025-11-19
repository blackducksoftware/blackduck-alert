import {
    PROVIDER_CLEAR_FIELD_ERRORS,
    PROVIDER_DELETE_FAIL,
    PROVIDER_DELETE_REQUEST,
    PROVIDER_DELETE_SUCCESS,
    PROVIDER_GET_FAIL,
    PROVIDER_GET_REQUEST,
    PROVIDER_GET_SUCCESS,
    PROVIDER_POST_FAIL,
    PROVIDER_POST_REQUEST,
    PROVIDER_POST_SUCCESS,
    PROVIDER_TEST_FAIL,
    PROVIDER_TEST_REQUEST,
    PROVIDER_TEST_SUCCESS,
    PROVIDER_VALIDATE_FAIL,
    PROVIDER_VALIDATE_REQUEST,
    PROVIDER_VALIDATE_SUCCESS
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import { CONFIG_API_URL } from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import HeaderUtilities from 'common/util/HeaderUtilities';
import { createRequestUrl } from 'common/util/RequestUtilities';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';

function fetchingProvider() {
    return {
        type: PROVIDER_GET_REQUEST
    };
}

function fetchingProviderFail() {
    return {
        type: PROVIDER_GET_FAIL
    };
}

function fetchingProviderSuccess(providers) {
    return {
        type: PROVIDER_GET_SUCCESS,
        data: providers
    };
}

function saveProviderRequest() {
    return {
        type: PROVIDER_POST_REQUEST
    };
}

function saveProviderSuccess() {
    return {
        type: PROVIDER_POST_SUCCESS
    };
}

function saveProviderErrorMessage(message) {
    return {
        type: PROVIDER_POST_FAIL,
        message
    };
}

function saveProviderError({ message, errors }) {
    return {
        type: PROVIDER_POST_FAIL,
        message,
        errors
    };
}

function validatingProvider() {
    return {
        type: PROVIDER_VALIDATE_REQUEST,
        saveStatus: 'VALIDATING'
    };
}

function validatingProviderSuccess() {
    return {
        type: PROVIDER_VALIDATE_SUCCESS
    };
}

function validatingProviderError(message, errors) {
    return {
        type: PROVIDER_VALIDATE_FAIL,
        message,
        errors
    };
}

function bulkDeleteProvidersRequest() {
    return {
        type: PROVIDER_DELETE_REQUEST
    };
}

function bulkDeleteProvidersSuccess() {
    return {
        type: PROVIDER_DELETE_SUCCESS
    };
}

function bulkDeleteProvidersError(errors) {
    return {
        type: PROVIDER_DELETE_FAIL,
        errors
    };
}

function testProviderRequest() {
    return {
        type: PROVIDER_TEST_REQUEST
    };
}

function testProviderSuccess() {
    return {
        type: PROVIDER_TEST_SUCCESS
    };
}

function testProviderFail(message, errors) {
    return {
        type: PROVIDER_TEST_FAIL,
        message,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: PROVIDER_CLEAR_FIELD_ERRORS
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchProviders() {
    return (dispatch, getState) => {
        dispatch(fetchingProvider());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingProviderFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        const url = createRequestUrl(CONFIG_API_URL, 'GLOBAL', BLACKDUCK_INFO.key);

        fetch(url, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders(),
            redirect: 'manual'
        }).then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchingProviderSuccess(responseData.fieldModels));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                message = responseData.message.toString();
                            }
                            return fetchingProviderFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchingProviderFail(error));
        });
    };
}

export function validateProvider(provider) {
    return (dispatch, getState) => {
        dispatch(validatingProvider());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveProviderErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const validateRequest = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, provider);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        if (validationResponse.hasErrors) {
                            handleValidationError(dispatch, errorHandlers, response.status, () => validatingProviderError(validationResponse.message, validationResponse.errors));
                        } else {
                            dispatch(validatingProviderSuccess());
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => validatingProviderError(response.message, HTTPErrorUtils.createEmptyErrorObject()));
            }
        })
            .catch(console.error);
    };
}

export function saveProvider(provider) {
    return (dispatch, getState) => {
        dispatch(saveProviderRequest());
        const { id } = provider;
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => saveProviderErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        let saveRequest;
        if (id) {
            saveRequest = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id, provider);
        } else {
            saveRequest = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, provider);
        }
        saveRequest.then((response) => {
            if (response.ok) {
                dispatch(saveProviderSuccess());
            } else {
                response.json()
                    .then((responseData) => {
                        const defaultHandler = () => saveProviderError(responseData);
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

export function bulkDeleteProviders(providerIdArray) {
    return (dispatch, getState) => {
        dispatch(bulkDeleteProvidersRequest());
        const { csrfToken } = getState().session;

        Promise.all(providerIdArray.map((provider) => { // eslint-disable-line
            return ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, provider.id);
        })).catch((error) => {
            dispatch(bulkDeleteProvidersError(error));
            console.error; // eslint-disable-line
        }).then((response) => {
            if (response) {
                dispatch(bulkDeleteProvidersSuccess());
            }
        });
    };
}

export function testProvider(provider) {
    return (dispatch, getState) => {
        dispatch(testProviderRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => testProviderFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION, {})));
        const testRequest = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, provider);
        testRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((testResponse) => {
                        if (testResponse.hasErrors) {
                            handleValidationError(dispatch, errorHandlers, response.status, () => testProviderFail(testResponse.message, testResponse.errors));
                        } else {
                            dispatch(testProviderSuccess());
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => testProviderFail(response.message, response.errors));
            }
        })
            .catch(console.error);
    };
}

export function clearProviderFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
