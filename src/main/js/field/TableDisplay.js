import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import AutoRefresh from 'component/common/AutoRefresh';
import IconTableCellFormatter from 'component/common/IconTableCellFormatter';
import { connect } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import PopUp from 'field/PopUp';
import ConfirmModal from 'component/common/ConfirmModal';

const VALIDATION_STATE = {
    NONE: 'NONE',
    SUCCESS: 'SUCCESS',
    FAILED: 'FAILED'
};

class TableDisplay extends Component {
    constructor(props) {
        super(props);

        this.createTableColumns = this.createTableColumns.bind(this);
        this.createButtonGroup = this.createButtonGroup.bind(this);
        this.createInsertModal = this.createInsertModal.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.updateData = this.updateData.bind(this);
        this.collectItemsToDelete = this.collectItemsToDelete.bind(this);
        this.closeDeleteModal = this.closeDeleteModal.bind(this);
        this.deleteItems = this.deleteItems.bind(this);
        this.editButtonClicked = this.editButtonClicked.bind(this);
        this.editButtonClick = this.editButtonClick.bind(this);
        this.copyButtonClicked = this.copyButtonClicked.bind(this);
        this.copyButtonClick = this.copyButtonClick.bind(this);
        this.isShowModal = this.isShowModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
        this.handleInsertModalSubmit = this.handleInsertModalSubmit.bind(this);
        this.handleInsertModalTest = this.handleInsertModalTest.bind(this);
        this.onAutoRefresh = this.onAutoRefresh.bind(this);
        this.tablePopup = React.createRef();
        this.table = React.createRef();
        this.state = {
            currentRowSelected: null,
            uiValidation: VALIDATION_STATE.NONE,
            showConfiguration: false,
            isInsertModal: false,
            showDelete: false,
            rowsToDelete: []
        };
    }

    componentDidMount() {
        this.updateData();
    }

    componentDidUpdate() {
        const { showConfiguration, currentRowSelected, uiValidation } = this.state;
        const { inProgress, hasFieldErrors } = this.props;
        if (!showConfiguration && currentRowSelected && !inProgress && !hasFieldErrors
            && uiValidation === VALIDATION_STATE.SUCCESS) {
            this.handleClose();
        }
    }

    onAutoRefresh() {
        const { refreshData } = this.props;
        const { showConfiguration } = this.state;
        if (!showConfiguration) {
            refreshData();
        }
    }

    createTableColumns() {
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

        const { columns } = this.props;
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
    }

    updateData() {
        const { refreshData } = this.props;
        refreshData();
    }

    createButtonGroup(buttons) {
        const {
            autoRefresh, clearModalFieldState, tableNewButtonLabel, tableDeleteButtonLabel, tableRefresh
        } = this.props;
        const classes = 'btn btn-md btn-info react-bs-table-add-btn tableButton';
        const insertOnClick = buttons.insertBtn ? buttons.insertBtn.props.onClick : null;
        const deleteOnClick = buttons.deleteBtn ? buttons.deleteBtn.props.onClick : null;
        const refreshButton = !autoRefresh && (
            <button type="button" className={classes} onClick={this.updateData}>
                <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />
                Refresh
            </button>
        );
        return (
            <div>
                {buttons.insertBtn
                && (
                    <InsertButton
                        className="addJobButton btn-md"
                        onClick={() => {
                            insertOnClick();
                            clearModalFieldState();
                            this.setState({
                                showConfiguration: true,
                                isInsertModal: true
                            });
                        }}
                    >
                        <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                        {tableNewButtonLabel}
                    </InsertButton>
                )}
                {buttons.deleteBtn
                && (
                    <DeleteButton className="deleteJobButton btn-md" onClick={deleteOnClick}>
                        <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                        {tableDeleteButtonLabel}
                    </DeleteButton>
                )}
                {tableRefresh && refreshButton}
            </div>
        );
    }

    handleClose() {
        const { onConfigClose } = this.props;
        const stateCallback = () => {
            const closeCallback = () => {
                this.table.current.cleanSelected();
                this.updateData();
            };
            onConfigClose(closeCallback);
        };
        this.setState({
            currentRowSelected: null
        }, stateCallback);
    }

