import {
    DESCRIPTORS_DISTRIBUTION_FETCH_ERROR,
    DESCRIPTORS_DISTRIBUTION_FETCHED,
    DESCRIPTORS_DISTRIBUTION_FETCHING,
    DESCRIPTORS_DISTRIBUTION_RESET,
    DESCRIPTORS_FETCH_ERROR,
    DESCRIPTORS_FETCHED,
    DESCRIPTORS_FETCHING
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

const FETCH_DESCRIPTOR_URL = '/alert/api/descriptor';
const FETCH_DISTRIBUTION_URL = `${FETCH_DESCRIPTOR_URL}/distribution`;

function fetchingDescriptors() {
    return {
        type: DESCRIPTORS_FETCHING
    };
}

function descriptorsFetched(descriptorType, descriptors) {
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
            items: {
                [descriptorType]: sortedList
            }
        };
    }
    return {
        type: DESCRIPTORS_FETCHED,
        items: {
            [descriptorType]: []
        }
    };
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
    };
}

function distributionDescriptorError(message, error) {
    return {
        type: DESCRIPTORS_DISTRIBUTION_FETCH_ERROR,
        message,
        error
    };
}

function distributionDescriptorReset() {
    return {
        type: DESCRIPTORS_DISTRIBUTION_RESET
    };
}

export function getDescriptorsByTypeAndContext(distributionConfigType, configContextName) {
    return (dispatch) => {
        dispatch(fetchingDescriptors());
        const getUrl = `${FETCH_DESCRIPTOR_URL}?context=${configContextName}`;
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
                    dispatch(descriptorsFetched(distributionConfigType, json));
                }
            });
        }).catch(dispatch(descriptorsError(console.error)));
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
                    dispatch(descriptorsFetched(distributionConfigType, json));
                }
            });
        }).catch(dispatch(descriptorsError(console.error)));
    };
}

export function getDistributionDescriptor(provider, channel) {
    return (dispatch) => {
        dispatch(fetchingDistributionDescriptor());
        const getUrl = `${FETCH_DISTRIBUTION_URL}?providerName=${provider}&channelName=${channel}`;
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

export function resetDistributionDescriptor() {
    return (dispatch) => {
        dispatch(distributionDescriptorReset());
    };
}
