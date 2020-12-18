import React, { Component } from 'react';
import { connect } from 'react-redux';
import {
    BootstrapTable,
    SearchField,
    TableHeaderColumn
} from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import LabeledField from 'field/LabeledField';
import Select, { components } from 'react-select';
import DescriptorOption from 'component/common/DescriptorOption';
import GeneralButton from 'field/input/GeneralButton';
import { createNewConfigurationRequest } from 'util/configurationRequestBuilder';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import ConfirmModal from 'component/common/ConfirmModal';

const { MultiValue, ValueContainer } = components;

const typeLabel = (props) => {
    const { data } = props;
    const missingItem = (data.missing) ? { textDecoration: 'line-through' } : {};

    return (
        <MultiValue {...props}>
            <DescriptorOption style={missingItem} label={data.label} value={data.value} />
        </MultiValue>
    );
};

const container = ({ children, getValue, ...props }) => {
    const { length } = getValue();
    const error = (
        <span className="missingData">
            <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
        </span>
    );
    const hasError = getValue().find((value) => value.missing);
    if (length <= 5) {
        return (
            <ValueContainer {...props}>
                {children}
                {hasError && error}
            </ValueContainer>
        );
    }

    return (
        <ValueContainer {...props}>
            {!props.selectProps.menuIsOpen
            && `${length} Items selected`}
            {React.cloneElement(children[1])}
            {hasError && error}
        </ValueContainer>
    );
};

class TableSelectInput extends Component {
    constructor(props) {
        super(props);

        this.updateSelectedValues = this.updateSelectedValues.bind(this);
        this.retrieveTableData = this.retrieveTableData.bind(this);
        this.createTable = this.createTable.bind(this);
        this.createSelect = this.createSelect.bind(this);
        this.onRowSelected = this.onRowSelected.bind(this);
        this.onRowSelectedAll = this.onRowSelectedAll.bind(this);
        this.selectOnClick = this.selectOnClick.bind(this);
        this.createDataList = this.createDataList.bind(this);
        this.handleClearClick = this.handleClearClick.bind(this);
        this.handleShowClearConfirm = this.handleShowClearConfirm.bind(this);
        this.handleHideClearConfirm = this.handleHideClearConfirm.bind(this);
        this.onHideTableSelectModal = this.onHideTableSelectModal.bind(this);

        this.state = {
            progress: false,
            showTable: false,
            currentPage: 1,
            currentPageSize: 10,
            totalPageCount: 0,
            currentSearchTerm: '',
            data: [],
            selectedData: [],
            displayedData: [],
            showClearConfirm: false
        };
    }

    componentDidMount() {
        this.updateSelectedValues();
    }

    updateSelectedValues() {
        const { value, columns, useRowAsValue } = this.props;
        const { selectedData } = this.state;

        let valueToUse = value;
        if (useRowAsValue && value && value.length > 0) {
            const areStringValues = value.every(option => typeof option === "string");
            if (areStringValues) {
                valueToUse = value.map(option => JSON.parse(option));
            }
        }

        selectedData.push(...valueToUse);

        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const convertedValues = selectedData.map((selected) => {
            const labelToUse = useRowAsValue ? selected[keyColumnHeader] : selected;
            const isMissing = selected.missing !== undefined ? selected.missing : false;
            return {
                label: labelToUse,
                value: selected,
                missing: isMissing
            };
        });
        this.setState({
            displayedData: convertedValues
        });
    }

    handleShowClearConfirm() {
        this.setState({
            showClearConfirm: true
        });
    }

    handleHideClearConfirm() {
        this.setState({
            showClearConfirm: false
        });
    }

    handleClearClick() {
        this.setState({
            selectedData: [],
            displayedData: [],
            showClearConfirm: false
        });

        this.props.onChange({
            target: {
                name: this.props.fieldKey,
                value: []
            }
        });
    }

    onRowSelectedAll(isSelected, rows) {
        if (rows) {
            const selected = Object.assign([], this.state.selectedData);
            rows.forEach((row) => {
                this.createSelectedArray(selected, row, isSelected);
            });
            this.setState({
                selectedData: selected
            });
        } else {
            this.setState({
                selectedData: []
            });
        }
    }

    onRowSelected(row, isSelected) {
        const selected = Object.assign([], this.state.selectedData);
        this.createSelectedArray(selected, row, isSelected);
        this.setState({
            selectedData: selected
        });
    }

    createSelectedArray(selectedArray, row, isSelected) {
        const { columns, useRowAsValue } = this.props;
        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const rowKeyValue = row[keyColumnHeader];

        if (isSelected) {
            const itemFound = selectedArray.find((selectedItem) => (useRowAsValue ? selectedItem[keyColumnHeader] === rowKeyValue : selectedItem === rowKeyValue));
            if (!itemFound) {
                useRowAsValue ? selectedArray.push(row) : selectedArray.push(rowKeyValue);
            }
        } else {
            const index = useRowAsValue ? selectedArray.findIndex(selection => selection[keyColumnHeader] === rowKeyValue) : selectedArray.indexOf(rowKeyValue);
            if (index >= 0) {
                // if found, remove that element from selected array
                selectedArray.splice(index, 1);
            }
        }
    }

