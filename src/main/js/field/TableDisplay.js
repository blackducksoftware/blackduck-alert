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

        this.state = {
            currentRowSelected: null,
            uiValidation: VALIDATION_STATE.NONE,
            showConfiguration: false,
            showDelete: false,
            rowsToDelete: []
        };
    }

    componentDidMount() {
        this.updateData();
    }

    componentDidUpdate(prevProps, prevState) {
        if (!this.state.showConfiguration && this.state.currentRowSelected && !this.props.inProgress && !this.props.hasFieldErrors && this.state.uiValidation === VALIDATION_STATE.SUCCESS) {
            this.handleClose();
        }
    }

    createTableColumns() {
        const defaultDataFormat = (cell) => {
            if (cell) {
                return <div title={cell.toString()}> {cell} </div>;
            }
            return <div> {cell} </div>;
        };

        return this.props.columns.map(column => {

            const assignedDataFormate = column['dataFormat'] ? column['dataFormat'] : defaultDataFormat;
            const searchable = column['searchable'] ? column['searchable'] : true;
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
                    dataFormat={assignedDataFormate}
                >{column.headerLabel}
                </TableHeaderColumn>
            )
        });
    }

    updateData() {
        this.props.refreshData();
    }

    createButtonGroup(buttons) {
        const classes = 'btn btn-md btn-info react-bs-table-add-btn tableButton';
        const insertOnClick = buttons.insertBtn ? buttons.insertBtn.props.onClick : null;
        const deleteOnClick = buttons.deleteBtn ? buttons.deleteBtn.props.onClick : null;
        const refreshButton = !this.props.autoRefresh && (
            <button type="button" className={classes} onClick={this.updateData}>
                <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />Refresh
            </button>
        );
        return (
            <div>
                {buttons.insertBtn
                && <InsertButton
                    className="addJobButton btn-md"
                    onClick={() => {
                        insertOnClick();
                        this.props.clearModalFieldState();
                        this.setState({
                            showConfiguration: true
                        });
                    }}
                >
                    <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                    {this.props.tableNewButtonLabel}
                </InsertButton>
                }
                {buttons.deleteBtn
                && <DeleteButton className="deleteJobButton btn-md" onClick={deleteOnClick}>
                    <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                    {this.props.tableDeleteButtonLabel}
                </DeleteButton>
                }
                {this.props.tableRefresh && refreshButton}
            </div>
        );
    }

    handleClose() {
        const stateCallback = () => {
            const closeCallback = () => {
                this.refs.table.cleanSelected();
                this.updateData();
            };
            this.props.onConfigClose(closeCallback);
        }
        this.setState({
            currentRowSelected: null
        }, stateCallback);
    }

    handleInsertModalSubmit(event, onModalClose) {
        if (event) {
            event.preventDefault()
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
            event.preventDefault()
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
        if (event) {
            event.preventDefault()
            event.stopPropagation();
        }
        const callback = (result) => {
            const validationState = result ? VALIDATION_STATE.SUCCESS : VALIDATION_STATE.FAILED;
            const validationSetCallback = () => this.setState({
                showConfiguration: false
            }, this.updateData);
            this.setState({
                uiValidation: validationState
            }, validationSetCallback);
        };
        this.props.onConfigSave(callback);
    }

    handleTest(event) {
        if (event) {
            event.preventDefault()
            event.stopPropagation();
        }
        const callback = (result) => {
            const validationState = result ? VALIDATION_STATE.SUCCESS : VALIDATION_STATE.FAILED;
            this.setState({
                uiValidation: validationState
            });
        };
        this.props.onConfigTest(callback);
    }

    createEditModal() {
        const { currentRowSelected } = this.state;
        const { modalTitle, newConfigFields, inProgress, saveButton, testButton, testButtonLabel, errorDialogMessage, actionMessage } = this.props;
        const popupActionMessage = errorDialogMessage ? errorDialogMessage : actionMessage;
        return (
            <div
                onKeyDown={e => e.stopPropagation()}
                onClick={e => e.stopPropagation()}
                onFocus={e => e.stopPropagation()}
                onMouseOver={e => e.stopPropagation()}
            >
                <PopUp
                    onCancel={() => {
                        this.hideModal();
                        this.handleClose();
                    }}
                    handleSubmit={this.handleSubmit}
                    includeSave={saveButton}
                    handleTest={this.handleTest}
                    testLabel={testButtonLabel}
                    includeTest={testButton}
                    show={this.isShowModal()}
                    title={modalTitle}
                    okLabel="Save"
                    performingAction={inProgress}
                    actionMessage={popupActionMessage}
                >
                    {newConfigFields(currentRowSelected)}
                </PopUp>
            </div>
        );
    }

    isShowModal() {
        return this.state.showConfiguration || this.props.hasFieldErrors;
    }

    hideModal() {
        this.setState({
            showConfiguration: false
        });
    }

    createInsertModal(onModalClose) {
        const { showConfiguration } = this.state;
        const { modalTitle, newConfigFields, inProgress, saveButton, testButton, errorDialogMessage, actionMessage, testButtonLabel } = this.props;
        const popupActionMessage = errorDialogMessage ? errorDialogMessage : actionMessage;
        return (
            <div
                onKeyDown={e => e.stopPropagation()}
                onClick={e => e.stopPropagation()}
                onFocus={e => e.stopPropagation()}
                onMouseOver={e => e.stopPropagation()}
            >
                <PopUp
                    onCancel={() => {
                        onModalClose();
                        this.hideModal();
                        this.handleClose();
                    }}
                    handleSubmit={(event) => {
                        this.handleInsertModalSubmit(event, onModalClose);
                    }}
                    includeSave={saveButton}
                    handleTest={(event) => {
                        this.handleInsertModalTest(event, onModalClose);
                    }}
                    includeTest={testButton}
                    testLabel={testButtonLabel}
                    show={showConfiguration}
                    title={modalTitle}
                    okLabel="Save"
                    performingAction={inProgress}
                    actionMessage={popupActionMessage}
                >
                    {newConfigFields()}
                </PopUp>
            </div>
        );
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
        this.props.onConfigDelete(this.state.rowsToDelete, this.closeDeleteModal);
    }

    editButtonClicked(currentRowSelected) {
        this.props.clearModalFieldState();
        const callback = () => this.setState({
            currentRowSelected,
            showConfiguration: true
        });
        this.props.onEditState(currentRowSelected, callback);
    }

    editButtonClick(cell, row) {
        return (<IconTableCellFormatter
            handleButtonClicked={this.editButtonClicked}
            currentRowSelected={row}
            buttonIconName="pencil-alt"
            buttonText="Edit"
        />);
    }

    copyButtonClicked(currentRowSelected) {
        const callback = () => this.setState({
            currentRowSelected,
            showConfiguration: true
        });
        this.props.onConfigCopy(currentRowSelected, callback);
    }

    copyButtonClick(cell, row) {
        return (<IconTableCellFormatter
            handleButtonClicked={this.copyButtonClicked}
            currentRowSelected={row}
            buttonIconName="copy"
            buttonText="Copy"
        />);
    }

    render() {
        const tableColumns = this.createTableColumns();
        if (this.props.enableEdit) {
            tableColumns.push(<TableHeaderColumn
                dataField=""
                width="48"
                columnClassName="tableCell"
                dataFormat={this.editButtonClick}
                thStyle={{ textAlign: 'center' }}
            >Edit</TableHeaderColumn>);
        }
        if (this.props.enableCopy) {
            tableColumns.push(<TableHeaderColumn
                dataField=""
                width="48"
                columnClassName="tableCell"
                dataFormat={this.copyButtonClick}
                thStyle={{ textAlign: 'center' }}
            >Copy</TableHeaderColumn>);
        }

        const {
            selectRowBox, sortName, sortOrder, autoRefresh, tableMessage, newButton, deleteButton, data, tableSearchable, inProgress
        } = this.props;

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
        const deleteModal = (<ConfirmModal title="Delete"
                                           affirmativeAction={this.deleteItems}
                                           affirmativeButtonText="Confirm"
                                           negativeAction={this.closeDeleteModal}
                                           negativeButtonText="Cancel"
                                           message="Are you sure you want to delete these items?"
                                           showModal={this.state.showDelete} />
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
                    ref="table"
                >
                    {tableColumns}
                </BootstrapTable>

                {this.props.inProgress &&
                <div className="progressIcon">
                    <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                </div>
                }

                <p name="tableMessage">{tableMessage}</p>
            </div>
        );

        const refresh = this.props.tableRefresh && (
            <div className="pull-right">
                <AutoRefresh startAutoReload={this.props.refreshData} autoRefresh={autoRefresh} />
            </div>
        );

        return (
            <div>

                {this.createEditModal()}
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
    testButtonLabel: "Test Configuration"
};

const mapStateToProps = state => ({
    autoRefresh: state.refresh.autoRefresh
});

export default connect(mapStateToProps, null)(TableDisplay);
