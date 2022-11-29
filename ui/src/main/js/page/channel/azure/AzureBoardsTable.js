import React, { useRef, useState, useEffect } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/configuration/global/CommonGlobalConfiguration';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import AutoRefresh from 'common/component/table/AutoRefresh';
import ConfirmModal from 'common/component/ConfirmModal';
import { ProgressIcon } from 'common/component/table/ProgressIcon';
import { useHistory } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import IconTableCellFormatter from 'common/component/table/IconTableCellFormatter';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';

const AzureBoardsTable = ({
    csrfToken,
    fieldKey,
    label,
    description,
    children,
    tableData,
    apiUrl,
    setTableData,
    readonly,
    displayDelete,
    editPageUrl,
    copyPageUrl,
    showRefreshButton,
    includeEnabled
}) => {
    const [showDelete, setShowDelete] = useState(false);
    const [allSelectedRows, setAllSelectedRows] = useState([]);
    const [progress, setProgress] = useState(false);

    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [sortName, setSortName] = useState('name');
    const [sortOrder, setSortOrder] = useState('asc');
    const [searchTerm, setSearchTerm] = useState('');

    const tableRef = useRef();
    const history = useHistory();

    const retrieveTableData = async () => {
        setProgress(true);

        const mutatorData = {
            sortName,
            sortOrder,
            searchTerm
        };

        const pageNumber = currentPage ? currentPage - 1 : 0;

        const response = await ConfigurationRequestBuilder.createReadPageRequest(apiUrl, csrfToken, pageNumber, pageSize, mutatorData);

        const data = await response.json();

        if (data) {
            setTotalPages(data.totalPages);
            setTableData(data.models);
        }

        setProgress(false);
    };

    useEffect(() => {
        retrieveTableData();
    }, [currentPage, pageSize, searchTerm, sortName, sortOrder]);

    const deleteTableData = async () => {
        setProgress(true);
        if (allSelectedRows) {
            await Promise.all(allSelectedRows.map(async (configId) => {
                await ConfigurationRequestBuilder.createDeleteRequest(apiUrl, csrfToken, configId);
            }));
        }
        await retrieveTableData();
        setShowDelete(false);
        setProgress(false);
    };

    const createTableCellFormatter = (iconName, buttonText, clickFunction) => {
        const buttonId = buttonText.toLowerCase();
        return (cell, row) => (
            <IconTableCellFormatter
                id={`${fieldKey}-${buttonId}-cell`}
                handleButtonClicked={clickFunction}
                currentRowSelected={row}
                buttonIconName={iconName}
                buttonText={buttonText}
            />
        );
    };

    const navigateToConfigPage = (id, copy) => {
        const url = (copy) ? copyPageUrl : editPageUrl;
        if (id) {
            history.push(`${url}/${id}`);
            return;
        }
        history.push(url);
    };

    const editButtonClicked = ({ id }) => {
        navigateToConfigPage(id);
    };

    const copyButtonClicked = ({ id }) => {
        navigateToConfigPage(id, true);
    };

    const editColumnFormatter = () => createTableCellFormatter('pencil-alt', 'Edit', editButtonClicked);
    const copyColumnFormatter = () => createTableCellFormatter('copy', 'Copy', copyButtonClicked);

    const enabledFormat = (cell) => {
        const icon = (cell) ? 'check' : 'times';
        const color = (cell) ? 'synopsysGreen' : 'synopsysRed';
        const className = `alert-icon ${color}`;

        return (
            <div className="btn btn-link jobIconButton">
                <FontAwesomeIcon icon={icon} className={className} size="lg" />
            </div>
        );
    };

    const createEnabledColumnHeader = () => includeEnabled && (
        <TableHeaderColumn
            dataField="enabled"
            width="70"
            columnClassName="tableCell"
            dataFormat={enabledFormat}
            thStyle={{ textAlign: 'center' }}
            tdStyle={{ textAlign: 'center' }}
        >
            Enabled
        </TableHeaderColumn>
    );

    const createIconTableHeader = (dataFormat, text) => (
        <TableHeaderColumn
            key={`${fieldKey}-${text}Key`}
            dataField=""
            width="48"
            columnClassName="tableCell"
            dataFormat={dataFormat}
            thStyle={{ textAlign: 'center' }}
            tdStyle={{ textAlign: 'center' }}
        >
            {text}
        </TableHeaderColumn>
    );

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
                { !readonly
                    && (
                        <InsertButton
                            id={`${fieldKey}-insert-button`}
                            className="addJobButton btn-md"
                            onClick={insertClick}
                        >
                            <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                            New
                        </InsertButton>

                    )}
                { !readonly && displayDelete
                    && (
                        <DeleteButton
                            id={`${fieldKey}-delete-button`}
                            className="deleteJobButton btn-md"
                            onClick={deleteClick}
                        >
                            <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                            Delete
                        </DeleteButton>
                    )}
                { showRefreshButton
                    && (
                        <button
                            id={`${fieldKey}-refresh-button`}
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

    const onSortChange = (newSortName, newSortOrder) => {
        setSortName(newSortName);
        setSortOrder(newSortOrder);
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

    const tableFetchInfo = {
        dataTotalSize: totalPages * pageSize
    };

    const tableOptions = {
        btnGroup: insertAndDeleteButton,
        noDataText: 'No Data',
        clearSearch: true,
        handleConfirmDeleteRow: (next, rows) => setAllSelectedRows(rows),
        onRowDoubleClick: (id) => {
            editButtonClicked(id);
        },
        defaultSortName: 'name',
        defaultSortOrder: 'asc',
        onSortChange,
        onPageChange,
        onSizePerPageList,
        onSearchChange,
        sizePerPage: pageSize,
        page: currentPage
    };

    const selectRow = {
        mode: 'checkbox',
        clickToSelect: true,
        bgColor(row, isSelect) {
            return isSelect && '#e8e8e8';
        }
    };

    const createColumns = () => {
        const columns = [];
        columns.push(<TableHeaderColumn dataField="id" hidden isKey>Id</TableHeaderColumn>);
        columns.push(...children);
        columns.push(createEnabledColumnHeader());
        columns.push(editPageUrl && createIconTableHeader(editColumnFormatter(), 'Edit'));
        columns.push(copyPageUrl && createIconTableHeader(copyColumnFormatter(), 'Copy'));

        return columns;
    };

    return (
        <CommonGlobalConfiguration
            label={label}
            description={description}
        >
            <div className="pull-right">
                <AutoRefresh startAutoReload={retrieveTableData} />
            </div>
            <ConfirmModal
                id="delete-confirm-modal"
                title="Delete"
                affirmativeAction={deleteTableData}
                affirmativeButtonText="Confirm"
                negativeAction={() => setShowDelete(false)}
                negativeButtonText="Cancel"
                showModal={showDelete}
            >
                Are you sure you want to delete these items?
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
                selectRow={selectRow}
                ref={tableRef}
                options={tableOptions}
                search
                pagination
                remote
                fetchInfo={tableFetchInfo}
            >
                {createColumns()}
            </BootstrapTable>
            <ProgressIcon inProgress={progress} />
        </CommonGlobalConfiguration>
    );
};

AzureBoardsTable.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired,
    tableData: PropTypes.array.isRequired,
    setTableData: PropTypes.func.isRequired,
    apiUrl: PropTypes.string.isRequired,
    editPageUrl: PropTypes.string,
    copyPageUrl: PropTypes.string,
    fieldKey: PropTypes.string,
    description: PropTypes.string,
    readonly: PropTypes.bool,
    displayDelete: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    includeEnabled: PropTypes.bool
};

AzureBoardsTable.defaultProps = {
    fieldKey: '',
    description: '',
    editPageUrl: null,
    copyPageUrl: null,
    showRefreshButton: false,
    readonly: false,
    displayDelete: true,
    includeEnabled: true
};

export default AzureBoardsTable;
