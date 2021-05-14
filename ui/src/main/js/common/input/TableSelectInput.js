import React, { useEffect, useState } from 'react';
import { BootstrapTable, SearchField, TableHeaderColumn } from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';
import Select, { components } from 'react-select';
import DescriptorOption from 'common/DescriptorOption';
import GeneralButton from 'common/button/GeneralButton';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import ConfirmModal from 'common/ConfirmModal';

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

export const createTableSelectColumn = (header, headerLabel, isKey, sortBy, visible) => ({
    header,
    headerLabel,
    isKey,
    sortBy,
    hidden: !visible
});

// TODO remove currentConfig and requiredRelatedFields in favor of createRequestBody function.
const TableSelectInput = (props) => {
    const {
        id, value, columns, useRowAsValue, onChange, fieldKey, csrfToken, currentConfig, endpoint, requiredRelatedFields, paged, searchable, readOnly,
        description,
        errorName,
        errorValue,
        label,
        required,
        showDescriptionPlaceHolder,
        createRequestBody
    } = props;
    const [progress, setProgress] = useState(false);
    const [showTable, setShowTable] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(10);
    const [totalPageCount, setTotalPageCount] = useState(0);
    const [currentSearchTerm, setCurrentSearchTerm] = useState('');
    const [data, setData] = useState([]);
    const [selectedData, setSelectedData] = useState([]);
    const [displayedData, setDisplayedData] = useState([]);
    const [showClearConfirm, setShowClearConfirm] = useState(false);
    const [fieldError, setFieldError] = useState(null);

    const loadSelectedValues = () => {
        let valueToUse = value;
        if (useRowAsValue && value && value.length > 0) {
            const areStringValues = value.every((option) => typeof option === 'string');
            if (areStringValues) {
                valueToUse = value.map((option) => JSON.parse(option));
            }
        }
        const loadedSelectedData = [];
        loadedSelectedData.push(...valueToUse);

        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const convertedValues = loadedSelectedData.map((selected) => {
            const labelToUse = useRowAsValue ? selected[keyColumnHeader] : selected;
            const isMissing = selected.missing !== undefined ? selected.missing : false;
            return {
                label: labelToUse,
                value: selected,
                missing: isMissing
            };
        });
        setDisplayedData(convertedValues);
        setSelectedData(loadedSelectedData);
    };

    useEffect(() => {
        loadSelectedValues();
    }, []);

    const handleShowClearConfirm = () => {
        setShowClearConfirm(true);
    };

    const handleHideClearConfirm = () => {
        setShowClearConfirm(false);
    };

    const handleClearClick = () => {
        setSelectedData([]);
        setDisplayedData([]);
        setShowClearConfirm(false);

        onChange({
            target: {
                name: fieldKey,
                value: []
            }
        });
    };

    const createSelectedArray = (selectedArray, row, isSelected) => {
        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const rowKeyValue = row[keyColumnHeader];

        if (isSelected) {
            const itemFound = selectedArray.find((selectedItem) => (useRowAsValue ? selectedItem[keyColumnHeader] === rowKeyValue : selectedItem === rowKeyValue));
            if (!itemFound) {
                const rowData = (useRowAsValue) ? row : rowKeyValue;
                selectedArray.push(rowData);
            }
        } else {
            const index = useRowAsValue ? selectedArray.findIndex((selection) => selection[keyColumnHeader] === rowKeyValue) : selectedArray.indexOf(rowKeyValue);
            if (index >= 0) {
                // if found, remove that element from selected array
                selectedArray.splice(index, 1);
            }
        }
    };

    const onRowSelectedAll = (isSelected, rows) => {
        if (rows) {
            const selected = Object.assign([], selectedData);
            rows.forEach((row) => {
                createSelectedArray(selected, row, isSelected);
            });
            setSelectedData(selected);
        } else {
            setSelectedData([]);
        }
    };

    const onRowSelected = (row, isSelected) => {
        const selected = Object.assign([], selectedData);
        createSelectedArray(selected, row, isSelected);
        setSelectedData(selected);
    };

    const createRowSelectionProps = () => {
        const keyColumnHeader = columns.find((column) => column.isKey).header;

        const condition = selectedData && useRowAsValue;
        let selectedRowData;
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
            onSelect: onRowSelected,
            onSelectAll: onRowSelectedAll
        };
    };

    const retrieveTableData = async (uiPageNumber, pageSize, searchTerm) => {
        setProgress(true);

        const newFieldModel = createRequestBody ? createRequestBody() : FieldModelUtilities.createFieldModelFromRequestedFields(currentConfig, requiredRelatedFields);
        const pageNumber = uiPageNumber ? uiPageNumber - 1 : 0;
        const encodedSearchTerm = encodeURIComponent(searchTerm);
        const apiUrl = `/alert${endpoint}/${fieldKey}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${encodedSearchTerm}`;
        const request = createNewConfigurationRequest(apiUrl, csrfToken, newFieldModel);
        return request.then((response) => {
            setProgress(false);
            if (response.ok) {
                return response.json().then((responseData) => {
                    const { options, totalPages } = responseData;
                    setData(options);
                    setTotalPageCount(totalPages);
                    return responseData;
                });
            }
            response.json().then((responseData) => {
                setFieldError(responseData.message);
            });
        });
    };

    const createDataList = () => {
        const dataList = data.map((itemData) => Object.assign(itemData, { missing: false }));
        selectedData.forEach((selected) => {
            const missingAttribute = selected.missing;
            if (missingAttribute !== undefined && missingAttribute) {
                dataList.unshift(Object.assign(selected, { missing: true }));
            }
        });
        return dataList;
    };

    const createTable = () => {
        const defaultSortName = columns.find((column) => column.sortBy).header;

        const onPageChange = (page, sizePerPage) => {
            setCurrentPage(page);
            setCurrentPageSize(sizePerPage);
            retrieveTableData(page, sizePerPage, currentSearchTerm);
        };

        const onSizePerPageListChange = (sizePerPage) => {
            setCurrentPageSize(sizePerPage);
            retrieveTableData(currentPage, sizePerPage, currentSearchTerm);
        };

        const onSearchChange = (searchTerm, colInfos, multiColumnSearch) => {
            setCurrentSearchTerm(searchTerm);
            setCurrentPage(1);
            retrieveTableData(1, currentPageSize, searchTerm);
        };

        // Displays the # of pages for the table
        const tableFetchInfo = {
            dataTotalSize: totalPageCount * currentPageSize
        };

        const noTableDataMessage = fieldError || 'No data found';
        const tableOptions = {
            noDataText: noTableDataMessage,
            clearSearch: true,
            defaultSortName,
            defaultSortOrder: 'asc',

            searchDelayTime: 750,
            searchField: (searchFieldProps) => (
                <SearchField
                    defaultValue={currentSearchTerm}
                    placeholder="Search"
                />
            ),

            sizePerPage: currentPageSize,
            page: currentPage,
            onPageChange,
            onSizePerPageListChange,
            onSearchChange
        };

        const projectsSelectRowProp = createRowSelectionProps();

        const assignDataFormat = (cell, row) => {
            const missingData = row.missing && cell && cell !== '';
            return (
                <div title={(cell) ? cell.toString() : undefined}>
                    {' '}
                    {!missingData && cell}
                    {missingData && (
                        <span className="missingData">
                            <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
                            {cell}
                        </span>
                    )}
                    {' '}
                </div>
            );
        };

        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const okClicked = () => {
            const convertedValues = selectedData.map((selected) => {
                const labelToUse = useRowAsValue ? selected[keyColumnHeader] : selected;
                const isMissing = selected.missing !== undefined ? selected.missing : false;
                return {
                    label: labelToUse,
                    value: selected,
                    missing: isMissing
                };
            });
            setShowTable(false);
            setDisplayedData(convertedValues);
            setCurrentPage(1);
            setCurrentPageSize(10);
            setCurrentSearchTerm('');

            onChange({
                target: {
                    name: fieldKey,
                    value: selectedData
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
        tableColumns.push(<TableHeaderColumn key="missingHeaderKey" dataField="missing" dataFormat={assignDataFormat} hidden>Missing Data</TableHeaderColumn>);

        return (
            <div>
                {progress && (
                    <div className="progressIcon">
                        <span className="fa-layers fa-fw">
                            <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                        </span>
                    </div>
                )}
                {!progress && (
                    <BootstrapTable
                        version="4"
                        data={createDataList()}
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
                )}
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
    };

    const selectOnClick = (event) => {
        event.preventDefault();
        event.stopPropagation();
        retrieveTableData(currentPage, currentPageSize, '');
        setShowTable(true);
    };

    const createSelect = () => {
        const selectComponents = {
            MultiValue: typeLabel,
            ValueContainer: container,
            DropdownIndicator: null,
            MultiValueRemove: () => <div />
        };
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
                        components={selectComponents}
                        noOptionsMessage={null}
                        isDisabled
                        clearable={false}
                        value={displayedData}
                    />
                    <div className="d-inline-flex float-right">
                        <GeneralButton
                            id={selectButtonId}
                            className="selectButton"
                            onClick={selectOnClick}
                            disabled={showTable || readOnly}
                        >
                            Select
                        </GeneralButton>
                        {selectedData && selectedData.length > 0
                        && (
                            <GeneralButton
                                id={clearButtonId}
                                className="selectClearButton"
                                onClick={handleShowClearConfirm}
                            >
                                Clear
                            </GeneralButton>
                        )}
                    </div>
                    <ConfirmModal
                        id={confirmModalId}
                        title="Clear Input"
                        showModal={showClearConfirm}
                        affirmativeAction={handleClearClick}
                        negativeAction={handleHideClearConfirm}
                    >
                        <div>
                            Are you sure you want to clear all selected items?
                        </div>
                    </ConfirmModal>
                </div>
            </div>
        );
    };

    const onHideTableSelectModal = () => {
        const previousSelectedData = displayedData.map((currentValue) => ({
            ...currentValue.value
        }));

        setShowTable(false);
        setSelectedData(previousSelectedData);
    };

    const tableModal = (
        <Modal dialogClassName="topLevelModal" size="lg" show={showTable} onHide={onHideTableSelectModal}>
            <Modal.Header closeButton>
                <Modal.Title>{label}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {createTable()}
            </Modal.Body>
        </Modal>
    );

    return (
        <div>
            <div>
                <LabeledField
                    id={id}
                    description={description}
                    errorName={errorName}
                    errorValue={errorValue}
                    label={label}
                    labelClass="col-sm-3"
                    required={required}
                    showDescriptionPlaceHolder={showDescriptionPlaceHolder}
                >
                    {createSelect()}
                </LabeledField>
            </div>
            {showTable && tableModal}
        </div>
    );
};

TableSelectInput.propTypes = {
    id: PropTypes.string,
    fieldKey: PropTypes.string.isRequired,
    endpoint: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    currentConfig: PropTypes.object,
    columns: PropTypes.array.isRequired,
    requiredRelatedFields: PropTypes.array,
    searchable: PropTypes.bool,
    onChange: PropTypes.func,
    paged: PropTypes.bool,
    readOnly: PropTypes.bool,
    useRowAsValue: PropTypes.bool,
    value: PropTypes.array,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool,
    createRequestBody: PropTypes.func
};

TableSelectInput.defaultProps = {
    id: 'tableSelectInputId',
    currentConfig: {},
    requiredRelatedFields: [],
    searchable: true,
    onChange: () => {
    },
    paged: false,
    readOnly: false,
    useRowAsValue: false,
    value: [],
    createRequestBody: null,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default TableSelectInput;
