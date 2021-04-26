import * as RequestUtilities from 'util/RequestUtilities';
import { createPostRequest, createReadRequest } from 'util/RequestUtilities';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const auditReadRequest = async (csrfToken, jobIds) => createPostRequest('/alert/api/audit/job', csrfToken, {
    jobIds
});

export const validateCurrentJobs = ({ csrfToken, stateUpdateFunctions, jobIds }) => {
    const { setProgress, setError, setJobsValidationResults } = stateUpdateFunctions;
    setProgress(true);
    const errorHandlers = [];
    // errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    // errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobsValidationError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));

    RequestUtilities.createPostRequest(`${ConfigRequestBuilder.JOB_API_URL}/validateJobsById`, csrfToken, {
        jobIds
    })
        .then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        setJobsValidationResults(responseData);
                    } else {
                        setError(HTTPErrorUtils.createErrorObject(responseData));
                        setProgress(false);
                    }
                });
        })
        .catch((httpError) => {
            console.log(httpError);
            setError(HTTPErrorUtils.createErrorObject({ message: httpError }));
            setProgress(false);
        });
};

const readAuditDataAndBuildTableData = (csrfToken, stateUpdateFunctions, createTableEntry, jobs) => {
    const {
        setProgress, setTableData
    } = stateUpdateFunctions;

    const jobIds = [];
    jobs.forEach((jobConfig) => {
        jobIds.push(jobConfig.jobId);
    });
    auditReadRequest(csrfToken, jobIds)
        .then((response) => {
            const dataWithAuditInfo = [];
            const jobIdToStatus = {};
            response.json()
                .then((auditQueryResult) => {
                    if (response.ok) {
                        auditQueryResult.statuses.forEach((status) => {
                            jobIdToStatus[status.jobId] = status;
                        });
                    }
                })
                .then(() => {
                    jobs.forEach((jobConfig) => {
                        let lastRan = 'Unknown';
                        let currentStatus = 'Unknown';
                        const jobAuditStatus = jobIdToStatus[jobConfig.jobId];
                        if (jobAuditStatus) {
                            lastRan = jobAuditStatus.timeLastSent;
                            currentStatus = jobAuditStatus.status;
                        }
                        const entry = createTableEntry(jobConfig, lastRan, currentStatus);
                        if (entry) {
                            dataWithAuditInfo.push(entry);
                        }
                    });
                    setTableData(dataWithAuditInfo);
                    setProgress(false);
                });
        })
        .then(() => {
            validateCurrentJobs({ csrfToken, stateUpdateFunctions, jobIds });
        });
};

export const fetchDistributions = ({
    csrfToken, pagingData, stateUpdateFunctions, createTableEntry
}) => {
    const { currentPage, pageSize, searchTerm } = pagingData;
    const {
        setProgress, setError, setTotalPages, setTableData
    } = stateUpdateFunctions;
    setProgress(false);
    const errorHandlers = [];
    // errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(loggedOut));
    // errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => fetchingAllJobsError(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
    // errorHandlers.push(HTTPErrorUtils.createNotFoundHandler(fetchingAllJobsNoneFound));

    const pageNumber = currentPage ? currentPage - 1 : 0;
    const searchPageSize = pageSize || 10;
    const encodedSearchTerm = encodeURIComponent(searchTerm);
    const requestUrl = `${ConfigRequestBuilder.JOB_API_URL}?pageNumber=${pageNumber}&pageSize=${searchPageSize}&searchTerm=${encodedSearchTerm}`;
    createReadRequest(requestUrl, csrfToken)
        .then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        const { jobs } = responseData;
                        setTotalPages(responseData.totalPages);
                        readAuditDataAndBuildTableData(csrfToken, stateUpdateFunctions, createTableEntry, jobs);
                    } else {
                        // TODO handle other status codes
                        setError(HTTPErrorUtils.createErrorObject(responseData));
                        setTableData([]);
                    }
                });
        })
        .catch((httpError) => {
            console.log(httpError);
            setError(HTTPErrorUtils.createErrorObject({ message: httpError }));
            setProgress(false);
        });
};

export const deleteDistribution = ({
    csrfToken, distributionId, stateUpdateFunctions, removeTableEntry
}) => {
    const {
        setProgress, setError
    } = stateUpdateFunctions;
    setProgress(true);
    const errorHandlers = [];
    // errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    // errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobDeleteError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
    const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, distributionId);
    request.then((response) => {
        if (response.ok) {
            removeTableEntry(distributionId);
        } else {
            response.json()
                .then((responseData) => {
                    // TODO handle other status codes
                    setError(HTTPErrorUtils.createErrorObject(responseData));
                    setProgress(false);
                });
        }
    }).catch(console.error);
};
