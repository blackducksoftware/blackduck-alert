import { createPostRequest } from 'util/RequestUtilities';
import HeaderUtilities from 'util/HeaderUtilities';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const auditReadRequest = async (csrfToken, jobIds) => createPostRequest('/alert/api/audit/job', csrfToken, {
    jobIds
});

const readAuditDataAndBuildTableData = (csrfToken, jobs, stateUpdateFunctions, createTableEntry) => {
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
    const headersUtil = new HeaderUtilities();
    headersUtil.addApplicationJsonContentType();
    headersUtil.addXCsrfToken(csrfToken);

    const pageNumber = currentPage ? currentPage - 1 : 0;
    const searchPageSize = pageSize || 10;
    const encodedSearchTerm = encodeURIComponent(searchTerm);
    const requestUrl = `${ConfigRequestBuilder.JOB_API_URL}?pageNumber=${pageNumber}&pageSize=${searchPageSize}&searchTerm=${encodedSearchTerm}`;
    fetch(requestUrl, {
        credentials: 'same-origin',
        headers: headersUtil.getHeaders()
    })
        .then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        const { jobs } = responseData;
                        setTotalPages(responseData.totalPages);
                        readAuditDataAndBuildTableData(csrfToken, jobs, stateUpdateFunctions, createTableEntry);
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
    csrfToken, job, stateUpdateFunctions, removeTableEntry
}) => {
    const { jobId } = job;
    const {
        setProgress, setError
    } = stateUpdateFunctions;
    setProgress(true);
    const errorHandlers = [];
    // errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    // errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobDeleteError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
    const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, jobId);
    request.then((response) => {
        if (response.ok) {
            removeTableEntry(jobId);
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

// export function validateCurrentJobs() {
//     return (dispatch, getState) => {
//         dispatch(jobsValidationFetching());
//         const { session, distributions } = getState();
//         const errorHandlers = [];
//         errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
//         errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobsValidationError(HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
//         const headersUtil = new HeaderUtilities();
//         headersUtil.addApplicationJsonContentType();
//         headersUtil.addXCsrfToken(session.csrfToken);
//
//         const jobIdsToValidate = [];
//         distributions.jobs.forEach(job => {
//             jobIdsToValidate.push(job.jobId);
//         });
//         RequestUtilities.createPostRequest(`${ConfigRequestBuilder.JOB_API_URL}/validateJobsById`, session.csrfToken, {
//             jobIds: jobIdsToValidate
//         })
//             .then((response) => {
//                 response.json()
//                     .then((responseData) => {
//                         if (response.ok) {
//                             dispatch(jobsValidationFetched(responseData));
//                         } else {
//                             errorHandlers.push(HTTPErrorUtils.createDefaultHandler(() => {
//                                 let message = '';
//                                 if (responseData && responseData.message) {
//                                     // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
//                                     message = responseData.message.toString();
//                                 }
//                                 return jobsValidationError(message);
//                             }));
//                             const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
//                             dispatch(handler(response.status));
//                         }
//                     });
//             })
//             .catch((error) => {
//                 console.log(error);
//                 dispatch(jobsValidationError(error));
//             });
//     };
// }
