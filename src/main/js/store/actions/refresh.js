import {REFRESH_DISABLE, REFRESH_ENABLE} from './types';

/**
 * Triggers Auto Refresh
 * @returns {{type}}
 */
function refreshEnable() {
    return {
        type: REFRESH_ENABLE
    };
}

/**
 * Triggers Auto Refresh to stop
 * @returns {{type}}
 */
function refreshDisable() {
    return {
        type: REFRESH_DISABLE
    };
}


export function updateRefresh(boolean) {
    return (dispatch) => {
        if (boolean) {
            dispatch(refreshEnable());
        } else {
            dispatch(refreshDisable());
        }
    };
}
