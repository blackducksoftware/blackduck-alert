import {
    CONFIG_ALL_FETCHED,
    CONFIG_CLEAR_FIELD_ERRORS,
    CONFIG_DELETED,
    CONFIG_DELETING,
    CONFIG_FETCH_ERROR,
    CONFIG_FETCHED,
    CONFIG_FETCHING,
    CONFIG_REFRESH,
    CONFIG_REFRESH_ERROR,
    CONFIG_REFRESHING,
    CONFIG_TEST_FAILED,
    CONFIG_TEST_SUCCESS,
    CONFIG_TESTING,
    CONFIG_UPDATE_ERROR,
    CONFIG_UPDATED,
    CONFIG_UPDATING,
    SERIALIZE
} from 'store/actions/types';

import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const initialState = {
    fetching: false,
    updateStatus: null,
    testing: false,
    actionMessage: null,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    config: {},
    allConfigs: []
};

const globalConfiguration = (state = initialState, action) => {
    switch (action.type) {
        case CONFIG_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: null,
                testing: false,
                actionMessage: null
            });
        case CONFIG_ALL_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                allConfigs: action.config,
                error: HTTPErrorUtils.createEmptyErrorObject()
            });
        case CONFIG_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'FETCHED',
                testing: false,
                actionMessage: null,
                config: action.config,
                error: HTTPErrorUtils.createEmptyErrorObject()
            });
        case CONFIG_FETCH_ERROR:
        case CONFIG_REFRESH_ERROR:
            return Object.assign({}, state, {
                error: HTTPErrorUtils.createErrorObject(action)
            });
        case CONFIG_REFRESHING:
            return Object.assign({}, state, {
                fetching: true,
                updateStatus: null,
                testing: false
            });

        case CONFIG_REFRESH:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'FETCHED',
                testing: false,
                config: action.config,
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case CONFIG_UPDATE_ERROR:
            return Object.assign({}, state, {
                updateStatus: 'ERROR',
                actionMessage: null,
                error: HTTPErrorUtils.createErrorObject(action)
            });
        case CONFIG_UPDATING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATING',
                testing: false,
                actionMessage: null,
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case CONFIG_UPDATED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: 'UPDATED',
                testing: false,
                actionMessage: 'Update successful',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                config: action.config
            });

        case CONFIG_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: true,
                actionMessage: null
            });

        case CONFIG_TEST_SUCCESS:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                actionMessage: 'Test successful',
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case CONFIG_TEST_FAILED:
            return Object.assign({}, state, {
                fetching: false,
                updateStatus: null,
                testing: false,
                actionMessage: null,
                error: HTTPErrorUtils.createErrorObject(action)
            });

        case CONFIG_DELETED:
            return Object.assign({}, state, {
                updateStatus: 'DELETED',
                actionMessage: 'Delete successful',
                config: {},
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case CONFIG_DELETING:
            return Object.assign({}, state, {
                actionMessage: null,
                updateStatus: 'DELETING',
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case CONFIG_CLEAR_FIELD_ERRORS:
            return Object.assign({}, state, {
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default globalConfiguration;