    handleInsertModalSubmit(event, onModalClose) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        const { nestedInAnotherModal } = this.props;
        // nested modals are not supported by react-bootstrap.
        // if this table is nested in a modal it cannot call onModalClose because it would close all modals.
        if (!nestedInAnotherModal) {
            onModalClose();
        }
        this.handleSubmit();
    }

    handleInsertModalTest(event, onModalClose) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        const { nestedInAnotherModal } = this.props;
        // nested modals are not supported by react-bootstrap.
        // if this table is nested in a modal it cannot call onModalClose because it would close all modals.
        if (!nestedInAnotherModal) {
            onModalClose();
        }
        this.handleTest();
    }

    handleSubmit(event) {
        const { onConfigSave } = this.props;
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        const callback = (result) => {
            const validationState = result ? VALIDATION_STATE.FAILED : VALIDATION_STATE.SUCCESS;
            const validationSetCallback = () => this.setState({
                showConfiguration: false
            }, () => {
                if (validationState !== VALIDATION_STATE.FAILED) {
                    this.updateData();
                }
            });
            this.setState({
                uiValidation: validationState
            }, validationSetCallback);
        };
        onConfigSave(callback);
    }

    handleTest(event) {
        const { onConfigTest } = this.props;
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        const callback = (result) => {
            const validationState = result ? VALIDATION_STATE.SUCCESS : VALIDATION_STATE.FAILED;
            this.setState({
                uiValidation: validationState
            });
        };
        onConfigTest(callback);
    }

    handleCancel() {
        this.hideModal();
        this.handleClose();
    }

    createTableModal() {
        const tablePopupRef = this.tablePopup.current;
        const { currentRowSelected, isInsertModal } = this.state;
        const {
            modalTitle, newConfigFields, inProgress, saveButton, testButton, testButtonLabel, errorDialogMessage,
            actionMessage
        } = this.props;
        const popupActionMessage = errorDialogMessage || actionMessage;
        const configFields = isInsertModal ? newConfigFields() : newConfigFields(currentRowSelected);
        let cancelFunction = this.handleCancel;
        let submitFunction = this.handleSubmit;
        let testFunction = this.handleTest;
        if (isInsertModal) {
            cancelFunction = tablePopupRef && tablePopupRef.onCancel;
            submitFunction = tablePopupRef && tablePopupRef.handleSubmit;
            testFunction = tablePopupRef && tablePopupRef.handleTest;
        }
        return (
            <div>
                <PopUp
                    onKeyDown={(e) => e.stopPropagation()}
                    onClick={(e) => e.stopPropagation()}
                    onFocus={(e) => e.stopPropagation()}
                    onMouseOver={(e) => e.stopPropagation()}
                    ref={this.tablePopup}
                    onCancel={cancelFunction}
                    handleSubmit={submitFunction}
                    includeSave={saveButton}
                    handleTest={testFunction}
                    testLabel={testButtonLabel}
                    includeTest={testButton}
                    show={this.isShowModal()}
                    title={modalTitle}
                    okLabel="Save"
                    performingAction={inProgress}
                    actionMessage={popupActionMessage}
                >
                    {configFields}
                </PopUp>
            </div>
        );
    }

    isShowModal() {
        const { showConfiguration } = this.state;
        const { hasFieldErrors } = this.props;
        return showConfiguration || hasFieldErrors;
    }

    hideModal() {
        this.setState({
            showConfiguration: false,
            isInsertModal: false
        });
    }

    createInsertModal(onModalClose) {
        const cancelFunction = () => {
            onModalClose();
            this.handleCancel();
        };
        const submitFunction = (event) => {
            this.handleInsertModalSubmit(event, onModalClose);
        };
        const testFunction = (event) => {
            this.handleInsertModalTest(event, onModalClose);
        };
        if (this.tablePopup && this.tablePopup.current) {
            this.tablePopup.current.onCancel = cancelFunction;
            this.tablePopup.current.handleSubmit = submitFunction;
            this.tablePopup.current.handleTest = testFunction;
        }
        return (<div />);
    }

    collectItemsToDelete(next, dropRowKeys) {
        this.setState({
            rowsToDelete: dropRowKeys,
            showDelete: true
        });
    }

    closeDeleteModal() {
        this.setState({
            rowsToDelete: [],
            showDelete: false
        }, this.updateData);
    }

    deleteItems() {
        const { rowsToDelete } = this.state;
        const { onConfigDelete } = this.props;
        onConfigDelete(rowsToDelete, this.closeDeleteModal);
    }

    editButtonClicked(currentRowSelected) {
        const { clearModalFieldState, onEditState } = this.props;
        clearModalFieldState();
        const callback = () => this.setState({
            currentRowSelected,
            showConfiguration: true
        });
        onEditState(currentRowSelected, callback);
    }

    editButtonClick(cell, row) {
        return (
            <IconTableCellFormatter
                handleButtonClicked={this.editButtonClicked}
                currentRowSelected={row}
                buttonIconName="pencil-alt"
                buttonText="Edit"
            />
        );
    }

    copyButtonClicked(currentRowSelected) {
        const { onConfigCopy } = this.props;
        const callback = () => this.setState({
            currentRowSelected,
            showConfiguration: true
        });
        onConfigCopy(currentRowSelected, callback);
    }

    copyButtonClick(cell, row) {
        return (
            <IconTableCellFormatter
                handleButtonClicked={this.copyButtonClicked}
                currentRowSelected={row}
                buttonIconName="copy"
                buttonText="Copy"
            />
        );
    }

    createIconTableHeader(dataFormat, text) {
        return (
            <TableHeaderColumn
                dataField=""
                width="48"
                columnClassName="tableCell"
                dataFormat={dataFormat}
                thStyle={{ textAlign: 'center' }}
            >
                {text}
            </TableHeaderColumn>
        );
    }

    render() {
        const tableColumns = this.createTableColumns();
        const { showDelete } = this.state;
        const {
            selectRowBox, sortName, sortOrder, autoRefresh, tableMessage, newButton, deleteButton, data,
            tableSearchable, enableEdit, enableCopy, inProgress, tableRefresh
        } = this.props;
        if (enableEdit) {
            const editColumn = this.createIconTableHeader(this.editButtonClick, 'Edit');
            tableColumns.push(editColumn);
        }
        if (enableCopy) {
            const copyColumn = this.createIconTableHeader(this.copyButtonClick, 'Copy');
            tableColumns.push(copyColumn);
        }

        const emptyTableMessage = inProgress ? 'Loading...' : 'No Data';

        const tableOptions = {
            btnGroup: this.createButtonGroup,
            noDataText: emptyTableMessage,
            clearSearch: true,
            insertModal: this.createInsertModal,
            handleConfirmDeleteRow: this.collectItemsToDelete,
            defaultSortName: sortName,
            defaultSortOrder: sortOrder,
            onRowDoubleClick: this.editButtonClicked
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
                title="Delete"
                affirmativeAction={this.deleteItems}
                affirmativeButtonText="Confirm"
                negativeAction={this.closeDeleteModal}
                negativeButtonText="Cancel"
                message="Are you sure you want to delete these items?"
                showModal={showDelete}
            />
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
                    ref={this.table}
                >
                    {tableColumns}
                </BootstrapTable>

                {inProgress && progressIndicator}

                <p id="tableMessage">{tableMessage}</p>
            </div>
        );

        const refresh = tableRefresh && (
            <div className="pull-right">
                <AutoRefresh startAutoReload={this.onAutoRefresh} autoRefresh={autoRefresh} />
            </div>
        );

        return (
            <div>
                {this.createTableModal()}
                {refresh}
                {deleteModal}
                {content}
            </div>
        );
    }
}

