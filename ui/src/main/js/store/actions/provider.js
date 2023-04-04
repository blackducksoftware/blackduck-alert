import {
    BULK_DELETE_PROVIDER_REQUEST,
    BULK_DELETE_PROVIDER_FAIL,
    BULK_DELETE_PROVIDER_SUCCESS,
    GET_PROVIDER_REQUEST,
    GET_PROVIDER_FAIL,
    GET_PROVIDER_SUCCESS,
    POST_PROVIDER_REQUEST,
    POST_PROVIDER_FAIL,
    POST_PROVIDER_SUCCESS,
    VALIDATE_PROVIDER_REQUEST,
    VALIDATE_PROVIDER_FAIL,
    VALIDATE_PROVIDER_SUCCESS,
    CLEAR_PROVIDER_FIELD_ERRORS
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import HeaderUtilities from 'common/util/HeaderUtilities';
import { createRequestUrl } from 'common/util/RequestUtilities';
import { CONFIG_API_URL } from 'common/util/configurationRequestBuilder';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';

function fetchingProvider() {
    return {
        type: GET_PROVIDER_REQUEST
    }
}

function fetchingProviderFail() {
    return {
        type: GET_PROVIDER_FAIL
    }
}

function fetchingProviderSuccess(providers) {
    return {
        type: GET_PROVIDER_SUCCESS,
        data: providers
    }
}

function savingProvider() {
    return {
        type: POST_PROVIDER_REQUEST
    }
}

function savedProvider() {
    return {
        type: POST_PROVIDER_SUCCESS
    };
}

function saveProviderErrorMessage(message) {
    return {
        type: POST_PROVIDER_FAIL,
        message
    };
}

function saveProviderError({ message, errors }) {
    return {
        type: POST_PROVIDER_FAIL,
        message,
        errors
    };
}

function validatingProvider() {
    return {
        type: VALIDATE_PROVIDER_REQUEST,
        saveStatus: 'VALIDATING'
    };
}

function validatingProviderSuccess() {
    return {
        type: VALIDATE_PROVIDER_SUCCESS
    };
}

function validatingProviderError(message, errors) {
    return {
        type: VALIDATE_PROVIDER_FAIL,
        message,
        errors
    };
}

function bulkDeleteProvidersRequest() {
    return {
        type: BULK_DELETE_PROVIDER_REQUEST
    };
}

function bulkDeleteProvidersSuccess() {
    return {
        type: BULK_DELETE_PROVIDER_SUCCESS
    };
}

function bulkDeleteProvidersError(errors) {
    return {
        type: BULK_DELETE_PROVIDER_FAIL,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: CLEAR_PROVIDER_FIELD_ERRORS
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

export function fetchProviders () {
    return (dispatch, getState) => {
        dispatch(fetchingProvider());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingProviderFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        const url = createRequestUrl(CONFIG_API_URL, 'GLOBAL', BLACKDUCK_INFO.key)

        fetch(url, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
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
        })
        .catch((error) => {
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
                            handleValidationError(dispatch, errorHandlers, response.status, () => validatingProviderError(validationResponse.message, validationResponse.errors))
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
        dispatch(savingProvider());
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
                dispatch(savedProvider());
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

export function clearProviderFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    };
}
