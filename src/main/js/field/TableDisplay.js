import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import AutoRefresh from 'component/common/AutoRefresh';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'component/common/ConfigButtons';
import IconTableCellFormatter from 'component/common/IconTableCellFormatter';
import { connect } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import PopUp from 'field/PopUp';

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
        this.updateData = this.updateData.bind(this);
        this.collectItemsToDelete = this.collectItemsToDelete.bind(this);
        this.closeDeleteModal = this.closeDeleteModal.bind(this);
        this.flipDeleteModalShowFlag = this.flipDeleteModalShowFlag.bind(this);
        this.deleteItems = this.deleteItems.bind(this);
        this.editButtonClicked = this.editButtonClicked.bind(this);
        this.editButtonClick = this.editButtonClick.bind(this);
        this.copyButtonClicked = this.copyButtonClicked.bind(this);
        this.copyButtonClick = this.copyButtonClick.bind(this);
        this.isShowModal = this.isShowModal.bind(this);
        this.createErrorModal = this.createErrorModal.bind(this);

        this.state = {
            currentRowSelected: null,
            uiValidation: VALIDATION_STATE.NONE,
            showConfiguration: false,
            showDelete: false,
            rowsToDelete: [],
            showErrorDialog: Boolean(this.props.errorDialogMessage)
        };
    }

    componentDidMount() {
        this.updateData();
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevProps.errorDialogMessage !== this.props.errorDialogMessage) {
            this.setState({
                showErrorDialog: Boolean(this.props.errorDialogMessage)
            });
        }
        console.log(`modalTitle: ${this.props.modalTitle} showConfig: ${this.state.showConfiguration} currentRow: ${this.state.currentRowSelected} previous inProgress: ${prevProps.inProgress} current inProgress; ${this.props.inProgress} fieldErrors: ${this.props.hasFieldErrors} uiValidation: ${this.state.uiValidation}`);
        if ((this.state.showConfiguration || this.state.currentRowSelected) && prevProps.inProgress && !this.props.inProgress && !this.props.hasFieldErrors && this.state.uiValidation === VALIDATION_STATE.SUCCESS) {
            this.handleClose();
            this.setState({
                showConfiguration: false
            });
        }
    }

    createTableColumns() {
        const assignDataFormat = (cell) => {
            if (cell) {
                return <div title={cell.toString()}> {cell} </div>;
            }
            return <div> {cell} </div>;
        };

        return this.props.columns.map(column => (
            <TableHeaderColumn
                key={column.header}
                dataField={column.header}
                isKey={column.isKey}
                hidden={column.hidden}
                dataSort
                columnClassName="tableCell"
                tdStyle={{ whiteSpace: 'normal' }}
                dataFormat={assignDataFormat}
            >{column.headerLabel}
            </TableHeaderColumn>
        ));
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
        this.setState({
            showConfiguration: false
        });
        this.props.onConfigClose();
        this.refs.table.cleanSelected();
        this.setState({
            currentRowSelected: null
        });
        this.updateData();
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const result = this.props.onConfigSave();
        console.log(`Table save result ${result}`);
        let validationState;
        if (result) {
            validationState = VALIDATION_STATE.SUCCESS;
        } else {
            validationState = VALIDATION_STATE.FAILED;
        }
        console.log(`Table submit state ${validationState}`);
        this.setState({
            uiValidation: validationState
        });
    }

    createEditModal() {
        const { currentRowSelected } = this.state;
        const { modalTitle, newConfigFields, inProgress } = this.props;
        const showModal = Boolean(currentRowSelected) || this.isShowModal();
        return (
            <div
                onKeyDown={e => e.stopPropagation()}
                onClick={e => e.stopPropagation()}
                onFocus={e => e.stopPropagation()}
                onMouseOver={e => e.stopPropagation()}
            >
                <PopUp
                    onCancel={this.handleClose}
                    handleSubmit={this.handleSubmit}
                    show={showModal}
                    title={modalTitle}
                    okLabel={'Save'}
                    performingAction={inProgress}
                >
                    {newConfigFields(currentRowSelected)}
                </PopUp>
            </div>
        );
    }

    isShowModal() {
        return this.state.showConfiguration || this.props.hasFieldErrors;
    }

    createInsertModal(onModalClose) {
        const { showConfiguration } = this.state;
        const { modalTitle, newConfigFields, inProgress } = this.props;
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
                        this.handleClose();
                    }}
                    handleSubmit={this.handleSubmit}
                    show={showConfiguration}
                    title={modalTitle}
                    okLabel="Save"
                    performingAction={inProgress}
                >
                    {newConfigFields()}
                </PopUp>
            </div>
        );
    }

    createErrorModal() {
        const { showErrorDialog } = this.state;
        const { errorDialogMessage } = this.props;

        return (
            <div
                onKeyDown={e => e.stopPropagation()}
                onClick={e => e.stopPropagation()}
                onFocus={e => e.stopPropagation()}
                onMouseOver={e => e.stopPropagation()}
            >
                <Modal
                    size="lg"
                    show={showErrorDialog}
                    onHide={() => {
                        this.handleClose();
                        this.setState({
                            showErrorDialog: false
                        });
                    }}
                >
                    <Modal.Header closeButton>
                        <Modal.Title>Error</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <div>{errorDialogMessage}</div>
                    </Modal.Body>
                </Modal>
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
        this.flipDeleteModalShowFlag();
        this.setState({
            rowsToDelete: []
        });
    }

    flipDeleteModalShowFlag() {
        this.setState({
            showDelete: !this.state.showDelete
        });
    }

    deleteItems(event) {
        event.preventDefault();
        event.stopPropagation();
        this.props.onConfigDelete(this.state.rowsToDelete);
        this.closeDeleteModal();
    }

    editButtonClicked(currentRowSelected) {
        this.props.clearModalFieldState();
        this.props.editState(currentRowSelected);
        this.setState({
            currentRowSelected
        });
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
        currentRowSelected.id = null;
        this.props.editState(currentRowSelected);
        this.setState({
            currentRowSelected
        });
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
        tableColumns.push(<TableHeaderColumn
            dataField=""
            width="48"
            columnClassName="tableCell"
            dataFormat={this.editButtonClick}
            thStyle={{ textAlign: 'center' }}
        >Edit
        </TableHeaderColumn>);
        tableColumns.push(<TableHeaderColumn
            dataField=""
            width="48"
            columnClassName="tableCell"
            dataFormat={this.copyButtonClick}
            thStyle={{ textAlign: 'center' }}
        >Copy
        </TableHeaderColumn>);

        const {
            selectRowBox, sortName, sortOrder, autoRefresh, tableMessage, newButton, deleteButton, data, tableSearchable
        } = this.props;

        const tableOptions = {
            btnGroup: this.createButtonGroup,
            noDataText: 'No Data',
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
            <Modal size="lg" show={this.state.showDelete} onHide={this.closeDeleteModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Delete</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <form className="form-horizontal" onSubmit={this.deleteItems}>
                        <p name="tableDeleteMessage">Are you sure you want to delete these items?</p>
                        <ConfigButtons
                            performingAction={this.props.inProgress}
                            cancelId="delete-cancel"
                            submitId="delete-submit"
                            submitLabel="Confirm"
                            includeSave
                            includeCancel
                            onCancelClick={this.closeDeleteModal}
                            isFixed={false}
                        />
                    </form>
                </Modal.Body>
            </Modal>
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
                {/* <AutoRefresh startAutoReload={this.props.refreshData} autoRefresh={autoRefresh} /> */}
                <AutoRefresh startAutoReload={() => true} autoRefresh={autoRefresh} />
            </div>
        );

        return (
            <div>
                {this.state.showErrorDialog && this.createErrorModal()}
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
    data: PropTypes.array.isRequired,
    columns: PropTypes.arrayOf(PropTypes.shape({
        header: PropTypes.string.isRequired,
        headerLabel: PropTypes.string.isRequired,
        isKey: PropTypes.bool.isRequired,
        hidden: PropTypes.bool.isRequired
    })).isRequired,
    newConfigFields: PropTypes.func.isRequired,
    editState: PropTypes.func.isRequired,
    onConfigSave: PropTypes.func,
    onConfigDelete: PropTypes.func,
    onConfigClose: PropTypes.func,
    clearModalFieldState: PropTypes.func,
    sortName: PropTypes.string,
    sortOrder: PropTypes.string,
    selectRowBox: PropTypes.bool,
    tableMessage: PropTypes.string,
    autoRefresh: PropTypes.bool,
    newButton: PropTypes.bool,
    deleteButton: PropTypes.bool,
    inProgress: PropTypes.bool,
    fetching: PropTypes.bool,
    modalTitle: PropTypes.string,
    tableNewButtonLabel: PropTypes.string,
    tableDeleteButtonLabel: PropTypes.string,
    tableSearchable: PropTypes.bool,
    tableRefresh: PropTypes.bool,
    hasFieldErrors: PropTypes.bool,
    errorDialogMessage: PropTypes.string
};

TableDisplay.defaultProps = {
    sortName: '',
    sortOrder: 'asc',
    selectRowBox: true,
    tableMessage: '',
    autoRefresh: true,
    newButton: true,
    deleteButton: true,
    inProgress: false,
    fetching: false,
    onConfigSave: () => true,
    onConfigDelete: () => null,
    onConfigClose: () => null,
    clearModalFieldState: () => null,
    modalTitle: 'New',
    tableNewButtonLabel: 'New',
    tableDeleteButtonLabel: 'Delete',
    tableSearchable: true,
    tableRefresh: true,
    hasFieldErrors: false,
    errorDialogMessage: null
};

const mapStateToProps = state => ({
    autoRefresh: state.refresh.autoRefresh
});

export default connect(mapStateToProps, null)(TableDisplay);
