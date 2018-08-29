import {DESCRIPTORS_DISTRIBUTION_FETCH_ERROR, DESCRIPTORS_DISTRIBUTION_FETCHED, DESCRIPTORS_DISTRIBUTION_FETCHING, DESCRIPTORS_FETCH_ERROR, DESCRIPTORS_FETCHED, DESCRIPTORS_FETCHING} from './types';

import {verifyLoginByStatus} from './session';

const FETCH_DESCRIPTOR_URL = "/alert/api/descriptor"
const FETCH_DISTRIBUTION_URL = `${FETCH_DESCRIPTOR_URL}/distribution`

function fetchingDescriptors() {
    return {
        type: DESCRIPTORS_FETCHING
    };
}

function descriptorsFetched(descriptorType, descriptors) {
    if (descriptors) {
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

function fetchingDistributionDescriptor() {
    return {
        type: DESCRIPTORS_DISTRIBUTION_FETCHING
    };
}

function fetchedDistributionDescriptors(items) {
    return {
        type: DESCRIPTORS_DISTRIBUTION_FETCHED,
        currentDistributionComponents: items
    }
}

function distributionDescriptorError(message, error) {
    return {
        type: DESCRIPTORS_DISTRIBUTION_FETCH_ERROR,
        message,
        error
    };
}

export function getDescriptorByType(distributionConfigType) {
    return (dispatch) => {
        dispatch(fetchingDescriptors());
        const getUrl = `${FETCH_DESCRIPTOR_URL}?descriptorType=${distributionConfigType}`;
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
                    dispatch(descriptorsFetched(distributionConfigType, json))
                }
            });
        }).catch(dispatch(descriptorsError(console.error)));
    };
}

export function getDistributionDescriptor(provider, channel) {
    return (dispatch) => {
        dispatch(fetchingDistributionDescriptor());
        const getUrl = `${FETCH_DISTRIBUTION_URL}?providerName=${provider}&channelName=${channel}`
        fetch(getUrl, {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            response.json().then((json) => {
                if (!response.ok) {
                    dispatch(distributionDescriptorError(json.message, json.error));
                    dispatch(verifyLoginByStatus(response.status));
                } else {
                    dispatch(fetchedDistributionDescriptors(json));
                }
            });
        }).catch(dispatch(distributionDescriptorError(console.error)));
    };
}
