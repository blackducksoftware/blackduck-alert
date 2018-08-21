import {
    DISTRIBUTION_JOB_FETCHING,
    DISTRIBUTION_JOB_FETCHED,
    DISTRIBUTION_JOB_FETCH_ERROR,
    DISTRIBUTION_JOB_SAVING,
    DISTRIBUTION_JOB_SAVED,
    DISTRIBUTION_JOB_SAVE_ERROR,
    DISTRIBUTION_JOB_UPDATING,
    DISTRIBUTION_JOB_UPDATED,
    DISTRIBUTION_JOB_UPDATE_ERROR,
    DISTRIBUTION_JOB_TESTING,
    DISTRIBUTION_JOB_TEST_SUCCESS,
    DISTRIBUTION_JOB_TEST_FAILURE,
    SERIALIZE

} from '../actions/types';

const initialState = {
    fetching: false,
    inProgress: false,
    success: false,
    error: {
        message: ''
    },
    configurationMessage: ''
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_JOB_FETCHING:
            return Object.assign({}, state, {
                fetching: true,
                inProgress: true,
                success: false,
                configurationMessage: '',
                error: {
                    message: ''
                }
            });

        case DISTRIBUTION_JOB_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                configurationMessage: action.configurationMessage,
                error: {
                    message: ''
                },
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case DISTRIBUTION_JOB_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: true,
                inProgress: true,
                success: false,
                configurationMessage: action.configurationMessage,
                error: {
                    ...action.errors,
                    message: action.configurationMessage
                }
            });

        case DISTRIBUTION_JOB_UPDATING:
        case DISTRIBUTION_JOB_SAVING:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: true,
                success: false,
                configurationMessage: '',
                error: {
                    message: ''
                },
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case DISTRIBUTION_JOB_UPDATED:
        case DISTRIBUTION_JOB_SAVED:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: true,
                configurationMessage: action.configurationMessage,
                error: {
                    message: ''
                },
                ...action
            });

        case DISTRIBUTION_JOB_UPDATE_ERROR:
        case DISTRIBUTION_JOB_SAVE_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                configurationMessage: action.configurationMessage,
                error: {
                    ...action.errors,
                    message: action.configurationMessage
                }
            });

        case DISTRIBUTION_JOB_TESTING:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: true,
                success: false,
                configurationMessage: action.configurationMessage,
                error: {
                    message: ''
                }
            });

        case DISTRIBUTION_JOB_TEST_SUCCESS:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: true,
                configurationMessage: action.configurationMessage,
                error: {
                    message: ''
                }
            });

        case DISTRIBUTION_JOB_TEST_FAILURE:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                configurationMessage: action.configurationMessage,
                error: {
                    ...action.errors,
                    message: action.configurationMessage
                }
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;

    }
}

export default config;
