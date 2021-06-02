import * as PropTypes from 'prop-types';
import React, { useEffect, useRef, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { DISTRIBUTION_COMMON_FIELD_KEYS, DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import {
    BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn
} from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import DescriptorLabel from 'common/DescriptorLabel';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import ConfirmModal from 'common/ConfirmModal';
import AutoRefresh from 'common/table/AutoRefresh';
import IconTableCellFormatter from 'common/table/IconTableCellFormatter';
import * as DistributionRequestUtility from 'page/distribution/DistributionTableRequestUtility';
import { EXISTING_CHANNELS, EXISTING_PROVIDERS } from 'common/DescriptorInfo';

const DistributionConfigurationTable = ({
    csrfToken, errorHandler, readonly, showRefreshButton, descriptors
}) => {
    const [error, setError] = useState(HTTPErrorUtils.createEmptyErrorObject());
    const [progress, setProgress] = useState(false);
    const [tableData, setTableData] = useState([]);
    const [showDelete, setShowDelete] = useState(false);
    const [selectedRows, setSelectedRows] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [searchTerm, setSearchTerm] = useState('');
    const [jobsValidationResults, setJobsValidationResults] = useState(null);
    const [entriesToDelete, setEntriesToDelete] = useState(null);
    const tableRef = useRef();
    const history = useHistory();

    const createTableEntry = (jobConfig, lastRan, currentStatus) => {
        if (!jobConfig.fieldModels) {
            return null;
        }
        const channelModel = jobConfig.fieldModels.find((model) => FieldModelUtilities.hasKey(model, DISTRIBUTION_COMMON_FIELD_KEYS.channelName));
        const providerName = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerName);
        const id = jobConfig.jobId;
        const name = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.name);
        const distributionType = channelModel.descriptorName;
        const frequency = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.frequency);
        const enabled = FieldModelUtilities.getFieldModelSingleValue(channelModel, DISTRIBUTION_COMMON_FIELD_KEYS.enabled);
        return {
            id,
            name,
            distributionType,
            providerName,
            frequency,
            lastRan,
            status: currentStatus,
            enabled
        };
    };

    const retrieveTableData = () => {
        const pagingData = {
            currentPage,
            pageSize,
            searchTerm
        };
        const stateUpdateFunctions = {
            setProgress,
            setError,
            setTableData,
            setTotalPages,
            setJobsValidationResults
        };
        DistributionRequestUtility.fetchDistributions({
            csrfToken, errorHandler, pagingData, stateUpdateFunctions, createTableEntry
        });
    };

    const deleteTableData = () => {
        if (entriesToDelete) {
            const stateUpdateFunctions = {
                setProgress,
                setError
            };
            entriesToDelete.forEach((config) => {
                const { id } = config;
                DistributionRequestUtility.deleteDistribution({
                    csrfToken, errorHandler, distributionId: id, stateUpdateFunctions
                });
            });
            const ids = entriesToDelete.map((entry) => entry.id);
            const newTableData = tableData.filter((distribution) => !ids.includes(distribution.id));
            setTableData(newTableData);
        }
        setSelectedRows([]);
        setShowDelete(false);
    };

    const onPageChange = (page, sizePerPage) => {
        setCurrentPage(page);
        setPageSize(sizePerPage);
    };

    const onSizePerPageList = (sizePerPage) => {
        setPageSize(sizePerPage);
    };

    const onSearchChange = (inputSearchTerm) => {
        setSearchTerm(inputSearchTerm);
        setCurrentPage(1);
    };

    useEffect(() => {
        retrieveTableData();
    }, [currentPage, pageSize, searchTerm]);

    useEffect(() => {
        let possibleDeleteEntries = [];
        selectedRows.forEach((id) => {
            possibleDeleteEntries = possibleDeleteEntries.concat(tableData.filter((distribution) => distribution.id === id));
        });
        if (possibleDeleteEntries && possibleDeleteEntries.length === 0) {
            setEntriesToDelete(null);
        } else {
            setEntriesToDelete(possibleDeleteEntries);
        }
    }, [selectedRows]);

    const navigateToConfigPage = (id, copy) => {
        const url = (copy) ? DISTRIBUTION_URLS.distributionConfigCopyUrl : DISTRIBUTION_URLS.distributionConfigUrl;
        if (id) {
            history.push(`${url}/${id}`);
            return;
        }
        history.push(url);
    };

    const insertAndDeleteButton = (buttons) => {
        const insertClick = () => {
            buttons.insertBtn.props.onClick();
            navigateToConfigPage();
        };
        const deleteClick = () => {
            buttons.deleteBtn.props.onClick();
            setShowDelete(true);
        };
        return (
            <div>
                {!readonly
                && (
                    <>
                        <InsertButton
                            id="distribution-insert-button"
                            className="addJobButton btn-md"
                            onClick={insertClick}
                        >
                            <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                            New
                        </InsertButton>
                        <DeleteButton
                            id="distribution-delete-button"
                            className="deleteJobButton btn-md"
                            onClick={deleteClick}
                        >
                            <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                            Delete
                        </DeleteButton>
                    </>
                )}
                {showRefreshButton
                && (
                    <button
                        id="distribution-refresh-button"
                        type="button"
                        className="btn btn-md btn-info react-bs-table-add-btn tableButton"
                        onClick={retrieveTableData}
                    >
                        <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />
                        Refresh
                    </button>
                )}
            </div>
        );
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

    const editButtonClicked = ({ id }) => {
        setSelectedRows(id);
        // Navigate to config page
        navigateToConfigPage(id);
    };

    const copyButtonClicked = ({ id }) => {
        setSelectedRows(id);
        // Navigate to config page
        navigateToConfigPage(id, true);
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
            const descriptorOptions = {
                ...EXISTING_PROVIDERS,
                ...EXISTING_CHANNELS
            };
            const descriptor = descriptorOptions[cell];
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

    const tableOptions = {
        btnGroup: insertAndDeleteButton,
        noDataText: 'No Data',
        clearSearch: true,
        handleConfirmDeleteRow: (next, rows) => setSelectedRows(rows),
        defaultSortName: 'name',
        defaultSortOrder: 'asc',
        onRowDoubleClick: (id) => {
            editButtonClicked(id);
        },
        // Paging
        sizePerPage: pageSize,
        page: currentPage,
        onPageChange,
        onSizePerPageList,
        onSearchChange
    };

    const tableFetchInfo = {
        dataTotalSize: totalPages * pageSize
    };

    return (
        <div>
            <div className="pull-right">
                <AutoRefresh startAutoReload={retrieveTableData} isEnabled={!showRefreshButton} />
            </div>
            <ConfirmModal
                id="distribution-delete-confirm-modal"
                title="Delete"
                affirmativeAction={deleteTableData}
                affirmativeButtonText="Confirm"
                modalSize="lg"
                negativeAction={() => setShowDelete(false)}
                negativeButtonText="Cancel"
                showModal={showDelete}
            >
                <div className="form-group">
                    <BootstrapTable
                        version="4"
                        hover
                        condensed
                        data={entriesToDelete}
                        options={{
                            noDataText: 'No jobs configured'
                        }}
                        containerClass="table"
                        trClassName="tableRow"
                        headerContainerClass="scrollable"
                        bodyContainerClass="tableScrollableBody"
                    >
                        <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                        <TableHeaderColumn dataField="distributionConfigId" hidden>
                            Distribution
                            Id
                        </TableHeaderColumn>
                        {column('name', 'Distribution Job', nameColumnFormatter)}
                        {column('distributionType', 'Type', descriptorColumnFormatter)}
                        {column('providerName', 'Provider', descriptorColumnFormatter)}
                        {column('frequency', 'Frequency Type', frequencyColumnFormatter)}
                        {column('lastRan', 'Last Run')}
                        {column('status', 'Status', assignedDataFormat, statusColumnClassName)}
                    </BootstrapTable>
                </div>
            </ConfirmModal>
            <BootstrapTable
                version="4"
                hover
                condensed
                containerClass="table"
                trClassName="tableRow"
                headerContainerClass="scrollable"
                bodyContainerClass="tableScrollableBody"
                data={tableData}
                insertRow
                deleteRow
                newButton={!readonly}
                selectRow={selectRow}
                ref={tableRef}
                options={tableOptions}
                fetchInfo={tableFetchInfo}
                search
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
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

DistributionConfigurationTable.defaultProps = {
    readonly: false,
    showRefreshButton: false,
    descriptors: []
};

export default DistributionConfigurationTable;
