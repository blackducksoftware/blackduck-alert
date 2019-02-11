import {
    EMAIL_CONFIG_FETCHED,
    EMAIL_CONFIG_FETCHING,
    EMAIL_CONFIG_HIDE_TEST_MODAL,
    EMAIL_CONFIG_SHOW_TEST_MODAL,
    EMAIL_CONFIG_TEST_SUCCESSFUL,
    EMAIL_CONFIG_UPDATE_ERROR,
    EMAIL_CONFIG_UPDATED,
    EMAIL_CONFIG_UPDATING
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';


/**
 * Triggers Email Config Fetching reducer
 * @returns {{type}}
 */
function fetchingEmailConfig() {
    return {
        type: EMAIL_CONFIG_FETCHING
    };
}

/**
 * Triggers Email Config Fetched Reducer
 * @returns {{type}}
 */
function emailConfigFetched(config) {
    return {
        type: EMAIL_CONFIG_FETCHED,
        config
    };
}

/**
 * Triggers Config Error
 * @returns {{type}}
 */
function configError(message, errors) {
    return {
        type: EMAIL_CONFIG_UPDATE_ERROR,
        message,
        errors
    };
}

/**
 * Triggers Email Config Fetching reducer
 * @returns {{type}}
 */
function updatingEmailConfig() {
    return {
        type: EMAIL_CONFIG_UPDATING
    };
}

/**
 * Triggers Email Config Fetched Reducer
 * @returns {{type}}
 */
function emailConfigUpdated(config, message) {
    return {
        type: EMAIL_CONFIG_UPDATED,
        message,
        config
    };
}

function handleFailureResponse(dispatch, response) {
    response.json()
        .then((data) => {
            switch (response.status) {
                case 400:
                    return dispatch(configError(data.message, data.errors));
                case 412:
                    return dispatch(configError(data.message, data.errors));
                default: {
                    dispatch(configError(data.message, null));
                    return dispatch(verifyLoginByStatus(response.status));
                }
            }
        });
}

export function openEmailConfigTest() {
    return { type: EMAIL_CONFIG_SHOW_TEST_MODAL };
}

export function closeEmailConfigTest() {
    return { type: EMAIL_CONFIG_HIDE_TEST_MODAL };
}

export function emailConfigTestSucceeded() {
    return {
        type: EMAIL_CONFIG_TEST_SUCCESSFUL
    };
}

export function getEmailConfig() {
    return (dispatch, getState) => {
        dispatch(fetchingEmailConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL);
        request.then((response) => {
            if (response.ok) {
                response.json().then((body) => {
                    if (body.length > 0) {
                        dispatch(emailConfigFetched(body[0]));
                    } else {
                        dispatch(emailConfigFetched({}));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    };
}

export function updateEmailConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingEmailConfig());
        const { csrfToken } = getState().session;
        let request;
        if (config.id) {
            request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config.id, config);
        } else {
            request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config);
        }
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const updatedConfig = FieldModelUtilities.updateFieldModelSingleValue(config, 'id', data.id);
                    dispatch(emailConfigUpdated(updatedConfig, data.message));
                });
            } else {
                handleFailureResponse(dispatch, response);
            }
        }).catch(console.error);
    };
}

export function sendEmailConfigTest(config, destination) {
    return (dispatch, getState) => {
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, config, destination);
        request.then((response) => {
            dispatch(closeEmailConfigTest());
            if (response.ok) {
                dispatch(emailConfigTestSucceeded());
            } else {
                handleFailureResponse(dispatch, response);
            }
        }).catch(console.error);
    };
}
