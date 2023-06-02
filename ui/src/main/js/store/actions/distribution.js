import {
    DISTRIBUTION_GET_REQUEST,
    DISTRIBUTION_GET_SUCCESS,
    DISTRIBUTION_GET_FAIL,
    DISTRIBUTION_DELETE_REQUEST,
    DISTRIBUTION_DELETE_SUCCESS,
    DISTRIBUTION_DELETE_FAIL
} from 'store/actions/types';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';

function fetchDistributionRequest() {
    return {
        type: DISTRIBUTION_GET_REQUEST
    };
}

function fetchDistributionSuccess(distro) {
    return {
        type: DISTRIBUTION_GET_SUCCESS,
        data: distro
    };
}

function fetchDistributionFail(error) {
    return {
        type: DISTRIBUTION_GET_FAIL,
        error
    };
}

function deleteDistributionRequest() {
    return {
        type: DISTRIBUTION_DELETE_REQUEST
    };
}

function deleteDistributionSuccess() {
    return {
        type: DISTRIBUTION_DELETE_SUCCESS
    };
}

function deleteDistributionError(errors) {
    return {
        type: DISTRIBUTION_DELETE_FAIL,
        errors
    };
}

export function fetchDistibution(requestParams) {
    return (dispatch, getState) => {
        dispatch(fetchDistributionRequest());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchDistributionFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));

        let request;
        if (requestParams) {
            const { pageNumber, pageSize, mutatorData } = requestParams;
            request = ConfigRequestBuilder.createReadPageRequest(ConfigRequestBuilder.JOB_AUDIT_API_URL, csrfToken, pageNumber, pageSize, mutatorData);
        } else {
            request = ConfigRequestBuilder.createReadPageRequest(ConfigRequestBuilder.JOB_AUDIT_API_URL, csrfToken, 0, 10, {});
        }

        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchDistributionSuccess(responseData));
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = responseData.message.toString();
                            }
                            return fetchDistributionFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchDistributionFail(error));
        });
    };
}

export function deleteDistribution(distributions) {
    return (dispatch, getState) => {
        dispatch(deleteDistributionRequest());
        const { csrfToken } = getState().session;
        Promise.all(distributions.map((distro) => { // eslint-disable-line
            return ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, distro.jobId);
        })).catch((error) => {
            dispatch(deleteDistributionError(error));
            console.error; // eslint-disable-line
        }).then((response) => {
            if (response) {
                dispatch(deleteDistributionSuccess());
            }
        });
    };
}
