import React, { Component } from 'react';
import { connect } from 'react-redux';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
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
            data: [],
            selectedData: [],
            displayedData: [],
            showClearConfirm: false
        };
    }

    componentWillMount() {
        const { value } = this.props;
        if (value && value.length > 0) {
            this.retrieveTableData().then(() => {
                this.updateSelectedValues();
            });
        }
    }

    componentDidUpdate(prevProps) {
        const { value } = this.props;
        const prevSize = prevProps.value && prevProps.value.length === 0;
        const currentSize = value && value.length > 0;
        const emptySelected = this.state.selectedData.length === 0;
        if (prevSize && currentSize && emptySelected) {
            if (this.state.data.length == 0) {
                this.retrieveTableData().then(() => {
                    this.updateSelectedValues();
                });
            } else {
                this.updateSelectedValues();
            }
        }
    }

    updateSelectedValues() {
        const { value } = this.props;
        const { selectedData, data } = this.state;
        selectedData.push(...value);
        const keyColumnHeader = this.props.columns.find((column) => column.isKey).header;
        const convertedValues = selectedData.map((selected) => {
            const columnContainsValue = data.map((dataValue) => dataValue[keyColumnHeader])
                .includes(selected);
            return {
                label: selected,
                value: selected,
                missing: !columnContainsValue
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
        const keyColumnHeader = this.props.columns.find((column) => column.isKey).header;
        const rowValue = row[keyColumnHeader];

        if (isSelected) {
            const projectFound = selectedArray.find((project) => project === rowValue);
            if (!projectFound) {
                selectedArray.push(rowValue);
            }
        } else {
            const index = selectedArray.indexOf(rowValue);
            if (index >= 0) {
                selectedArray.splice(index, 1); // if found, remove that element from selected array
            }
        }
    }

    createRowSelectionProps() {
        return {
            mode: 'checkbox',
            clickToSelect: true,
            showOnlySelected: true,
            selected: this.state.selectedData,
            onSelect: this.onRowSelected,
            onSelectAll: this.onRowSelectedAll
        };
    }

    async retrieveTableData() {
        this.setState({
            progress: true,
            success: false
        });
        const {
            fieldKey, csrfToken, currentConfig, endpoint, requestedDataFieldKeys
        } = this.props;

        const newFieldModel = FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requestedDataFieldKeys);
        const request = createNewConfigurationRequest(`/alert${endpoint}/${fieldKey}`, csrfToken, newFieldModel);
        return request.then((response) => {
            this.setState({
                progress: false
            });
            if (response.ok) {
                return response.json().then((data) => {
                    const { options } = data;
                    this.setState({
                        data: options,
                        success: true
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
        const addMissingColumn = data.map((itemData) => Object.assign(itemData, { missing: false }));

        const keyColumnHeader = this.props.columns.find((column) => column.isKey).header;
        selectedData.forEach((itemDataValue) => {
            const dataFound = addMissingColumn.find((foundData) => itemDataValue === foundData[keyColumnHeader]);
            if (!dataFound) {
                addMissingColumn.unshift({
                    [keyColumnHeader]: itemDataValue,
                    missing: true
                });
            }
        });

        return addMissingColumn;
    }

    createTable() {
        const columnsProp = this.props.columns;
        const defaultSortName = columnsProp.find((column) => column.sortBy).header;

        const tableOptions = {
            noDataText: 'No data found',
            clearSearch: true,
            defaultSortName,
            defaultSortOrder: 'asc'
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

        const okClicked = () => {
            const keyColumnHeader = this.props.columns.find((column) => column.isKey).header;
            const convertedValues = this.state.selectedData.map((selected) => {
                const columnContainsValue = this.state.data.map((dataValue) => dataValue[keyColumnHeader]).includes(selected);
                return { label: selected, value: selected, missing: !columnContainsValue };
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

        const columns = columnsProp.map((column) => (
            <TableHeaderColumn
                key={column.header}
                dataField={column.header}
                isKey={column.isKey}
                dataSort
                columnClassName="tableCell"
                tdStyle={{ whiteSpace: 'normal' }}
                dataFormat={assignDataFormat}
            >
                {column.headerLabel}
            </TableHeaderColumn>
        ));

        // Need to add this column to the array as you can't display columns dynamically and statically https://github.com/AllenFang/react-bootstrap-table/issues/1814
        columns.push(<TableHeaderColumn dataField="missing" dataFormat={assignDataFormat} hidden>Missing Data</TableHeaderColumn>);

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
                    search={searchable}
                    options={tableOptions}
                    trClassName="tableRow"
                    headerContainerClass="scrollable"
                    bodyContainerClass="tableScrollableBody"
                    pagination={paged}
                >
                    {columns}
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
        event.preventDefault();
        event.stopPropagation();
        this.retrieveTableData();
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
                        showModal={this.state.showClearConfirm}
                        title="Are you sure you want to clear all selected items?"
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
    requestedDataFieldKeys: PropTypes.array
};

TableSelectInput.defaultProps = {
    requestedDataFieldKeys: []
};

const mapStateToProps = (state) => ({
    csrfToken: state.session.csrfToken
});

export default connect(mapStateToProps)(TableSelectInput);
