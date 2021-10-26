import React, { useEffect, useRef, useState } from 'react';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useHistory } from 'react-router-dom';
import * as PropTypes from 'prop-types';
import AutoRefresh from 'common/table/AutoRefresh';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import { BLACKDUCK_GLOBAL_FIELD_KEYS, BLACKDUCK_INFO, BLACKDUCK_URLS } from 'page/provider/blackduck/BlackDuckModel';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import ConfirmModal from 'common/ConfirmModal';
import IconTableCellFormatter from 'common/table/IconTableCellFormatter';
import { ProgressIcon } from 'common/table/ProgressIcon';

const BlackDuckProviderTable = ({
    csrfToken, readonly, showRefreshButton, displayDelete
}) => {
    const [tableData, setTableData] = useState([]);
    const [showDelete, setShowDelete] = useState(false);
    const [allSelectedRows, setAllSelectedRows] = useState([]);
    const [progress, setProgress] = useState(false);
    const tableRef = useRef();
    const history = useHistory();

    const readRequest = () => ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, BLACKDUCK_INFO.key);
    const deleteRequest = (id) => ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id);

    const retrieveTableData = async () => {
        setProgress(true);
        const response = await readRequest();
        const data = await response.json();

        const { fieldModels } = data;
        const filteredFieldModels = (fieldModels) ? fieldModels.filter((model) => FieldModelUtilities.hasAnyValuesExcludingId(model)) : [];
        const convertedTableData = filteredFieldModels.map((fieldModel) => ({
            id: FieldModelUtilities.getFieldModelId(fieldModel),
            name: FieldModelUtilities.getFieldModelSingleValue(fieldModel, BLACKDUCK_GLOBAL_FIELD_KEYS.name),
            enabled: FieldModelUtilities.getFieldModelBooleanValue(fieldModel, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled),
            lastUpdated: fieldModel.lastUpdated,
            createdAt: fieldModel.createdAt
        }));
        setTableData(convertedTableData);
        setProgress(false);
    };

    const deleteTableData = async () => {
        setProgress(true);
        if (allSelectedRows) {
            allSelectedRows.forEach((configId) => {
                deleteRequest(configId);
            });
        }
        await retrieveTableData();
        setShowDelete(false);
        setProgress(false);
    };

    useEffect(() => {
        retrieveTableData();
    }, [showDelete]);

    const navigateToConfigPage = (id, copy) => {
        const url = (copy) ? BLACKDUCK_URLS.blackDuckConfigCopyUrl : BLACKDUCK_URLS.blackDuckConfigUrl;
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
                { !readonly
                    && (
                        <InsertButton
                            id="blackduck-insert-button"
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
                            id="blackduck-delete-button"
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
                            id="blackduck-refresh-button"
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

    const column = (header, value) => (
        <TableHeaderColumn
            key={header}
            dataField={header}
            searchable
            dataSort
            columnClassName="tableCell"
            tdStyle={{ whiteSpace: 'normal' }}
            dataFormat={assignedDataFormat}
        >
            {value}
        </TableHeaderColumn>
    );

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

    const createTableCellFormatter = (iconName, buttonText, clickFunction) => {
        const buttonId = buttonText.toLowerCase();
        return (cell, row) => (
            <IconTableCellFormatter
                id={`blackduck-${buttonId}-cell`}
                handleButtonClicked={clickFunction}
                currentRowSelected={row}
                buttonIconName={iconName}
                buttonText={buttonText}
            />
        );
    };

    const editButtonClicked = ({ id }) => {
        navigateToConfigPage(id);
    };

    const copyButtonClicked = ({ id }) => {
        navigateToConfigPage(id, true);
    };

    const editColumnFormatter = () => createTableCellFormatter('pencil-alt', 'Edit', editButtonClicked);
    const copyColumnFormatter = () => createTableCellFormatter('copy', 'Copy', copyButtonClicked);

    const createIconTableHeader = (dataFormat, text) => (
        <TableHeaderColumn
            key={`${text}Key`}
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

    const tableOptions = {
        btnGroup: insertAndDeleteButton,
        noDataText: 'No Data',
        clearSearch: true,
        handleConfirmDeleteRow: (next, rows) => setAllSelectedRows(rows),
        defaultSortName: 'name',
        defaultSortOrder: 'asc',
        onRowDoubleClick: (id) => {
            editButtonClicked(id);
        }
    };

    return (
        <div>
            <div className="pull-right">
                <AutoRefresh startAutoReload={retrieveTableData} />
            </div>
            <ConfirmModal
                id="blackduck-delete-confirm-modal"
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
            >
                <TableHeaderColumn dataField="id" hidden isKey>Id</TableHeaderColumn>
                {column('name', 'Name')}
                {column('createdAt', 'Created At')}
                {column('lastUpdated', 'Last Updated')}
                <TableHeaderColumn
                    dataField="enabled"
                    width="70"
                    columnClassName="tableCell"
                    dataFormat={editFormat}
                    thStyle={{ textAlign: 'center' }}
                    tdStyle={{ textAlign: 'center' }}
                >
                    Enabled
                </TableHeaderColumn>
                {createIconTableHeader(editColumnFormatter(), 'Edit')}
                {createIconTableHeader(copyColumnFormatter(), 'Copy')}
            </BootstrapTable>
            <ProgressIcon inProgress={progress} />
        </div>
    );
};

BlackDuckProviderTable.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    displayDelete: PropTypes.bool
};

BlackDuckProviderTable.defaultProps = {
    readonly: false,
    showRefreshButton: false,
    displayDelete: true
};

export default BlackDuckProviderTable;
