import {
    SYSTEM_DIAGNOSTICS_GET_FAIL,
    SYSTEM_DIAGNOSTICS_GET_REQUEST,
    SYSTEM_DIAGNOSTICS_GET_SUCCESS,
} from 'store/actions/types';
import { SYSTEM_DIAGNOSTICS_URL } from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { unauthorized } from 'store/actions/session';
import HeaderUtilities from 'common/util/HeaderUtilities';
import { createRequestUrl } from 'common/util/RequestUtilities';

function fetchingSystemDiagnostics() {
    return {
        type: SYSTEM_DIAGNOSTICS_GET_REQUEST
    };
}

function fetchingSystemDiagnosticsFail() {
    return {
        type: SYSTEM_DIAGNOSTICS_GET_FAIL
    };
}

function fetchingSystemDiagnosticsSuccess(diagnostics) {
    return {
        type: SYSTEM_DIAGNOSTICS_GET_SUCCESS,
        data: diagnostics
    };
}

export function fetchSystemDiagnostics() {
    return (dispatch, getState) => {
        dispatch(fetchingSystemDiagnostics());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingSystemDiagnosticsFail(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);
        const url = createRequestUrl(SYSTEM_DIAGNOSTICS_URL);

        return fetch(url, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        }).then((response) => {
            return response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(fetchingSystemDiagnosticsSuccess(responseData));
                        return responseData;
                    } else {
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                            let message = '';
                            if (responseData && responseData.message) {
                                message = responseData.message.toString();
                            }
                            return fetchingSystemDiagnosticsFail(message);
                        }));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                        throw new Error('Failed to fetch diagnostics');
                    }
                });
        }).catch((error) => {
            console.log(error);
            dispatch(fetchingSystemDiagnosticsFail(error));
            throw error;
        });
    };
}
