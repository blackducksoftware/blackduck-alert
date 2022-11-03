import { DESCRIPTORS_FETCH_ERROR, DESCRIPTORS_FETCHED, DESCRIPTORS_FETCHING } from 'store/actions/types';

import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import HeaderUtilities from 'common/util/HeaderUtilities';

const FETCH_DESCRIPTOR_URL = '/alert/api/metadata/descriptors';

function fetchingDescriptors() {
    return {
        type: DESCRIPTORS_FETCHING
    };
}

function descriptorsFetched(descriptors) {
    if (descriptors) {
        const sortedList = descriptors.sort((first, second) => {
            if (first.label < second.label) {
                return -1;
            }
            if (first.label > second.label) {
                return 1;
            }
            return 0;
        });
        return {
            type: DESCRIPTORS_FETCHED,
            items: sortedList
        };
    }
    return {
        type: DESCRIPTORS_FETCHED,
        items: []
    };
}

function descriptorsError(message) {
    return {
        type: DESCRIPTORS_FETCH_ERROR,
        message
    };
}

export function getDescriptors() {
    return (dispatch) => {
        dispatch(fetchingDescriptors());
        const getUrl = `${FETCH_DESCRIPTOR_URL}`;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(unauthorized));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        fetch(getUrl, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(descriptorsFetched(responseData.descriptors));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => descriptorsError(responseData.message)));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            }).catch((error) => {
                dispatch(descriptorsError(error));
            });
    };
}
