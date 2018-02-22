import {
    SCHEDULING_CONFIG_FETCHING,
    SCHEDULING_CONFIG_FETCHED,
    SCHEDULING_CONFIG_UPDATING,
    SCHEDULING_CONFIG_UPDATED,
    SCHEDULING_ACCUMULATOR_ERROR,
    SCHEDULING_ACCUMULATOR_RUNNING,
    SCHEDULING_ACCUMULATOR_SUCCESS
} from './types';

const CONFIG_URL = '/api/configuration/global/scheduling';
const ACCUMULATOR_URL = '/api/configuration/global/scheduling/accumulator/run';

/**
 * Triggers Email Config Fetching reducer
 * @returns {{type}}
 */
function fetchingSchedulingConfig() {
    return {
        type: SCHEDULING_CONFIG_FETCHING
    };
}

/**
 * Triggers Email Config Fetched Reducer
 * @returns {{type}}
 */
function schedulingConfigFetched(config) {
    return {
        type: SCHEDULING_CONFIG_FETCHED
    };
}

/**
 * Triggers Email Config Fetching reducer
 * @returns {{type}}
 */
function updatingSchedulingConfig() {
    return {
        type: SCHEDULING_CONFIG_UPDATING
    };
}

/**
 * Triggers Email Config Fetched Reducer
 * @returns {{type}}
 */
function schedulinglConfigUpdated(config) {
    return {
        type: SCHEDULING_CONFIG_UPDATED,
        config
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
function accumulatorError(message) {
    return {
        type: SCHEDULING_ACCUMULATOR_ERROR,
        message
    };
}

export function getSchedulingConfig() {
    return (dispatch) => {
        dispatch(fetchingEmailConfig());

        fetch(CONFIG_URL, {
            credentials: 'include'
        })
        .then((response) => response.json())
        .then((body) => { dispatch(emailConfigFetched(body[0])); console.log('body', body) })
        .catch(function(error) {
            console.error(error);
        });
    }
};

export function updateSchedulingConfig(config) {
    return (dispatch) => {
        dispatch(updatingEmailConfig());

        fetch(CONFIG_URL, {
            method: 'POST',
            credentials: 'include'
        })
            .then((response) => response.json())
            .then((body) => { dispatch(emailConfigUpdated(body[0])); console.log('body', body) })
            .catch(function(error) {
                console.error(error);
            });
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
        .catch(err => {
            dispatch(accumulatorError(err));
        });
    }
}

