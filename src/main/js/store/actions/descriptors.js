
import {
    DESCRIPTORS_FETCHING,
    DESCRIPTORS_FETCHED,
    DESCRIPTORS_FETCH_ERROR
} from './types';

import {verifyLoginByStatus} from './session';

const FETCH_URL = "/alert/api/descriptor"

function fetchingDescriptors() {
    return {
        type: DESCRIPTORS_FETCHING
    };
}

function descriptorsFetched(descriptorType, descriptors) {
    if(descriptors) {
        const sortedList = descriptors.sort((first, second) => first.label > second.label);
        return {
            type: DESCRIPTORS_FETCHED,
            items: {
                [descriptorType]: sortedList
            }
        };
    } else {
        return {
            type: DESCRIPTORS_FETCHED,
            items: {
                [descriptorType]: []
            }
        };
    }
}

function descriptorsError(message) {
    return {
        type: DESCRIPTORS_FETCH_ERROR,
        message
    };
}

export function getDescriptorByType(distributionConfigType) {
    return (dispatch, getState) => {
        dispatch(fetchingDescriptors());
        const getUrl = `${FETCH_URL}?descriptorType=${distributionConfigType}`;
        fetch(getUrl, {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            response.json().then((json) => {
                if(!response.ok) {
                    dispatch(descriptorsError(json.message));
                    dispatch(verifyLoginByStatus(response.status));
                } else {
                    dispatch(descriptorsFetched(distributionConfigType, json))
                }
            });
        }).catch(dispatch(descriptorsError(console.error)));
    }
}
