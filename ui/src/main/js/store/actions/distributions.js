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
    DISTRIBUTION_JOB_VALIDATE_ALL_FETCHING
} from 'store/actions/types';

import { unauthorized } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';
import * as RequestUtilities from 'util/RequestUtilities'
import { createPostRequest } from 'util/RequestUtilities'
import HeaderUtilities from 'util/HeaderUtilities';

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

function allJobsFetched(totalPages) {
    return {
        type: DISTRIBUTION_JOB_FETCHED_ALL,
        totalPages
    };
}

function fetchingAllJobsError(message) {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR_ALL,
        jobConfigTableMessage: message,
        message
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

function jobsValidationFetching() {
    return {
        type: DISTRIBUTION_JOB_VALIDATE_ALL_FETCHING
    };
}

function jobsValidationFetched(result) {
    return {
        type: DISTRIBUTION_JOB_VALIDATE_ALL_FETCHED,
        jobsValidationResult: result
    };
}

function jobsValidationError(message) {
    return {
        type: DISTRIBUTION_JOB_VALIDATE_ALL_ERROR,
        jobsValidationMessage: message
    };
}

function updateJobModelWithAuditInfo(dispatch, jobConfig, lastRan, status) {
    let newConfig = { ...jobConfig };
    newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'lastRan', lastRan);
    newConfig = FieldModelUtilities.updateFieldModelSingleValue(newConfig, 'status', status);
    dispatch(updateJobWithAuditInfo(newConfig));
}

function queryForJobAuditInfo(jobConfigs) {
    return (dispatch, getState) => {
        const { csrfToken } = getState().session;

        if (jobConfigs) {
            const jobIds = [];
            jobConfigs.forEach(jobConfig => {
                jobIds.push(jobConfig.jobId);
            });

            createPostRequest(`/alert/api/audit/job`, csrfToken, {
                jobIds: jobIds
            }).then((response) => {
                if (response.ok) {
                    response.json().then((auditQueryResult) => {
                        const jobIdToStatus = {};
                        auditQueryResult.statuses.forEach(status => {
                            jobIdToStatus[status.jobId] = status;
                        });
                        jobConfigs.forEach(jobConfig => {
                            const jobAuditStatus = jobIdToStatus[jobConfig.jobId]
                            let lastRan = 'Unknown';
                            let currentStatus = 'Unknown';
                            if (jobAuditStatus) {
                                lastRan = jobAuditStatus.timeLastSent;
                                currentStatus = jobAuditStatus.status;
                            }
                            updateJobModelWithAuditInfo(dispatch, jobConfig, lastRan, currentStatus);
                        });
                    });
                } else {
                    jobConfigs.forEach(jobConfig => {
                        updateJobModelWithAuditInfo(dispatch, jobConfig, 'Unknown', 'Unknown');
                    });
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
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobDeleteError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, jobId);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletingJobConfigSuccess(jobId));
            } else {
                response.json()
                    .then((responseData) => {
                        const deleteMessageHandler = () => jobDeleteError(responseData.message);
                        errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(deleteMessageHandler));
                        errorHandlers.push(HTTPErrorUtils.createPreconditionFailedHandler(deleteMessageHandler));
                        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => jobDeleteError(responseData.message, null)));
                        const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                        dispatch(handler(response.status));
                    });
            }
        }).catch(console.error);
    };
}

export function fetchDistributionJobs(pageNumber, pageLimit, searchTerm) {
    return (dispatch, getState) => {
        dispatch(fetchingAllJobs());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAllJobsError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        errorHandlers.push(HTTPErrorUtils.createNotFoundHandler(fetchingAllJobsNoneFound));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(csrfToken);

        pageNumber = pageNumber ? pageNumber - 1 : 0;
        pageLimit = pageLimit ? pageLimit : 10;
        const requestUrl = `${ConfigRequestBuilder.JOB_API_URL}?pageNumber=${pageNumber}&pageSize=${pageLimit}&searchTerm=${searchTerm}`;
        fetch(requestUrl, {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            const { jobs } = responseData;
                            dispatch(queryForJobAuditInfo(jobs));
                            dispatch(allJobsFetched(responseData.totalPages));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                                let message = '';
                                if (responseData && responseData.message) {
                                    // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                    message = responseData.message.toString();
                                }
                                return fetchingAllJobsError(message);
                            }));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch((error) => {
                console.log(error);
                dispatch(fetchingAllJobsError(error));
            });
    };
}

export function validateCurrentJobs() {
    return (dispatch, getState) => {
        dispatch(jobsValidationFetching());
        const { session, distributions } = getState();
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobsValidationError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
        const headersUtil = new HeaderUtilities();
        headersUtil.addApplicationJsonContentType();
        headersUtil.addXCsrfToken(session.csrfToken);

        const jobIdsToValidate = [];
        distributions.jobs.forEach(job => {
            jobIdsToValidate.push(job.jobId);
        });
        RequestUtilities.createPostRequest(`${ConfigRequestBuilder.JOB_API_URL}/validateJobsById`, session.csrfToken, {
            jobIds: jobIdsToValidate
        })
            .then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            dispatch(jobsValidationFetched(responseData));
                        } else {
                            errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
                                let message = '';
                                if (responseData && responseData.message) {
                                    // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                    message = responseData.message.toString();
                                }
                                return jobsValidationError(message);
                            }));
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            })
            .catch((error) => {
                console.log(error);
                dispatch(jobsValidationError(error));
            });
    };
}
