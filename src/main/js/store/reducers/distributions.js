import { DISTRIBUTION_JOB_DELETE_ERROR, DISTRIBUTION_JOB_DELETED, DISTRIBUTION_JOB_DELETING, DISTRIBUTION_JOB_FETCH_ERROR_ALL, DISTRIBUTION_JOB_FETCHED_ALL, DISTRIBUTION_JOB_FETCHING_ALL, SERIALIZE } from 'store/actions/types';

const initialState = {
    inProgress: false,
    jobs: {},
    jobConfigTableMessage: ''
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_JOB_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                jobConfigTableMessage: action.jobConfigTableMessage
            });

        case DISTRIBUTION_JOB_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                jobConfigTableMessage: action.jobConfigTableMessage,
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case DISTRIBUTION_JOB_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                jobConfigTableMessage: action.jobConfigTableMessage
            });

        case DISTRIBUTION_JOB_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                jobConfigTableMessage: action.jobConfigTableMessage
            });

        case DISTRIBUTION_JOB_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                jobConfigTableMessage: action.jobConfigTableMessage,
                jobs: {
                    ...state.jobs,
                    ...action.jobs
                }
            });

        case DISTRIBUTION_JOB_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: true,
                jobConfigTableMessage: 'Loading...'
            });

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
