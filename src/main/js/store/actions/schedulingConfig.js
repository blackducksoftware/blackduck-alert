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
function accumulatorError(accumulatorError) {
    return {
        type: SCHEDULING_ACCUMULATOR_ERROR,
        accumulatorError
    };
}

export function getSchedulingConfig() {
    return (dispatch) => {
        dispatch(fetchingSchedulingConfig());

        fetch(CONFIG_URL, {
            credentials: 'same-origin'
        })
        .then((response) => response.json())
        .then((body) => { dispatch(schedulingConfigFetched(body[0])); console.log('body', body) })
        .catch(console.error);
    }
};

export function updateSchedulingConfig(config) {
    return (dispatch) => {

        dispatch(updatingSchedulingConfig());

        const body = {
            ...config,
            id: 1
        };

        fetch(CONFIG_URL, {
            method: 'PUT',
            headers: {
                'content-type': 'application/json'
            },
            credentials: 'same-origin',
            body: JSON.stringify(body)
        })
            .then((response) => {
                if(response.ok) {
                    response.json().then((body) => dispatch(schedulingConfigUpdated({ ...config })));
                } else {
                    response.json()
                        .then((data) => {
                            console.log('data', data.message);
                            switch(response.status) {
                                case 400:
                                    return dispatch(schedulingConfigError(data.message, data.errors));
                                case 412:
                                    return dispatch(schedulingConfigError(data.message, data.errors));
                                default:
                                    dispatch(schedulingConfigError(data.message, null));
                            }
                        });
                }
            })

        .catch(console.error);
    }
};

export function runSchedulingAccumulator() {
    return (dispatch) => {
        dispatch(runningAccumulator());

        fetch(ACCUMULATOR_URL,{
            credentials: 'include',
            method: 'POST',
        })
        .then((response) => {
            if (!response.ok) {
                response.json().then(json => {
                    dispatch(accumulatorError(json.message));
                });
            } else {
                dispatch(accumulatorSuccess());
            }
        })
        .then(() => {
            getSchedulingConfig()(dispatch);
        })
        .catch(err => {
            dispatch(accumulatorError(err));
        });
    }
}

