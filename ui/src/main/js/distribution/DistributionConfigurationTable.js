import * as PropTypes from 'prop-types';
import React, { useEffect, useRef, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { DISTRIBUTION_COMMON_FIELD_KEYS, DISTRIBUTION_URLS } from 'distribution/DistributionModel';
import {
    BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn
} from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import DescriptorLabel from 'component/common/DescriptorLabel';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';
import HeaderUtilities from 'util/HeaderUtilities';
import { createPostRequest } from 'util/RequestUtilities';
import ConfirmModal from 'component/common/ConfirmModal';
import AutoRefresh from 'component/common/AutoRefresh';
import IconTableCellFormatter from 'component/common/IconTableCellFormatter';

const DistributionConfigurationTable = ({
    csrfToken, readonly, shouldRefresh, descriptors
}) => {
    const [error, setError] = useState(HTTPErrorUtils.createEmptyErrorObject());
    const [progress, setProgress] = useState(false);
    const [tableData, setTableData] = useState([]);
    const [selectedConfigs, setSelectedConfigs] = useState([]);
    const [showDelete, setShowDelete] = useState(false);
    const [selectedRow, setSelectedRow] = useState(null);
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [searchTerm, setSearchTerm] = useState('');
    const [jobsValidationResults, setJobsValidationResults] = useState(null);
    const tableRef = useRef();

    const auditReadRequest = async (jobIds) => createPostRequest('/alert/api/audit/job', csrfToken, {
        jobIds
    });

    const readAuditDataAndBuildTableData = (jobs) => {
        const jobIds = [];
        jobs.forEach((jobConfig) => {
            jobIds.push(jobConfig.jobId);
        });
        auditReadRequest(jobIds)
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
                            if (jobConfig.fieldModels) {
                                const channelModel = jobConfig.fieldModels.find((model) => FieldModelUtilities.hasKey(model, DISTRIBUTION_COMMON_FIELD_KEYS.channelName));
                                const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
                                const id = jobConfig.jobId;
                                const name = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.name);
                                const distributionType = channelModel.descriptorName;
                                const frequency = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.frequency);
                                const enabled = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.enabled);
                                const entry = {
                                    id,
                                    name,
                                    distributionType,
                                    providerName,
                                    frequency,
                                    lastRan,
                                    status: currentStatus,
                                    enabled
                                };
                                dataWithAuditInfo.push(entry);
                            }
                        });
                        setTableData(dataWithAuditInfo);
                        setProgress(false);
                    });
            });
    };

    const retrieveTableData = () => {
        setProgress(true);
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
                            readAuditDataAndBuildTableData(jobs);
                        } else {
                            // TODO handle other status codes
                            setError(HTTPErrorUtils.createErrorObject(responseData));
                            setTableData([]);
                        }
                    });
            })
            .catch((httpError) => {
                console.log(error);
                setError(HTTPErrorUtils.createErrorObject({ message: httpError }));
                setProgress(false);
            });
    };

    const deleteTableData = () => {
        if (selectedConfigs) {
            selectedConfigs.forEach((config) => {
                const configId = FieldModelUtilities.getFieldModelId(config);
                // deleteRequest(configId);
            });
        }
        retrieveTableData();
        setSelectedConfigs([]);
        setShowDelete(false);
    };

    useEffect(() => {
        retrieveTableData();
    }, []);

    const insertAndDeleteButton = (
        <div>
            <NavLink to={DISTRIBUTION_URLS.distributionConfigUrl} activeClassName="addJobButton btn-md">
                <InsertButton
                    id="distribution-insert-button"
                    className="addJobButton btn-md"
                    onClick={() => null}
                >
                    <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                    New
                </InsertButton>
            </NavLink>
            <DeleteButton
                id="distribution-delete-button"
                className="deleteJobButton btn-md"
                onClick={() => setShowDelete(true)}
            >
                <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                Delete
            </DeleteButton>
        </div>
    );

    const tableOptions = {
        btnGroup: () => insertAndDeleteButton,
        noDataText: 'No Data',
        clearSearch: true,
        // handleConfirmDeleteRow: this.collectItemsToDelete,
        defaultSortName: 'name',
        defaultSortOrder: 'asc'
        // onRowDoubleClick: this.editButtonClicked
    };

    const assignedDataFormat = (cell) => (
        <div title={(cell) ? cell.toString() : null}>
            {cell}
        </div>
    );

    const selectRow = {
        mode: 'checkbox',
        clickToSelect: true,
        bgColor(row, isSelect) {
            return isSelect && '#e8e8e8';
        }
    };

    const column = (header, value, dataFormat = assignedDataFormat, columnClassName = 'tableCell') => (
        <TableHeaderColumn
            key={header}
            dataField={header}
            searchable
            dataSort
            columnClassName={columnClassName}
            tdStyle={{ whiteSpace: 'normal' }}
            dataFormat={dataFormat}
        >
            {value}
        </TableHeaderColumn>
    );

    const createIconTableCellFormatter = (iconName, buttonText, clickFunction) => {
        const buttonId = buttonText.toLowerCase();
        return (cell, row) => (
            <IconTableCellFormatter
                id={`distribution-${buttonId}-cell`}
                handleButtonClicked={clickFunction}
                currentRowSelected={row}
                buttonIconName={iconName}
                buttonText={buttonText}
            />
        );
    };

    const createIconColumn = (iconName, text, clickFunction) => {
        const dataFormat = createIconTableCellFormatter(iconName, text, clickFunction);
        return (
            <TableHeaderColumn
                key={`${text}Key`}
                dataField=""
                width="48"
                columnClassName="tableCell"
                dataFormat={dataFormat}
                thStyle={{ textAlign: 'center' }}
            >
                {text}
            </TableHeaderColumn>
        );
    };

    const editFormat = (cell) => {
        const icon = (cell) ? 'check' : 'times';
        const color = (cell) ? 'synopsysGreen' : 'synopsysRed';
        const className = `alert-icon ${color}`;

        return (
            <div className="btn btn-link jobIconButton">
                <FontAwesomeIcon icon={icon} className={className} size="lg" />
            </div>
        );
    };

    const editButtonClicked = ({ id }) => {
        setSelectedRow(id);
        // Navigate to config page
    };

    const copyButtonClicked = ({ id }) => {
        setSelectedRow(id);
        // Navigate to config page
    };

    const enabledColumnFormatter = (cell) => {
        const icon = (cell === 'true') ? 'check' : 'times';
        const color = (cell === 'true') ? 'synopsysGreen' : 'synopsysRed';
        const className = `alert-icon ${color}`;

        return (
            <div className="btn btn-link jobIconButton" title={cell}>
                <FontAwesomeIcon icon={icon} className={className} size="lg" title={cell} />
            </div>
        );
    };

    const nameColumnFormatter = (cell, row) => {
        const defaultValue = <div className="inline" title={cell}>{cell}</div>;
        if (jobsValidationResults && jobsValidationResults.length > 0) {
            const jobErrors = jobsValidationResults.filter((item) => item.id === row.id);
            if (jobErrors && jobErrors.length > 0) {
                return (
                    <span className="missingData" title={cell}>
                        <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" title={cell} />
                        {defaultValue}
                    </span>
                );
            }
            return defaultValue;
        }
        return defaultValue;
    };

    const descriptorColumnFormatter = (cell) => {
        const defaultValue = <div className="inline" title={cell}>{cell}</div>;
        if (descriptors) {
            const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, cell, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
            if (descriptor) {
                return (<DescriptorLabel keyPrefix="distribution-channel-icon" descriptor={descriptor} />);
            }
            return defaultValue;
        }
        return defaultValue;
    };

    const frequencyColumnFormatter = (cell) => {
        let cellText = '';
        if (cell === 'REAL_TIME') {
            cellText = 'Real Time';
        } else if (cell === 'DAILY') {
            cellText = 'Daily';
        }

        return (
            <div title={cellText}>
                {cellText}
            </div>
        );
    };

    const statusColumnClassName = (fieldValue) => {
        let className = '';
        if (fieldValue === 'Pending') {
            className = 'statusPending';
        } else if (fieldValue === 'Success') {
            className = 'statusSuccess';
        } else if (fieldValue === 'Failure') {
            className = 'statusFailure';
        }
        return `${className} tableCell`;
    };

    return (

        <div>
            <div className="pull-right">
                <AutoRefresh startAutoReload={retrieveTableData} isEnabled={shouldRefresh} />
            </div>
            <ConfirmModal
                id="distribution-delete-confirm-modal"
                title="Delete"
                affirmativeAction={deleteTableData}
                affirmativeButtonText="Confirm"
                negativeAction={() => setShowDelete(false)}
                negativeButtonText="Cancel"
                message="Are you sure you want to delete these items?"
                showModal={showDelete}
            />
            <BootstrapTable
                version="4"
                hover
                condensed
                containerClass="table"
                trClassName="tableRow"
                headerContainerClass="scrollable"
                bodyContainerClass="tableScrollableBody"
                data={tableData}
                selectRow={selectRow}
                ref={tableRef}
                options={tableOptions}
                search
                newButton={!readonly}
                pagination
                remote
            >
                <TableHeaderColumn dataField="id" hidden isKey>Id</TableHeaderColumn>
                {column('name', 'Name', nameColumnFormatter)}
                {column('distributionType', 'Type', descriptorColumnFormatter)}
                {column('providerName', 'Provider', descriptorColumnFormatter)}
                {column('frequency', 'Frequency Type', frequencyColumnFormatter)}
                {column('lastRan', 'Last Run')}
                {column('status', 'Status', assignedDataFormat, statusColumnClassName)}
                {column('enabled', 'Enabled', enabledColumnFormatter)}
                {createIconColumn('pencil-alt', 'Edit', editButtonClicked)}
                {createIconColumn('copy', 'Copy', copyButtonClicked)}
            </BootstrapTable>
        </div>
    );
};
DistributionConfigurationTable.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    shouldRefresh: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

DistributionConfigurationTable.defaultProps = {
    readonly: false,
    shouldRefresh: false,
    descriptors: []
};

export default DistributionConfigurationTable;
