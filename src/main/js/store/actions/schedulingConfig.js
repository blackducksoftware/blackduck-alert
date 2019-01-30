import {
    SCHEDULING_ACCUMULATOR_ERROR,
    SCHEDULING_ACCUMULATOR_RUNNING,
    SCHEDULING_ACCUMULATOR_SUCCESS,
    SCHEDULING_CONFIG_FETCH_ERROR,
    SCHEDULING_CONFIG_FETCHED,
    SCHEDULING_CONFIG_FETCHING,
    SCHEDULING_CONFIG_UPDATE_ERROR,
    SCHEDULING_CONFIG_UPDATED,
    SCHEDULING_CONFIG_UPDATING
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';

import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtil from 'util/fieldModelUtilities';

/**
 * Triggers Scheduling Config Fetching reducer
 * @returns {{type}}
 */
function fetchingSchedulingConfig() {
    return {
        type: SCHEDULING_CONFIG_FETCHING
    };
}

/**
 * Triggers Scheduling Config Fetched Reducer
 * @returns {{type}}
 */
function schedulingConfigFetched(config) {
    return {
        type: SCHEDULING_CONFIG_FETCHED,
        config
    };
}

function schedulingConfigFetchError(message) {
    return {
        type: SCHEDULING_CONFIG_FETCH_ERROR,
        error: {
            message
        }
    };
}

/**
 * Triggers Scheduling Config Updating reducer
 * @returns {{type}}
 */
function updatingSchedulingConfig() {
    return {
        type: SCHEDULING_CONFIG_UPDATING
    };
}

/**
 * Triggers Scheduling Config Updated Reducer
 * @returns {{type}}
 */
function schedulingConfigUpdated(config) {
    return {
        type: SCHEDULING_CONFIG_UPDATED,
        config
    };
}

/**
 * Triggers Scheduling Config Error
 * @returns {{type}}
 */
function schedulingConfigError(message, errors) {
    return {
        type: SCHEDULING_CONFIG_UPDATE_ERROR,
        message,
        errors
    };
}

export function getSchedulingConfig() {
    return (dispatch, getState) => {
        dispatch(fetchingSchedulingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, 'component_scheduling');
        request.then((response) => {
            if (response.ok) {
                response.json().then((body) => {
                    if (body.length > 0) {
                        dispatch(schedulingConfigFetched(body[0]));
                    } else {
                        dispatch(schedulingConfigFetched({}));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        })
            .catch(dispatch(schedulingConfigFetchError(console.error)));
    };
}

export function updateSchedulingConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingSchedulingConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createUpdateRequest(csrfToken, 1, config);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    const updatedConfig = FieldModelUtil.updateFieldModelSingleValue(config, 'id', data.id);
                    dispatch(schedulingConfigUpdated(updatedConfig));
                }).then(() => dispatch(getSchedulingConfig()));
            } else {
                response.json()
                    .then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(schedulingConfigError(data.message, data.errors));
                            case 412:
                                return dispatch(schedulingConfigError(data.message, data.errors));
                            default: {
                                dispatch(schedulingConfigError(data.message, null));
                                return dispatch(verifyLoginByStatus(response.status));
                            }
                        }
                    });
            }
        }).then(() => {
            dispatch(getSchedulingConfig());
        }).catch(console.error);
    };
}
