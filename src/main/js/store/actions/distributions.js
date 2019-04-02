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

function updateJobWithAuditInfo(job) {
    return {
        type: DISTRIBUTION_JOB_UPDATE_AUDIT_INFO,
        job
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

function deletingJobConfigSuccess(jobId) {
    return {
        type: DISTRIBUTION_JOB_DELETED,
        jobId
    };
}

function jobDeleteError(message) {
    return {
        type: DISTRIBUTION_JOB_DELETE_ERROR,
        jobDeleteMessage: message
    };
}

function updateJobModelWithAuditInfo(dispatch, jobConfig, lastRan, status) {
    let newConfig = Object.assign({}, jobConfig);
    newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'lastRan', lastRan);
    newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'status', status);
    dispatch(updateJobWithAuditInfo(newConfig));
}

function fetchAuditInfoForJob(jobConfig) {
    return (dispatch, getState) => {
        const { csrfToken } = getState().session;
        const newConfig = Object.assign({}, jobConfig);
        let lastRan = 'Unknown';
        let currentStatus = 'Unknown';

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
                        const auditInfo = JSON.parse(jsonObj.message);
                        if (auditInfo != null) {
                            lastRan = auditInfo.timeLastSent;
                            currentStatus = auditInfo.status;
                        }
                        updateJobModelWithAuditInfo(dispatch, newConfig, lastRan, currentStatus);
                    });
                } else {
                    updateJobModelWithAuditInfo(dispatch, newConfig, lastRan, currentStatus);
                }
            }).catch((error) => {
                console.log(error);
            });
        }
    };
}

export function openJobDeleteModal() {
    return (dispatch, getState) => dispatch(openJobDelete());
}

export function deleteDistributionJob(job) {
    return (dispatch, getState) => {
        const { jobId } = job;
        dispatch(deletingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, jobId);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletingJobConfigSuccess(jobId));
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
