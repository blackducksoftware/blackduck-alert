import {
    GET_GITHUB_FETCHING,
    GET_GITHUB_SUCCESS,
    GET_GITHUB_ERROR,
    ADD_GITHUB_USER_REQUEST,
    ADD_GITHUB_USER_SUCCESS,
    ADD_GITHUB_USER_FAIL,
    VALIDATE_GITHUB_CONFIGURATION_REQUEST,
    VALIDATE_GITHUB_CONFIGURATION_SUCCESS,
    VALIDATE_GITHUB_CONFIGURATION_FAIL,
    VALIDATE_GITHUB_CONFIGURATION_ERROR
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import HeaderUtilities from 'common/util/HeaderUtilities';


function fetchingGithubList() {
    return {
        type: GET_GITHUB_FETCHING
    };
}

function fetchingGithubListSuccess(github) {
    return {
        type: GET_GITHUB_SUCCESS,
        data: github
    };
}

function fetchingGithubListError(message) {
    return {
        type: GET_GITHUB_ERROR,
        message
    };
}

function validatingGitHubConfiguration() {
    return {
        type: VALIDATE_GITHUB_CONFIGURATION_REQUEST
    };
}

function validatedGitHubConfiguration() {
    return {
        type: VALIDATE_GITHUB_CONFIGURATION_SUCCESS
    };
}

function validateGitHubConfigurationFail(message, errors) {
    return {
        type: VALIDATE_GITHUB_CONFIGURATION_FAIL,
        message,
        errors
    };
}

function validateGitHubConfigurationErrorMessage(message) {
    return {
        type: VALIDATE_GITHUB_CONFIGURATION_ERROR,
        message
    };
}

function postGithubConfigurationRequest() {
    return {
        type: ADD_GITHUB_USER_REQUEST
    };
}

function postGithubConfigurationSuccess() {
    return {
        type: ADD_GITHUB_USER_SUCCESS
    };
}

function postGithubConfigurationError(message) {
    return {
        type: ADD_GITHUB_USER_FAIL,
        configError: message,
        message,
        errors: {}
    };
}

function handleValidationError(dispatch, errorHandlers, responseStatus, defaultHandler) {
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
    dispatch(handler(responseStatus));
}

function createErrorHandler(defaultHandler) {
    const errorHandlers = [];
    errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => postGithubConfigurationError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    return HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
}

export function fetchGithub() {
    return (dispatch, getState) => {
        dispatch(fetchingGithubList());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingGithubListError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        fetch(ConfigRequestBuilder.GITHUB_API_URL, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(fetchingGithubListSuccess(responseData));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                                let message = '';
                                if (responseData && responseData.message) {
                                    // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                    message = responseData.message.toString();
                                }
                                return fetchingGithubListError(message);
                            }));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch((error) => {
                console.log(error);
                dispatch(fetchingGithubListError(error));
            });
    };
}

export function validateGitHubConfiguration(githubConfig) {
    return (dispatch, getState) => {
        dispatch(validatingGitHubConfiguration());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => validateGitHubConfigurationErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));

        const validateRequest = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.GITHUB_API_URL, csrfToken, githubConfig);
        validateRequest.then((response) => {
            if (response.ok) {
                response.json()
                    .then((validationResponse) => {
                        // FIXME figure out the best way to handle warning statuses
                        if (!Object.keys(validationResponse.errors).length) {
                            dispatch(validatedGitHubConfiguration());
                        } else {
                            handleValidationError(dispatch, errorHandlers, response.status, () => validateGitHubConfigurationFail(validationResponse.message, validationResponse.errors));
                        }
                    });
            } else {
                handleValidationError(dispatch, errorHandlers, response.status, () => validateGitHubConfigurationFail(response.message, HTTPErrorUtils.createEmptyErrorObject()));
            }
        })
            .catch(console.error);
    };
}

export function postGithubConfiguration(config) {
    return (dispatch, getState) => {
        console.log('config', config);

        dispatch(postGithubConfigurationRequest());
        const { csrfToken } = getState().session;
        const { id } = config;
        let request;
        if (id) {
            request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.GITHUB_API_URL, csrfToken, id, config);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.GITHUB_API_URL, csrfToken, config);
        }
        console.log(request);
        request.then((response) => {
            console.log(response);
            if (response.ok) {
                dispatch(postGithubConfigurationSuccess());
                dispatch(fetchingGithubList());
            } else {
                response.json()
                    .then((responseData) => {
                        const handler = createErrorHandler(() => postGithubConfigurationError(responseData.message));
                        dispatch(handler(response.status));
                    });
            }
        }).catch(console.error);
    };
}
