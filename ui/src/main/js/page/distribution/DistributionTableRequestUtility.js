import * as RequestUtilities from 'common/util/RequestUtilities';
import { createPostRequest, createReadRequest } from 'common/util/RequestUtilities';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const auditReadRequest = async (csrfToken, jobIds) => createPostRequest('/alert/api/audit/job', csrfToken, {
    jobIds
});

const validateCurrentJobs = ({
    csrfToken, errorHandler, stateUpdateFunctions, jobIds
}) => {
    const { setProgress, setError, setJobsValidationResults } = stateUpdateFunctions;
    setProgress(true);
    RequestUtilities.createPostRequest(`${ConfigRequestBuilder.JOB_API_URL}/validateJobsById`, csrfToken, {
        jobIds
    })
        .then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        setJobsValidationResults(responseData);
                    } else {
                        setError(errorHandler.handle(response, responseData, false));
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

const readAuditDataAndBuildTableData = (csrfToken, errorHandler, stateUpdateFunctions, createTableEntry, jobs) => {
    const {
        setProgress, setTableData
    } = stateUpdateFunctions;

    setProgress(true);
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
            validateCurrentJobs({
                csrfToken, errorHandler, stateUpdateFunctions, jobIds
            });
        });
};

export const fetchDistributions = ({
    csrfToken, errorHandler, pagingData, stateUpdateFunctions, createTableEntry
}) => {
    const { currentPage, pageSize, searchTerm } = pagingData;
    const {
        setProgress, setError, setTotalPages, setTableData
    } = stateUpdateFunctions;
    setProgress(true);
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
                        readAuditDataAndBuildTableData(csrfToken, errorHandler, stateUpdateFunctions, createTableEntry, jobs);
                    } else {
                        setError(errorHandler.handle(response, responseData, true));
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

export const fetchDistributionsWithAudit = ({
    csrfToken, errorHandler, pagingData, sortData, stateUpdateFunctions, createTableEntry
}) => {
    const { currentPage, pageSize, searchTerm } = pagingData;
    const {
        setProgress, setError, setTotalPages, setTableData
    } = stateUpdateFunctions;
    const { sortName, sortOrder } = sortData;
    setProgress(true);
    const pageNumber = currentPage ? currentPage - 1 : 0;
    const searchPageSize = pageSize || 10;
    const encodedSearchTerm = encodeURIComponent(searchTerm);
    const requestUrl = `${ConfigRequestBuilder.JOB_AUDIT_API_URL}?pageNumber=${pageNumber}&pageSize=${searchPageSize}&searchTerm=${encodedSearchTerm}&sortBy=${sortName}&sortOrder=${sortOrder}`;
    createReadRequest(requestUrl, csrfToken)
        .then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        const { models } = responseData;
                        setTotalPages(responseData.totalPages);
                        const tableData = models.map((model) => createTableEntry(model));
                        setTableData(tableData);
                    } else {
                        setError(errorHandler.handle(response, responseData, true));
                        setTableData([]);
                    }
                    setProgress(false);
                });
        })
        .catch((httpError) => {
            console.log(httpError);
            setError(HTTPErrorUtils.createErrorObject({ message: httpError }));
            setProgress(false);
        });
};

export const deleteDistribution = ({
    csrfToken, errorHandler, distributionId, stateUpdateFunctions
}) => {
    const {
        setProgress, setError
    } = stateUpdateFunctions;
    setProgress(true);
    const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, distributionId);
    request.then((response) => {
        if (!response.ok) {
            response.json()
                .then((responseData) => {
                    setError(errorHandler.handle(response, responseData, false));
                    setProgress(false);
                });
        }
    }).catch(console.error);
};