    createRowSelectionProps() {
        const { selectedData } = this.state;
        const { columns, useRowAsValue } = this.props;
        const keyColumnHeader = columns.find((column) => column.isKey).header;

        const condition = selectedData && useRowAsValue;
        let selectedRowData = null;
        if (condition) {
            selectedRowData = selectedData.map((itemData) => itemData[keyColumnHeader]);
        } else {
            selectedRowData = selectedData;
        }
        return {
            mode: 'checkbox',
            clickToSelect: true,
            showOnlySelected: true,
            selected: selectedRowData,
            onSelect: this.onRowSelected,
            onSelectAll: this.onRowSelectedAll
        };
    }

    async retrieveTableData(uiPageNumber, pageSize, searchTerm) {
        this.setState({
            progress: true,
            success: false
        });
        const {
            fieldKey, csrfToken, currentConfig, endpoint, requiredRelatedFields
        } = this.props;

        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
        const pageNumber = uiPageNumber ? uiPageNumber - 1 : 0;
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`, csrfToken, newFieldModel);
        return request.then((response) => {
            this.setState({
                progress: false
            });
            if (response.ok) {
                return response.json().then((data) => {
                    const { options, totalPages } = data;
                    this.setState({
                        data: options,
                        success: true,
                        totalPageCount: totalPages,
                        currentPage: uiPageNumber,
                        currentPageSize: pageSize,
                        currentSearchTerm: searchTerm
                    });
                    return data;
                });
            }
            response.json().then((data) => {
                this.setState({
                    fieldError: data.message
                });
            });
        });
    }

    createDataList() {
        const { data, selectedData } = this.state;
        const dataList = data.map((itemData) => Object.assign(itemData, { missing: false }));
        selectedData.forEach(selected => {
            const missingAttribute = selected.missing;
            if (missingAttribute !== undefined && missingAttribute) {
                dataList.unshift(Object.assign(selected, { missing: true }));
            }
        });
        return dataList;
    }

    createTable() {
        const { columns, useRowAsValue } = this.props;
        const defaultSortName = columns.find((column) => column.sortBy).header;

        const onPageChange = (page, sizePerPage) => {
            const { currentSearchTerm } = this.state;
            this.retrieveTableData(page, sizePerPage, currentSearchTerm);
        };

        const onSizePerPageListChange = (sizePerPage) => {
            const { currentPage, currentSearchTerm } = this.state;
            this.retrieveTableData(currentPage, sizePerPage, currentSearchTerm);
        };

        const onSearchChange = (searchTerm, colInfos, multiColumnSearch) => {
            const { currentPage, currentPageSize } = this.state;
            this.retrieveTableData(currentPage, currentPageSize, searchTerm);
        };

        const {
            currentPage, currentPageSize, totalPageCount, currentSearchTerm
        } = this.state;

        // Displays the # of pages for the table
        const tableFetchInfo = {
            dataTotalSize: totalPageCount * currentPageSize
        };

        const tableOptions = {
            noDataText: 'No data found',
            clearSearch: true,
            defaultSortName,
            defaultSortOrder: 'asc',

            searchDelayTime: 750,
            searchField: (searchFieldProps) => {
                return (
                    <SearchField
                        defaultValue={currentSearchTerm}
                        placeholder='Search'
                    />
                );
            },

            sizePerPage: currentPageSize,
            page: currentPage,
            onPageChange,
            onSizePerPageListChange,
            onSearchChange
        };

        const projectsSelectRowProp = this.createRowSelectionProps();

        const assignDataFormat = (cell, row) => {
            const cellContent = (row.missing && cell && cell !== '')
                ? (
                    <span className="missingData">
                        <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
                        {cell}
                    </span>
                )
                : cell;

            if (cell) {
                return (
                    <div title={cell.toString()}>
                        {' '}
                        {cellContent}
                        {' '}
                    </div>
                );
            }
            return (
                <div>
                    {' '}
                    {cellContent}
                    {' '}
                </div>
            );
        };

        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const okClicked = () => {
            const convertedValues = this.state.selectedData.map((selected) => {
                const labelToUse = useRowAsValue ? selected[keyColumnHeader] : selected;
                const isMissing = selected.missing !== undefined ? selected.missing : false;
                return {
                    label: labelToUse,
                    value: selected,
                    missing: isMissing
                };
            });
            this.setState({
                showTable: false,
                displayedData: convertedValues
            });

            this.props.onChange({
                target: {
                    name: this.props.fieldKey,
                    value: this.state.selectedData
                }
            });
        };

        const tableColumns = columns.map((column) => (
            <TableHeaderColumn
                key={column.header}
                dataField={column.header}
                isKey={column.isKey}
                dataSort
                columnClassName="tableCell"
                tdStyle={{ whiteSpace: 'normal' }}
                dataFormat={assignDataFormat}
                hidden={column.hidden}
            >
                {column.headerLabel}
            </TableHeaderColumn>
        ));

        // Need to add this column to the array as you can't display tableColumns dynamically and statically https://github.com/AllenFang/react-bootstrap-table/issues/1814
        tableColumns.push(<TableHeaderColumn dataField="missing" dataFormat={assignDataFormat} hidden>Missing Data</TableHeaderColumn>);

        const { paged, searchable, fieldKey } = this.props;

        const displayTable = (this.state.progress)
            ? (
                <div className="progressIcon">
                    <span className="fa-layers fa-fw">
                        <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                    </span>
                </div>
            )
            : (
                <BootstrapTable
                    ref
                    version="4"
                    data={this.createDataList()}
                    containerClass="table"
                    hover
                    condensed
                    selectRow={projectsSelectRowProp}
                    options={tableOptions}
                    trClassName="tableRow"
                    headerContainerClass="scrollable"
                    bodyContainerClass="tableScrollableBody"

                    search={searchable}
                    pagination={paged}
                    remote
                    fetchInfo={tableFetchInfo}
                >
                    {tableColumns}
                </BootstrapTable>
            );

        return (
            <div>
                {displayTable}
                <div>
                    <GeneralButton
                        id={`${fieldKey}-confirmation`}
                        className="tableSelectOkButton"
                        onClick={okClicked}
                    >
                        OK
                    </GeneralButton>
                </div>
            </div>
        );
    }

    selectOnClick(event) {
        const { currentPage, currentPageSize } = this.state;
        event.preventDefault();
        event.stopPropagation();
        this.retrieveTableData(currentPage, currentPageSize, '');
        this.setState({ showTable: true });
    }

    createSelect() {
        const components = {
            MultiValue: typeLabel,
            ValueContainer: container,
            DropdownIndicator: null,
            MultiValueRemove: () => <div />
        };
        const { fieldKey } = this.props;
        const selectFieldId = `${fieldKey}-selectField`;
        const selectButtonId = `${fieldKey}_select`;
        const clearButtonId = `${fieldKey}_clear`;
        const confirmModalId = `${fieldKey}-confirmModal`;
        return (
            <div className="col-sm-8 d-inline-flex p-2">
                <div className="d-block typeAheadField">
                    <Select
                        id={selectFieldId}
                        className="typeAheadField"
                        onChange={null}
                        options={[]}
                        isMulti
                        components={components}
                        noOptionsMessage={null}
                        isDisabled
                        clearable={false}
                        value={this.state.displayedData}
                    />
                    <div className="d-inline-flex float-right">
                        <GeneralButton
                            id={selectButtonId}
                            className="selectButton"
                            onClick={this.selectOnClick}
                            disabled={this.state.showTable || this.props.readOnly}
                        >
                            Select
                        </GeneralButton>
                        {this.state.selectedData && this.state.selectedData.length > 0
                        && (
                            <GeneralButton
                                id={clearButtonId}
                                className="selectClearButton"
                                onClick={this.handleShowClearConfirm}
                            >
                                Clear
                            </GeneralButton>
                        )}
                    </div>
                    <ConfirmModal
                        id={confirmModalId}
                        title="Clear Input"
                        message="Are you sure you want to clear all selected items?"
                        showModal={this.state.showClearConfirm}
                        affirmativeAction={this.handleClearClick}
                        negativeAction={this.handleHideClearConfirm}
                    />
                </div>
            </div>
        );
    }

    onHideTableSelectModal() {
        this.setState({
            showTable: false,
            selectedData: this.state.displayedData
        });
    }

    render() {
        const tableModal = (
            <Modal dialogClassName="topLevelModal" size="lg" show={this.state.showTable} onHide={this.onHideTableSelectModal}>
                <Modal.Header closeButton>
                    <Modal.Title>{this.props.label}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {this.createTable()}
                </Modal.Body>
            </Modal>
        );

        return (
            <div>
                <div>
                    <LabeledField field={this.createSelect()} labelClass="col-sm-3" {...this.props} />
                </div>
                {this.state.showTable && tableModal}
            </div>
        );
    }
}

TableSelectInput.propTypes = {
    fieldKey: PropTypes.string.isRequired,
    endpoint: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    currentConfig: PropTypes.object.isRequired,
    columns: PropTypes.array.isRequired,
    requiredRelatedFields: PropTypes.array,
    searchable: PropTypes.bool,
    paged: PropTypes.bool,
    useRowAsValue: PropTypes.bool
};

TableSelectInput.defaultProps = {
    requiredRelatedFields: [],
    searchable: true,
    paged: false,
    useRowAsValue: false
};

const mapStateToProps = (state) => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps)(TableSelectInput);
