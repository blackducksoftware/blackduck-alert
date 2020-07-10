import {
    DISTRIBUTION_JOB_CHECK_DESCRIPTOR,
    DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE,
    DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS,
    DISTRIBUTION_JOB_FETCH_ERROR,
    DISTRIBUTION_JOB_FETCHED,
    DISTRIBUTION_JOB_FETCHING,
    DISTRIBUTION_JOB_SAVE_ERROR,
    DISTRIBUTION_JOB_SAVED,
    DISTRIBUTION_JOB_SAVING,
    DISTRIBUTION_JOB_TEST_FAILURE,
    DISTRIBUTION_JOB_TEST_SUCCESS,
    DISTRIBUTION_JOB_TESTING,
    DISTRIBUTION_JOB_UPDATE_ERROR,
    DISTRIBUTION_JOB_UPDATED,
    DISTRIBUTION_JOB_UPDATING,
    SERIALIZE
} from 'store/actions/types';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const initialState = {
    fetching: false,
    saving: false,
    inProgress: false,
    success: false,
    testingConfig: false,
    sendingCustomMessage: false,
    job: {},
    error: HTTPErrorUtils.createEmptyErrorObject(),
    configurationMessage: ''
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_JOB_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                saving: false,
                inProgress: true,
                success: false,
                testingConfig: false,
                configurationMessage: '',
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case DISTRIBUTION_JOB_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.configurationMessage,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                job: action.job
            });

        case DISTRIBUTION_JOB_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action)
            });

        case DISTRIBUTION_JOB_UPDATING:
        case DISTRIBUTION_JOB_SAVING:
            return Object.assign({}, state, {
                fetching: false,
                saving: true,
                inProgress: true,
                success: false,
                testingConfig: false,
                configurationMessage: 'Saving...',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                job: action.job
            });

        case DISTRIBUTION_JOB_UPDATED:
        case DISTRIBUTION_JOB_SAVED:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: false,
                success: true,
                testingConfig: false,
                configurationMessage: action.configurationMessage,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                ...action
            });

        case DISTRIBUTION_JOB_UPDATE_ERROR:
        case DISTRIBUTION_JOB_SAVE_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action)
            });

        case DISTRIBUTION_JOB_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: true,
                success: false,
                testingConfig: true,
                configurationMessage: 'Testing...',
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case DISTRIBUTION_JOB_TEST_SUCCESS:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: false,
                success: true,
                testingConfig: true,
                configurationMessage: action.configurationMessage,
                error: HTTPErrorUtils.createEmptyErrorObject()
            });

        case DISTRIBUTION_JOB_TEST_FAILURE:
            return Object.assign({}, state, {
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: true,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action)
            });
        case DISTRIBUTION_JOB_CHECK_DESCRIPTOR:
            return Object.assign({}, state, {
                inProgress: true
            });
        case DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS:
            return Object.assign({}, state, {
                inProgress: false,
                error: HTTPErrorUtils.combineErrorObjects(state.error, HTTPErrorUtils.createErrorObject(action))
            });
        case DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE:
            return Object.assign({}, state, {
                inProgress: false,
                error: HTTPErrorUtils.combineErrorObjects(state.error, HTTPErrorUtils.createErrorObject(action))
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
