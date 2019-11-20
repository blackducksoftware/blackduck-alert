import React, { Component } from 'react';
import PropTypes from 'prop-types';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import AutoRefresh from 'component/common/AutoRefresh';

class TableDisplay extends Component {
    constructor(props) {
        super(props);

        this.createTableColumns = this.createTableColumns.bind(this);
        this.createButtonGroup = this.createButtonGroup.bind(this);
        this.createInsertModal = this.createInsertModal.bind(this);
        this.checkPermissions = this.checkPermissions.bind(this);

        this.state = {
            data: []
        };
    }

    createTableColumns() {
        const assignDataFormat = (cell, row) => {
            const cellContent = (cell && cell !== '') ?
                <span className="missingData">
                    <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />{cell}
                </span>
                : cell;

            if (cell) {
                return <div title={cell.toString()}> {cellContent} </div>;
            }
            return <div> {cellContent} </div>;
        }

        return this.props.columns.map(column => (
            <TableHeaderColumn key={column.header} dataField={column.header} isKey={column.isKey} dataSort columnClassName="tableCell" tdStyle={{ whiteSpace: 'normal' }}
                               dataFormat={assignDataFormat}>{column.headerLabel}</TableHeaderColumn>
        ));
    }

    createButtonGroup(buttons) {
        const classes = 'btn btn-md btn-info react-bs-table-add-btn tableButton';
        const insertOnClick = buttons.insertBtn ? buttons.insertBtn.props.onClick : null;
        const deleteOnClick = buttons.deleteBtn ? buttons.deleteBtn.props.onClick : null;
        const refreshButton = !this.props.autoRefresh &&
            (<button type="button" tabUserTable={0} className={classes} onClick={this.props.retrieveData}>
                <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />Refresh
            </button>);
        return (
            <div>
                {buttons.insertBtn
                && <InsertButton className="addJobButton btn-md" onClick={insertOnClick}>
                    <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                    New
                </InsertButton>
                }
                {buttons.deleteBtn
                && <DeleteButton className="deleteJobButton btn-md" onClick={deleteOnClick}>
                    <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                    Delete
                </DeleteButton>
                }
                {refreshButton}
            </div>
        );
    }

    createInsertModal() {
        return this.props.insertModal();
    }

    checkPermissions(operation) {
        const { descriptor } = this.props;
        if (descriptor) {
            return DescriptorUtilities.isOperationAssigned(descriptor, operation)
        }
        return false;
    }

    render() {
        const tableColumns = this.createTableColumns();

        const { selectRowBox, sortName, sortOrder, autoRefresh, tableMessage } = this.props;
        const { data } = this.state;

        const tableOptions = {
            btnGroup: this.createButtonGroup,
            noDataText: 'No Data',
            clearSearch: true,
            insertModal: this.createInsertModal,
            defaultSortName: sortName,
            defaultSortOrder: sortOrder
        };

        const selectRow = selectRowBox && {
            mode: 'checkbox',
            clickToSelect: true,
            bgColor(row, isSelect) {
                if (isSelect) {
                    return '#e8e8e8';
                }
                return null;
            }
        };

        const canCreate = this.checkPermissions(DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = this.checkPermissions(DescriptorUtilities.OPERATIONS.DELETE);

        const content = (
            <div>
                <BootstrapTable
                    version="4"
                    hover
                    condensed
                    data={data}
                    containerClass="table"
                    insertRow={canCreate}
                    deleteRow={canDelete}
                    selectRow={selectRow}
                    options={tableOptions}
                    search
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
        return (
            <div>
                <div className="pull-right">
                    <AutoRefresh startAutoReload={this.props.retrieveData} autoRefresh={autoRefresh} />
                </div>
                {content}
            </div>
        );
    }

}

TableDisplay.propTypes = {
    retrieveData: PropTypes.func.isRequired,
    columns: PropTypes.arrayOf(PropTypes.shape({
        header: PropTypes.string.isRequired,
        headerLabel: PropTypes.string.isRequired,
        isKey: PropTypes.bool.isRequired
    })).isRequired,
    insertModal: PropTypes.func.isRequired,
    name: PropTypes.string,
    sortName: PropTypes.string,
    sortOrder: PropTypes.string,
    selectRowBox: PropTypes.bool,
    tableMessage: PropTypes.string,
    autoRefresh: PropTypes.bool,
    descriptor: PropTypes.object,
    inProgress: PropTypes.bool
};

TableDisplay.defaultProps = {
    name: '',
    sortName: '',
    sortOrder: 'asc',
    selectRowBox: true,
    tableMessage: '',
    autoRefresh: true,
    descriptor: null,
    inProgress: false
};

export default TableDisplay;
