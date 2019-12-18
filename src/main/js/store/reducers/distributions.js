import {
    DISTRIBUTION_JOB_DELETE_ERROR,
    DISTRIBUTION_JOB_DELETE_OPEN_MODAL,
    DISTRIBUTION_JOB_DELETED,
    DISTRIBUTION_JOB_DELETING,
    DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND,
    DISTRIBUTION_JOB_FETCH_ERROR_ALL,
    DISTRIBUTION_JOB_FETCHED_ALL,
    DISTRIBUTION_JOB_FETCHING_ALL,
    DISTRIBUTION_JOB_UPDATE_AUDIT_INFO,
    DISTRIBUTION_JOB_VALIDATE_ALL_ERROR,
    DISTRIBUTION_JOB_VALIDATE_ALL_FETCHED,
    DISTRIBUTION_JOB_VALIDATE_ALL_FETCHING,
    SERIALIZE
} from 'store/actions/types';

const initialState = {
    inProgress: false,
    deleteSuccess: false,
    jobs: [],
    jobConfigTableMessage: '',
    jobDeleteMessage: '',
    jobsValidationResult: [],
    jobsValidationMessage: ''
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DISTRIBUTION_JOB_DELETE_OPEN_MODAL:
        case DISTRIBUTION_JOB_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                jobDeleteMessage: action.jobDeleteMessage
            });

        case DISTRIBUTION_JOB_FETCH_ERROR_ALL:
        case DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                jobConfigTableMessage: action.jobConfigTableMessage,
                jobs: []
            });

        case DISTRIBUTION_JOB_UPDATE_AUDIT_INFO:
            return Object.assign({}, state, {
                jobs: [
                    action.job,
                    ...state.jobs.filter(job => job.jobId !== action.job.jobId)
                ]
            });

        case DISTRIBUTION_JOB_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                jobConfigTableMessage: action.jobConfigTableMessage
            });

        case DISTRIBUTION_JOB_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true,
                jobConfigTableMessage: '',
                jobs: state.jobs.filter(job => job.jobId !== action.jobId)
            });

        case DISTRIBUTION_JOB_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                jobConfigTableMessage: 'Deleting...'
            });

        case DISTRIBUTION_JOB_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                jobConfigTableMessage: 'Loading...',
                jobs: []
            });
        case DISTRIBUTION_JOB_VALIDATE_ALL_FETCHING:
            return Object.assign({}, state, {
                jobsValidationResult: [],
                jobsValidationMessage: ''
            });
        case DISTRIBUTION_JOB_VALIDATE_ALL_FETCHED:
            return Object.assign({}, state, {
                jobsValidationResult: action.jobsValidationResult
            });
        case DISTRIBUTION_JOB_VALIDATE_ALL_ERROR:
            return Object.assign({}, state, {
                jobsValidationMessage: action.jobsValidationMessage
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
