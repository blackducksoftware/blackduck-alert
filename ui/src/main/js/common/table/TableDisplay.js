import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import AutoRefresh from 'common/table/AutoRefresh';
import IconTableCellFormatter from 'common/table/IconTableCellFormatter';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import PopUp from 'common/PopUp';
import ConfirmModal from 'common/ConfirmModal';
import StatusMessage from 'common/StatusMessage';

const VALIDATION_STATE = {
    NONE: 'NONE',
    SUCCESS: 'SUCCESS',
    FAILED: 'FAILED'
};

const TableDisplay = ({
    id,
    actionMessage,
    autoRefresh,
    clearModalFieldState,
    columns,
    copyColumnIcon,
    copyColumnText,
    data,
    deleteButton,
    editColumnIcon,
    editColumnText,
    enableCopy,
    enableEdit,
    errorDialogMessage,
    errorIsDetailed,
    inProgress,
    hasFieldErrors,
    ignoredActionMessages,
    modalTitle,
    nestedInAnotherModal,
    newButton,
    newConfigFields,
    onConfigClose,
    onConfigCopy,
    onConfigDelete,
    onConfigSave,
    onConfigTest,
    onEditState,
    refreshData,
    saveButton,
    sortName,
    sortOrder,
    selectRowBox,
    tableNewButtonLabel,
    tableDeleteButtonLabel,
    tableRefresh,
    tableSearchable,
    testButton,
    testButtonLabel
}) => {
    const [currentRowSelected, setCurrentRowSelected] = useState(null);
    const [uiValidation, setUiValidation] = useState(VALIDATION_STATE.NONE);
    const [showConfiguration, setShowConfiguration] = useState(false);
    const [isInsertModal, setIsInsertModal] = useState(false);
    const [showDelete, setShowDelete] = useState(false);
    const [rowsToDelete, setRowsToDelete] = useState([]);
    const [handleSubmitCalled, setHandleSubmitCalled] = useState(false);
    const [handleCloseCalled, setHandleCloseCalled] = useState(false);
    const modalCloseCallback = useRef(null);
    const table = useRef(null);

    const updateData = () => {
        refreshData();
    };

    const handleClose = () => {
        setCurrentRowSelected(null);
        setHandleCloseCalled(true);
    };

    useEffect(() => {
        updateData();
    }, []);

    useEffect(() => {
        if (!showConfiguration && currentRowSelected && !inProgress && !hasFieldErrors
            && uiValidation === VALIDATION_STATE.SUCCESS) {
            handleClose();
        }
    }, [showConfiguration, currentRowSelected, uiValidation, inProgress, hasFieldErrors]);

    useEffect(() => {
        if (handleSubmitCalled) {
            setShowConfiguration(uiValidation !== VALIDATION_STATE.SUCCESS);
            if (uiValidation !== VALIDATION_STATE.FAILED) {
                updateData();
            }
            setHandleSubmitCalled(false);
        }
    }, [handleSubmitCalled, uiValidation]);

    useEffect(() => {
        if (handleCloseCalled) {
            const closeCallback = () => {
                table.current.cleanSelected();
                updateData();
            };
            setHandleCloseCalled(false);
            onConfigClose(closeCallback);
        }
    }, [handleCloseCalled]);

    const isShowModal = () => showConfiguration || hasFieldErrors;

    const hideModal = () => {
        setShowConfiguration(false);
        setIsInsertModal(false);
    };

    const onAutoRefresh = () => {
        if (!showConfiguration) {
            refreshData();
        }
    };

    const handleSubmit = (event) => {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        const callback = (result) => {
            const validationState = result ? VALIDATION_STATE.SUCCESS : VALIDATION_STATE.FAILED;
            setUiValidation(validationState);
            setHandleSubmitCalled(true);
        };
        onConfigSave(callback);
    };

    const handleTest = (event) => {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        const callback = (result) => {
            const validationState = result ? VALIDATION_STATE.SUCCESS : VALIDATION_STATE.FAILED;
            setUiValidation(validationState);
        };
        onConfigTest(callback);
    };

    const handleCancel = () => {
        hideModal();
        handleClose();
    };

    const createTableColumns = () => {
        const defaultDataFormat = (cell) => {
            if (cell) {
                return (
                    <div title={cell.toString()}>
                        {cell}
                    </div>
                );
            }
            return (
                <div>
                    {cell}
                </div>
            );
        };

        return columns.map((column) => {
            const assignedDataFormat = column.dataFormat ? column.dataFormat : defaultDataFormat;
            const searchable = Object.prototype.hasOwnProperty.call(column, 'searchable') ? column.searchable : true;
            return (
                <TableHeaderColumn
                    key={column.header}
                    dataField={column.header}
                    isKey={column.isKey}
                    hidden={column.hidden}
                    searchable={searchable}
                    dataSort
                    columnClassName="tableCell"
                    tdStyle={{ whiteSpace: 'normal' }}
                    dataFormat={assignedDataFormat}
                >
                    {column.headerLabel}
                </TableHeaderColumn>
            );
        });
    };

    const createButtonGroup = (buttons) => {
        const classes = 'btn btn-md btn-info react-bs-table-add-btn tableButton';
        const insertOnClick = buttons.insertBtn ? buttons.insertBtn.props.onClick : null;
        const deleteOnClick = buttons.deleteBtn ? buttons.deleteBtn.props.onClick : null;
        const refreshButton = !autoRefresh && (
            <button id={`${id}-refresh-button`} type="button" className={classes} onClick={updateData}>
                <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />
                Refresh
            </button>
        );
        return (
            <div>
                {buttons.insertBtn
                && (
                    <InsertButton
                        id={`${id}-insert-button`}
                        className="addJobButton btn-md"
                        onClick={() => {
                            insertOnClick();
                            clearModalFieldState();
                            setShowConfiguration(true);
                            setIsInsertModal(true);
                        }}
                    >
                        <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                        {tableNewButtonLabel}
                    </InsertButton>
                )}
                {buttons.deleteBtn
                && (
                    <DeleteButton
                        id={`${id}-delete-button`}
                        className="deleteJobButton btn-md"
                        onClick={deleteOnClick}
                    >
                        <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                        {tableDeleteButtonLabel}
                    </DeleteButton>
                )}
                {tableRefresh && refreshButton}
            </div>
        );
    };

    const handleInsertModalSubmit = (event) => {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        // nested modals are not supported by react-bootstrap.
        // if this table is nested in a modal it cannot call onModalClose because it would close all modals.
        if (!nestedInAnotherModal) {
            const modalClose = modalCloseCallback.current;
            if (modalClose) {
                modalClose();
            }
        }
        handleSubmit();
    };

    const handleInsertModalTest = (event) => {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        // nested modals are not supported by react-bootstrap.
        // if this table is nested in a modal it cannot call onModalClose because it would close all modals.
        if (!nestedInAnotherModal) {
            const modalClose = modalCloseCallback.current;
            if (modalClose) {
                modalClose();
            }
        }
        handleTest();
    };

    const createTableModal = () => {
        // const tablePopupRef = tablePopup.current;
        // TODO have a better way of displaying action messages for the dialog versus the table. The ignoredActionMessages is to fix an issue with the provider table in a generic way.
        const popupActionMessage = (hasFieldErrors && errorDialogMessage) || (!ignoredActionMessages.includes(actionMessage) && actionMessage);
        const configFields = isInsertModal ? newConfigFields() : newConfigFields(currentRowSelected);
        let cancelFunction = handleCancel;
        let submitFunction = handleSubmit;
        let testFunction = handleTest;
        if (isInsertModal) {
            cancelFunction = () => {
                const modalClose = modalCloseCallback.current;
                if (modalClose) {
                    modalClose();
                }
                handleCancel();
                setIsInsertModal(false);
                modalCloseCallback.current = null;
            };
            submitFunction = handleInsertModalSubmit;
            testFunction = handleInsertModalTest;
        }
        return (
            <div>
                <PopUp
                    id={`${id}-popup`}
                    onKeyDown={(e) => e.stopPropagation()}
                    onClick={(e) => e.stopPropagation()}
                    onFocus={(e) => e.stopPropagation()}
                    onMouseOver={(e) => e.stopPropagation()}
                    onCancel={cancelFunction}
                    handleSubmit={submitFunction}
                    includeSave={saveButton}
                    handleTest={testFunction}
                    testLabel={testButtonLabel}
                    includeTest={testButton}
                    show={isShowModal()}
                    title={modalTitle}
                    okLabel="Save"
                    performingAction={inProgress}
                >
                    {configFields}
                    <StatusMessage
                        id={`${id}-status-message`}
                        actionMessage={popupActionMessage}
                        errorMessage={errorDialogMessage}
                        errorIsDetailed={errorIsDetailed}
                    />
                </PopUp>
            </div>
        );
    };

    const createInsertModal = (onModalClose) => {
        modalCloseCallback.current = onModalClose;
        return (<div />);
    };

    const collectItemsToDelete = (next, dropRowKeys) => {
        setRowsToDelete(dropRowKeys);
        setShowDelete(true);
    };

    const closeDeleteModal = () => {
        setRowsToDelete([]);
        setShowDelete(false);
        updateData();
    };

    const deleteItems = () => {
        onConfigDelete(rowsToDelete, closeDeleteModal);
        setRowsToDelete([]);
    };

    const editButtonClicked = (selectedRow) => {
        clearModalFieldState();
        const callback = () => {
            setCurrentRowSelected(selectedRow);
            setShowConfiguration(true);
        };
        onEditState(selectedRow, callback);
    };

    const createTableCellFormatter = (iconName, buttonText, clickFunction) => {
        const buttonId = buttonText.toLowerCase();
        return (cell, row) => (
            <IconTableCellFormatter
                id={`${id}-${buttonId}-cell`}
                handleButtonClicked={clickFunction}
                currentRowSelected={row}
                buttonIconName={iconName}
                buttonText={buttonText}
            />
        );
    };

    const editColumnFormatter = () => createTableCellFormatter(editColumnIcon, editColumnText, editButtonClicked);

    const copyButtonClicked = (selectedRow) => {
        const callback = () => {
            setCurrentRowSelected(selectedRow);
            setShowConfiguration(true);
        };
        onConfigCopy(selectedRow, callback);
    };

    const copyColumnFormatter = () => createTableCellFormatter(copyColumnIcon, copyColumnText, copyButtonClicked);

    const createIconTableHeader = (dataFormat, text) => (
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

    const tableColumns = createTableColumns();
    if (enableEdit) {
        const editColumn = createIconTableHeader(editColumnFormatter(), editColumnText);
        tableColumns.push(editColumn);
    }
    if (enableCopy) {
        const copyColumn = createIconTableHeader(copyColumnFormatter(), copyColumnText);
        tableColumns.push(copyColumn);
    }

    const emptyTableMessage = inProgress ? 'Loading...' : 'No Data';

    const tableOptions = {
        btnGroup: createButtonGroup,
        noDataText: emptyTableMessage,
        clearSearch: true,
        insertModal: createInsertModal,
        handleConfirmDeleteRow: collectItemsToDelete,
        defaultSortName: sortName,
        defaultSortOrder: sortOrder,
        onRowDoubleClick: editButtonClicked
    };

    const selectRow = selectRowBox && {
        mode: 'checkbox',
        clickToSelect: true,
        bgColor(row, isSelect) {
            return isSelect && '#e8e8e8';
        }
    };
    const deleteModal = (
        <ConfirmModal
            id={`${id}-delete-confirm-modal`}
            title="Delete"
            affirmativeAction={deleteItems}
            affirmativeButtonText="Confirm"
            negativeAction={closeDeleteModal}
            negativeButtonText="Cancel"
            showModal={showDelete}
        >
            <div>
                Are you sure you want to delete these items?
            </div>
        </ConfirmModal>
    );
    const progressIndicator = (
        <div className="progressIcon">
            <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
        </div>
    );
    const content = (
        <div>
            <BootstrapTable
                version="4"
                hover
                condensed
                data={data}
                containerClass="table"
                insertRow={newButton}
                deleteRow={deleteButton}
                selectRow={selectRow}
                options={tableOptions}
                search={tableSearchable}
                trClassName="tableRow"
                headerContainerClass="scrollable"
                bodyContainerClass="tableScrollableBody"
                ref={table}
            >
                {tableColumns}
            </BootstrapTable>
            {inProgress && progressIndicator}
        </div>
    );

    const shouldRefresh = !showConfiguration && !showDelete;

    const refresh = tableRefresh && (
        <div className="pull-right">
            <AutoRefresh startAutoReload={onAutoRefresh} autoRefresh={autoRefresh} isEnabled={shouldRefresh} />
        </div>
    );
    return (
        <div>
            {createTableModal()}
            {refresh}
            {deleteModal}
            {content}
        </div>
    );
};

TableDisplay.propTypes = {
    id: PropTypes.string,
    actionMessage: PropTypes.string,
    autoRefresh: PropTypes.bool,
    columns: PropTypes.arrayOf(PropTypes.shape({
        header: PropTypes.string.isRequired,
        headerLabel: PropTypes.string.isRequired,
        isKey: PropTypes.bool.isRequired,
        hidden: PropTypes.bool.isRequired
    })).isRequired,
    copyColumnIcon: PropTypes.string,
    copyColumnText: PropTypes.string,
    data: PropTypes.array,
    deleteButton: PropTypes.bool,
    editColumnText: PropTypes.string,
    editColumnIcon: PropTypes.string,
    enableCopy: PropTypes.bool,
    enableEdit: PropTypes.bool,
    errorDialogMessage: PropTypes.string,
    errorIsDetailed: PropTypes.bool,
    hasFieldErrors: PropTypes.bool,
    ignoredActionMessages: PropTypes.arrayOf(PropTypes.string),
    inProgress: PropTypes.bool,
    modalTitle: PropTypes.string,
    nestedInAnotherModal: PropTypes.bool,
    newButton: PropTypes.bool,
    newConfigFields: PropTypes.func.isRequired,
    onConfigClose: PropTypes.func,
    onConfigCopy: PropTypes.func,
    onConfigDelete: PropTypes.func,
    onConfigSave: PropTypes.func,
    onConfigTest: PropTypes.func,
    onEditState: PropTypes.func.isRequired,
    clearModalFieldState: PropTypes.func,
    refreshData: PropTypes.func.isRequired,
    sortName: PropTypes.string,
    sortOrder: PropTypes.string,
    saveButton: PropTypes.bool,
    selectRowBox: PropTypes.bool,
    tableDeleteButtonLabel: PropTypes.string,
    tableNewButtonLabel: PropTypes.string,
    tableRefresh: PropTypes.bool,
    tableSearchable: PropTypes.bool,
    testButton: PropTypes.bool,
    testButtonLabel: PropTypes.string
};

TableDisplay.defaultProps = {
    id: 'tableDisplayId',
    actionMessage: null,
    autoRefresh: true,
    clearModalFieldState: () => null,
    copyColumnText: 'Copy',
    copyColumnIcon: 'copy',
    data: [],
    deleteButton: true,
    editColumnIcon: 'pencil-alt',
    editColumnText: 'Edit',
    enableEdit: true,
    enableCopy: true,
    errorDialogMessage: null,
    errorIsDetailed: false,
    hasFieldErrors: false,
    ignoredActionMessages: [],
    inProgress: false,
    modalTitle: 'New',
    nestedInAnotherModal: false,
    newButton: true,
    onConfigClose: () => null,
    onConfigCopy: () => null,
    onConfigDelete: () => null,
    onConfigSave: () => true,
    onConfigTest: () => true,
    saveButton: true,
    selectRowBox: true,
    sortName: '',
    sortOrder: 'asc',
    tableNewButtonLabel: 'New',
    tableDeleteButtonLabel: 'Delete',
    tableRefresh: true,
    tableSearchable: true,
    testButton: false,
    testButtonLabel: 'Test Configuration'
};

export default TableDisplay;
