import {
    SCHEDULING_CONFIG_FETCHING,
    SCHEDULING_CONFIG_FETCHED,
    SCHEDULING_CONFIG_UPDATE_ERROR,
    SCHEDULING_CONFIG_UPDATING,
    SCHEDULING_CONFIG_UPDATED,
    SCHEDULING_ACCUMULATOR_ERROR,
    SCHEDULING_ACCUMULATOR_RUNNING,
    SCHEDULING_ACCUMULATOR_SUCCESS
} from './types';

const CONFIG_URL = '/api/configuration/global/scheduling';
const ACCUMULATOR_URL = '/api/configuration/global/scheduling/accumulator/run';

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

/**
 * Triggers Scheduling Accumulator Running
 * @returns {{type}}
 */
function runningAccumulator() {
    return {
        type: SCHEDULING_ACCUMULATOR_RUNNING
    };
}

/**
 * Triggers Scheduling Accumulator Ran
 * @returns {{type}}
 */
function accumulatorSuccess() {
    return {
        type: SCHEDULING_ACCUMULATOR_SUCCESS
    };
}

/**
 * Triggers Scheduling Accumulator Ran
 * @returns {{type}}
 */
function accumulatorError(error) {
    return {
        type: SCHEDULING_ACCUMULATOR_ERROR,
        accumulatorError: error
    };
}

export function getSchedulingConfig() {
    return (dispatch, getState) => {
        dispatch(fetchingSchedulingConfig());
        const csrfToken = getState().session.csrfToken;
        fetch(CONFIG_URL, {
            credentials: 'include',
            headers: {
              'X-CSRF-TOKEN': csrfToken
            }
        })
            .then(response => response.json().then((body) => {
                if (body.length > 0) {
                    dispatch(schedulingConfigFetched(body[0]));
                } else {
                    dispatch(schedulingConfigFetched({}));
                }
            }))
            .catch(console.error);
    };
}

export function updateSchedulingConfig(config) {
    return (dispatch, getState) => {
        dispatch(updatingSchedulingConfig());

        const body = {
            ...config,
            id: 1
        };
        const csrfToken = getState().session.csrfToken;
        fetch(CONFIG_URL, {
            method: 'PUT',
            headers: {
                'content-type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(body)
        })
            .then((response) => {
                if (response.ok) {
                    response.json().then(() => dispatch(schedulingConfigUpdated({ ...config })));
                } else {
                    response.json()
                        .then((data) => {
                            console.log('data', data.message);
                            switch (response.status) {
                                case 400:
                                    return dispatch(schedulingConfigError(data.message, data.errors));
                                case 412:
                                    return dispatch(schedulingConfigError(data.message, data.errors));
                                default:
                                    return dispatch(schedulingConfigError(data.message, null));
                            }
                        });
                }
            })
            .then(() => {
                dispatch(getSchedulingConfig());
            })
            .catch(console.error);
    };
}

export function runSchedulingAccumulator() {
    return (dispatch, getState) => {
        dispatch(runningAccumulator());
        const csrfToken = getState().session.csrfToken;
        fetch(ACCUMULATOR_URL, {
            credentials: 'include',
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
            .then((response) => {
                if (!response.ok) {
                    response.json().then((json) => {
                        dispatch(accumulatorError(json.message));
                    });
                } else {
                    dispatch(accumulatorSuccess());
                }
            })
            .then(() => {
                dispatch(getSchedulingConfig());
            })
            .catch((err) => {
                dispatch(accumulatorError(err));
            });
    };
}
