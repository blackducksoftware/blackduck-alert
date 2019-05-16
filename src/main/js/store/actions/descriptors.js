import { DESCRIPTORS_FETCH_ERROR, DESCRIPTORS_FETCHED, DESCRIPTORS_FETCHING } from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

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
            } else if (first.label > second.label) {
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
        fetch(getUrl, {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            response.json().then((json) => {
                if (!response.ok) {
                    dispatch(descriptorsError(json.message));
                    dispatch(verifyLoginByStatus(response.status));
                } else {
                    dispatch(descriptorsFetched(json));
                }
            });
        }).catch(dispatch(descriptorsError(console.error)));
    };
}