TableDisplay.propTypes = {
    refreshData: PropTypes.func.isRequired,
    data: PropTypes.array,
    columns: PropTypes.arrayOf(PropTypes.shape({
        header: PropTypes.string.isRequired,
        headerLabel: PropTypes.string.isRequired,
        isKey: PropTypes.bool.isRequired,
        hidden: PropTypes.bool.isRequired
    })).isRequired,
    newConfigFields: PropTypes.func.isRequired,
    onEditState: PropTypes.func.isRequired,
    onConfigSave: PropTypes.func,
    onConfigTest: PropTypes.func,
    onConfigDelete: PropTypes.func,
    onConfigClose: PropTypes.func,
    onConfigCopy: PropTypes.func,
    clearModalFieldState: PropTypes.func,
    sortName: PropTypes.string,
    sortOrder: PropTypes.string,
    selectRowBox: PropTypes.bool,
    tableMessage: PropTypes.string,
    autoRefresh: PropTypes.bool,
    newButton: PropTypes.bool,
    deleteButton: PropTypes.bool,
    saveButton: PropTypes.bool,
    testButton: PropTypes.bool,
    inProgress: PropTypes.bool,
    fetching: PropTypes.bool,
    modalTitle: PropTypes.string,
    tableNewButtonLabel: PropTypes.string,
    tableDeleteButtonLabel: PropTypes.string,
    tableSearchable: PropTypes.bool,
    tableRefresh: PropTypes.bool,
    hasFieldErrors: PropTypes.bool,
    errorDialogMessage: PropTypes.string,
    actionMessage: PropTypes.string,
    nestedInAnotherModal: PropTypes.bool,
    enableEdit: PropTypes.bool,
    enableCopy: PropTypes.bool,
    testButtonLabel: PropTypes.string
};

TableDisplay.defaultProps = {
    data: [],
    sortName: '',
    sortOrder: 'asc',
    selectRowBox: true,
    tableMessage: '',
    autoRefresh: true,
    newButton: true,
    deleteButton: true,
    saveButton: true,
    testButton: false,
    inProgress: false,
    fetching: false,
    onConfigSave: () => true,
    onConfigTest: () => true,
    onConfigDelete: () => null,
    onConfigClose: () => null,
    onConfigCopy: () => null,
    clearModalFieldState: () => null,
    modalTitle: 'New',
    tableNewButtonLabel: 'New',
    tableDeleteButtonLabel: 'Delete',
    tableSearchable: true,
    tableRefresh: true,
    hasFieldErrors: false,
    errorDialogMessage: null,
    actionMessage: null,
    nestedInAnotherModal: false,
    enableEdit: true,
    enableCopy: true,
    testButtonLabel: 'Test Configuration'
};

const mapStateToProps = (state) => ({
    autoRefresh: state.refresh.autoRefresh
});

export default connect(mapStateToProps, null)(TableDisplay);
