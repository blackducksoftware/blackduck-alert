import {
    DISTRIBUTION_JOB_DELETE_ERROR,
    DISTRIBUTION_JOB_DELETE_OPEN_MODAL,
    DISTRIBUTION_JOB_DELETED,
    DISTRIBUTION_JOB_DELETING,
    DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND,
    DISTRIBUTION_JOB_FETCH_ERROR_ALL,
    DISTRIBUTION_JOB_FETCHED_ALL,
    DISTRIBUTION_JOB_FETCHING_ALL,
    DISTRIBUTION_JOB_UPDATE_AUDIT_INFO
} from 'store/actions/types';

import { verifyLoginByStatus } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

function updateJobWithAuditInfo(jobs) {
    return {
        type: DISTRIBUTION_JOB_UPDATE_AUDIT_INFO,
        jobs
    };
}

function fetchingAllJobs() {
    return {
        type: DISTRIBUTION_JOB_FETCHING_ALL
    };
}

function allJobsFetched() {
    return {
        type: DISTRIBUTION_JOB_FETCHED_ALL
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

function fetchAuditInfoForJob(jobConfig) {
    return (dispatch, getState) => {
        const { csrfToken } = getState().session;
        let newConfig = Object.assign({}, jobConfig);
        let lastRan = 'Unknown';
        let status = 'Unknown';

        if (jobConfig) {
            fetch(`/alert/api/audit/job/${jobConfig.jobId}`, {
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
            }).finally(() => {
                newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'lastRan', lastRan);
                newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'status', status);

                let jobList = getState().jobs;
                if (!jobList || jobList.length === 0) {
                    jobList = [];
                }
                // remove this job from the list because it didnt have the audit information
                jobList = jobList.filter((job, index, arr) => job.jobId !== jobConfig.jobId);
                // Add the job back to the list with the new audit information
                jobList.push(newConfig);

                dispatch(updateJobWithAuditInfo(jobList));
            });
        }
    };
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
                    jsonArray.forEach((jobConfig) => {
                        dispatch(fetchAuditInfoForJob(jobConfig));
                    });
                    dispatch(allJobsFetched());
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
