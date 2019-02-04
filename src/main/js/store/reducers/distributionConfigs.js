import { DISTRIBUTION_JOB_DELETE_ERROR, DISTRIBUTION_JOB_DELETED, DISTRIBUTION_JOB_DELETING, DISTRIBUTION_JOB_FETCH_ERROR_ALL, DISTRIBUTION_JOB_FETCHED_ALL, DISTRIBUTION_JOB_FETCHING_ALL, SERIALIZE } from 'store/actions/types';

const initialState = {
    fetching: false,
    inProgress: false,
    success: false,
    jobs: {},
    jobConfigTableMessage: ''
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_JOB_DELETE_ERROR:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                jobConfigTableMessage: action.jobConfigTableMessage,
            });

        case DISTRIBUTION_JOB_DELETED:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                jobConfigTableMessage: action.jobConfigTableMessage,
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case DISTRIBUTION_JOB_DELETING:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                testingConfig: true,
                configurationMessage: action.configurationMessage,
                error: {
                    ...action.errors,
                    message: action.configurationMessage
                }
            });

        case DISTRIBUTION_JOB_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.configurationMessage,
                error: {
                    ...action.errors,
                    message: action.configurationMessage
                }
            });

        case DISTRIBUTION_JOB_FETCHED_ALL:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: false,
                success: false,
                testingConfig: false,
                configurationMessage: action.configurationMessage,
                error: {
                    message: ''
                },
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case DISTRIBUTION_JOB_FETCHING_ALL:
            return Object.assign({}, state, {
                fetching: false,
                inProgress: true,
                success: false,
                testingConfig: false,
                configurationMessage: 'Saving...',
                error: {
                    message: ''
                },
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;

    }
}

export default config;
