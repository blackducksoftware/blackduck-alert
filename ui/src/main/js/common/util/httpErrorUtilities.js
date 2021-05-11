export const MESSAGES = {
    FORBIDDEN_ACTION: 'You are not permitted to perform this action.',
    FORBIDDEN_READ: 'You are not permitted to view this information.'
};

export function createFieldError(message) {
    return {
        severity: 'ERROR',
        fieldMessage: message
    };
}

export function createFieldWarning(message) {
    return {
        severity: 'WARNING',
        fieldMessage: message
    };
}

export function createEmptyErrorObject() {
    return {
        message: '',
        isDetailed: false,
        fieldErrors: {}
    };
}

export function createErrorWithMessageOnly(message) {
    return {
        message,
        isDetailed: false,
        fieldErrors: {}
    };
}

export function createErrorObject(errorResponse) {
    if (!errorResponse) {
        return createEmptyErrorObject();
    }
    const responseMessage = errorResponse.message;
    const { isDetailed } = errorResponse;
    const responseErrors = errorResponse.errors;
    const message = responseMessage || '';
    const fieldErrors = responseErrors || {};
    return {
        fieldErrors,
        isDetailed,
        message
    };
}

export function combineErrorObjects(errorObject1, errorObject2) {
    const message = errorObject2.message || errorObject1.message;
    let fieldErrors = {};
    if (errorObject1.fieldErrors) {
        fieldErrors = {
            ...errorObject1.fieldErrors
        };
    }

    if (errorObject2.fieldErrors) {
        fieldErrors = {
            ...fieldErrors,
            ...errorObject2.fieldErrors
        };
    }

    return {
        message,
        isDetailed: false,
        fieldErrors
    };
}

export function containsErrors(responseObject) {
    if (!responseObject) {
        return false;
    }
    const { hasErrors, errors } = responseObject;

    if (hasErrors) {
        const keys = Object.keys(errors);
        // Test that the error object isn't empty and contains at least one error
        if (keys && keys.length > 0) {
            return keys.some((key) => errors[key].severity === 'ERROR');
        }
    }
    return hasErrors;
}

export function createStatusCodeHandler(statusCode, callback) {
    return {
        statusCode,
        callback
    };
}

export function createDefaultHandler(callback) {
    return createStatusCodeHandler(null, callback);
}

export function createBadRequestHandler(callback) {
    return createStatusCodeHandler(400, callback);
}

export function createUnauthorizedHandler(callback) {
    return createStatusCodeHandler(401, callback);
}

export function createForbiddenHandler(callback) {
    return createStatusCodeHandler(403, callback);
}

export function createNotFoundHandler(callback) {
    return createStatusCodeHandler(404, callback);
}

export function createPreconditionFailedHandler(callback) {
    return createStatusCodeHandler(412, callback);
}

export function createHttpErrorHandler(statusHandlers) {
    return (statusCode) => {
        const handler = statusHandlers && statusHandlers.find((statusHandler) => statusHandler.statusCode === statusCode);
        if (handler) {
            return handler.callback();
        }
        const defaultHandler = statusHandlers && statusHandlers.find((statusHandler) => !statusHandler.statusCode);

        if (defaultHandler) {
            return defaultHandler.callback();
        }
        const empty = () => {
        };
        return empty();
    };
}

export function isStatusWithinRange(statusCode, lowerBound, upperBound) {
    return statusCode >= lowerBound && statusCode <= upperBound;
}

export function isOk(statusCode) {
    return isStatusWithinRange(statusCode, 200, 299);
}

export function isRedirect(statusCode) {
    return isStatusWithinRange(statusCode, 300, 399);
}

export function isClientError(statusCode) {
    return isStatusWithinRange(statusCode, 400, 499);
}

export function isServerError(statusCode) {
    return isStatusWithinRange(statusCode, 500, 599);
}

export function isError(statusCode) {
    return isClientError(statusCode) || isServerError(statusCode);
}

export const createErrorHandler = (unauthorized) => ({
    handle: (response, deserializedResponseBody, readOperation = true) => {
        let errorObject;
        const { status } = response;
        if (isOk(status)) {
            errorObject = createEmptyErrorObject();
        }

        switch (status) {
            case 401: {
                unauthorized();
                break;
            }
            case 403: {
                if (readOperation) {
                    errorObject = createErrorWithMessageOnly(MESSAGES.FORBIDDEN_READ);
                } else {
                    // TODO Determine if this error message sufficient for all cases going forward...
                    errorObject = createErrorWithMessageOnly(MESSAGES.FORBIDDEN_ACTION);
                }
                break;
            }
            default: {
                errorObject = createErrorObject(deserializedResponseBody);
            }
        }

        return errorObject;
    }
});
