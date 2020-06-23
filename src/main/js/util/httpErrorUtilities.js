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

        return () => {
        };
    };
};
