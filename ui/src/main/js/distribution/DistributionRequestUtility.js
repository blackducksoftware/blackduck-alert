import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from '../util/httpErrorUtilities';
import * as ConfigRequestBuilder from '../util/configurationRequestBuilder';

const createErrorHandler = (type, defaultHandler) => {
    const errorHandlers = [];
    errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    // errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobError({ type, message: HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION })));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createPreconditionFailedHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    return HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
};

export const getDataById = async (id, csrfToken) => {
    if (id) {
        const response = await ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id);
        const retrievedModel = await response.json();
        return retrievedModel;
    }

    return null;
};
