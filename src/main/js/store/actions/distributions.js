import {
    DISTRIBUTION_JOB_DELETE_ERROR,
    DISTRIBUTION_JOB_DELETE_OPEN_MODAL,
    DISTRIBUTION_JOB_DELETED,
    DISTRIBUTION_JOB_DELETING,
    DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND,
    DISTRIBUTION_JOB_FETCH_ERROR_ALL,
    DISTRIBUTION_JOB_FETCHED_ALL,
    DISTRIBUTION_JOB_FETCHING_ALL
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';


function fetchingAllJobs() {
    return {
        type: DISTRIBUTION_JOB_FETCHING_ALL
    };
}

function allJobsFetched(jobs) {
    return {
        type: DISTRIBUTION_JOB_FETCHED_ALL,
        jobs
    };
}


function fetchingAllJobsError(message) {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR_ALL,
        jobConfigTableMessage: message
    };
}

function fetchingAllJobsNoneFound() {
    return {
        type: DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND,
        jobConfigTableMessage: ''
    };
}


function openJobDelete() {
    return {
        type: DISTRIBUTION_JOB_DELETE_OPEN_MODAL,
        jobDeleteMessage: ''
    };
}

function deletingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_DELETING
    };
}

function deletingJobConfigSuccess() {
    return {
        type: DISTRIBUTION_JOB_DELETED
    };
}

function jobDeleteError(message) {
    return {
        type: DISTRIBUTION_JOB_DELETE_ERROR,
        jobDeleteMessage: message
    };
}

function fetchAuditInfoForJob(csrfToken, jobConfig) {
    let newConfig = Object.assign({}, jobConfig);
    let lastRan = 'Unknown';
    let status = 'Unknown';

    const configId = FieldModelUtilities.getFieldModelSingleValue(jobConfig, 'configId');
    fetch(`/alert/api/audit/job/${configId}`, {
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': csrfToken,
            'Content-Type': 'application/json'
        }
    }).then((response) => {
        if (response.ok) {
            response.json().then((jsonObj) => {
                if (jsonObj != null) {
                    lastRan = jsonObj.timeLastSent;
                    [status] = jsonObj;
                }
            });
        }
    }).catch((error) => {
        console.log(error);
    });

    newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'lastRan', lastRan);
    newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'status', status);

    return newConfig;
}

export function openJobDeleteModal() {
    return (dispatch, getState) => dispatch(openJobDelete());
}

export function deleteDistributionJob(job) {
    return (dispatch, getState) => {
        dispatch(deletingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, job.jobId);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletingJobConfigSuccess());
            } else {
                response.json()
                    .then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(jobDeleteError(data.message));
                            case 401:
                                dispatch(jobDeleteError(data.message));
                                return dispatch(verifyLoginByStatus(response.status));
                            case 412:
                                return dispatch(jobDeleteError(data.message));
                            default: {
                                return dispatch(jobDeleteError(data.message, null));
                            }
                        }
                    });
            }
        }).catch(console.error);
    };
}

export function fetchDistributionJobs() {
    return (dispatch, getState) => {
        dispatch(fetchingAllJobs());
        const { csrfToken } = getState().session;
        fetch(ConfigRequestBuilder.JOB_API_URL, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            if (response.ok) {
                response.json().then((jsonArray) => {
                    const newJobs = [];
                    jsonArray.forEach((jobConfig) => {
                        const jobConfigWithAuditInfo = fetchAuditInfoForJob(csrfToken, jobConfig);
                        newJobs.push(jobConfigWithAuditInfo);
                    });
                    dispatch(allJobsFetched(newJobs));
                });
            } else {
                switch (response.status) {
                    case 401:
                    case 403:
                        dispatch(verifyLoginByStatus(response.status));
                        break;
                    case 404:
                        dispatch(fetchingAllJobsNoneFound());
                        break;
                    default:
                        response.json().then((json) => {
                            dispatch(fetchingAllJobsError(json.message));
                        });
                }
            }
        }).catch((error) => {
            console.log(error);
            dispatch(fetchingAllJobsError(error));
        });
    };
}
