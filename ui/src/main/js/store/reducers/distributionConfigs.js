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
    DISTRIBUTION_JOB_VALIDATE_ERROR,
    DISTRIBUTION_JOB_VALIDATED,
    DISTRIBUTION_JOB_VALIDATING,
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
    configurationMessage: '',
    status: ''
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_JOB_FETCHING:
            return {
                ...state,
                fetching: true,
                saving: false,
                inProgress: true,
                success: false,
                testingConfig: false,
                configurationMessage: '',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                status: ''
            };

        case DISTRIBUTION_JOB_FETCHED:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.configurationMessage,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                job: action.job,
                status: ''
            };

        case DISTRIBUTION_JOB_FETCH_ERROR:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action),
                status: ''
            };

        case DISTRIBUTION_JOB_UPDATING:
        case DISTRIBUTION_JOB_SAVING:
            return {
                ...state,
                fetching: false,
                saving: true,
                inProgress: true,
                success: false,
                testingConfig: false,
                configurationMessage: 'Saving...',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                job: action.job,
                status: 'SAVING'
            };

        case DISTRIBUTION_JOB_UPDATED:
        case DISTRIBUTION_JOB_SAVED:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: false,
                success: true,
                testingConfig: false,
                configurationMessage: action.configurationMessage,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                status: 'SAVED',
                ...action
            };

        case DISTRIBUTION_JOB_UPDATE_ERROR:
        case DISTRIBUTION_JOB_SAVE_ERROR:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action),
                status: 'ERROR'
            };

        case DISTRIBUTION_JOB_TESTING:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: true,
                success: false,
                testingConfig: true,
                configurationMessage: 'Testing...',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                status: 'TESTING'
            };

        case DISTRIBUTION_JOB_TEST_SUCCESS:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: false,
                success: true,
                testingConfig: true,
                configurationMessage: action.configurationMessage,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                status: 'TESTED'
            };

        case DISTRIBUTION_JOB_TEST_FAILURE:
            return {
                ...state,
                fetching: false,
                saving: false,
                inProgress: false,
                success: false,
                testingConfig: true,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action),
                status: 'ERROR'
            };
        case DISTRIBUTION_JOB_CHECK_DESCRIPTOR:
            return { ...state, inProgress: true };
        case DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS:
            return {
                ...state,
                inProgress: false,
                error: HTTPErrorUtils.combineErrorObjects(state.error, HTTPErrorUtils.createErrorObject(action))
            };
        case DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE:
            return {
                ...state,
                inProgress: false,
                error: HTTPErrorUtils.combineErrorObjects(state.error, HTTPErrorUtils.createErrorObject(action))
            };
        case DISTRIBUTION_JOB_VALIDATING:
            return {
                ...state,
                inProgress: true,
                status: 'VALIDATING'
            };
        case DISTRIBUTION_JOB_VALIDATED:
            return {
                ...state,
                inProgress: false,
                status: 'VALIDATED'
            };
        case DISTRIBUTION_JOB_VALIDATE_ERROR:
            return {
                ...state,
                inProgress: false,
                configurationMessage: action.message,
                error: HTTPErrorUtils.createErrorObject(action),
                status: 'ERROR'
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
