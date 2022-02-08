import React, { useEffect, useState } from 'react';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import Select, { components } from 'react-select';
import DescriptorOption from 'common/component/descriptor/DescriptorOption';
import GeneralButton from 'common/component/button/GeneralButton';
import { createReadRequest } from 'common/util/configurationRequestBuilder';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfirmModal from 'common/component/ConfirmModal';

const { SingleValue, ValueContainer } = components;

const typeLabel = (props) => {
    const { data } = props;
    const missingItem = (data.missing) ? { textDecoration: 'line-through', marginLeft: '15px' } : {};

    return (
        <SingleValue {...props}>
            <DescriptorOption style={missingItem} label={data.label} value={data.value} />
        </SingleValue>
    );
};

const container = ({ children, getValue, ...props }) => {
    const error = (
        <div className="missingData">
            <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
        </div>
    );
    const hasError = getValue().find((value) => value.missing);
    return (
        <ValueContainer {...props}>
            {hasError && error}
            {children}
        </ValueContainer>
    );
};

const GlobalConfigurationSelectInput = (props) => {
    const {
        id, value, columns, onChange, fieldKey, csrfToken, endpoint, paged, readOnly,
        description,
        errorName,
        errorValue,
        label,
        required,
        showDescriptionPlaceHolder
    } = props;
    const [progress, setProgress] = useState(false);
    const [showTable, setShowTable] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(10);
    const [totalPageCount, setTotalPageCount] = useState(0);
    const [currentSearchTerm, setCurrentSearchTerm] = useState('');
    const [data, setData] = useState([]);
    const [selectedData, setSelectedData] = useState(null);
    const [displayedData, setDisplayedData] = useState(null);
    const [showClearConfirm, setShowClearConfirm] = useState(false);
    const [fieldError, setFieldError] = useState(null);

    const loadSelectedValue = async () => {
        if (value) {
            setProgress(true);
            const apiUrl = `/alert${endpoint}/${value}`;
            const request = createReadRequest(apiUrl, csrfToken);
            const configuration = await request.then((response) => {
                setProgress(false);
                if (response.ok) {
                    return response.json().then((responseData) => {
                        const { id: configId, name } = responseData;
                        if (configId === value) {
                            return {
                                id: configId,
                                name,
                                missing: false
                            };
                        }
                        return {
                            id: value,
                            name: `Unknown(${value})`,
                            missing: true
                        };
                    });
                }
                response.json().then((responseData) => {
                    setFieldError(responseData.message);
                });
                return {
                    id: value,
                    name: `Unknown(${value})`,
                    missing: true
                };
            });
            setDisplayedData({
                label: configuration.name,
                value: configuration,
                missing: configuration.missing
            });
            setSelectedData(configuration);
        }
    };
    useEffect(() => {
        loadSelectedValue();
    }, []);

    const handleShowClearConfirm = () => {
        setShowClearConfirm(true);
    };

    const handleHideClearConfirm = () => {
        setShowClearConfirm(false);
    };

    const handleClearClick = () => {
        setSelectedData(null);
        setDisplayedData(null);
        setShowClearConfirm(false);

        onChange({
            target: {
                name: fieldKey,
                value: null
            }
        });
    };

    const onRowSelected = (row) => {
        setSelectedData(row);
    };

    const createRowSelectionProps = () => {
        const keyColumnHeader = columns.find((column) => column.isKey).header;
        const selectedRowData = selectedData ? [selectedData[keyColumnHeader]] : [];

        return {
            mode: 'radio',
            clickToSelect: true,
            showOnlySelected: true,
            selected: selectedRowData,
            onSelect: onRowSelected
        };
    };

    const retrieveTableData = async (uiPageNumber, pageSize, searchTerm) => {
        setProgress(true);

        const pageNumber = uiPageNumber ? uiPageNumber - 1 : 0;
        const encodedSearchTerm = encodeURIComponent(searchTerm);
        const apiUrl = `/alert${endpoint}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${encodedSearchTerm}`;
        const request = createReadRequest(apiUrl, csrfToken);
        return request.then((response) => {
            setProgress(false);
            if (response.ok) {
                return response.json().then((responseData) => {
                    const { models, totalPages } = responseData;
                    const configData = models.map((configurationModel) => {
                        const { id: configId, name } = configurationModel;
                        return {
                            id: configId,
                            name
                        };
                    });
                    setData(configData);
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
        if (selectedData) {
            if (!dataList.some((element) => element.id === selectedData.id)) {
                dataList.unshift({
                    id: selectedData.id,
                    name: selectedData.name,
                    missing: true
                });
            }
        }
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
            defaultSortName,
            defaultSortOrder: 'asc',
            sizePerPage: currentPageSize,
            page: currentPage,
            onPageChange,
            onSizePerPageListChange
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
            const labelToUse = selectedData[keyColumnHeader];
            const isMissing = selectedData.missing !== undefined ? selectedData.missing : false;
            const convertedValue = {
                label: labelToUse,
                value: selectedData,
                missing: isMissing
            };
            setShowTable(false);
            setDisplayedData(convertedValue);
            setCurrentPage(1);
            setCurrentPageSize(10);
            setCurrentSearchTerm('');

            onChange({
                target: {
                    name: fieldKey,
                    value: selectedData.id
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
                        search={false}
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
            SingleValue: typeLabel,
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
                        isMulti={false}
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
                        {selectedData
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
        setShowTable(false);
        setSelectedData(displayedData && displayedData.value);
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
GlobalConfigurationSelectInput.propTypes = {
    id: PropTypes.string,
    fieldKey: PropTypes.string.isRequired,
    endpoint: PropTypes.string.isRequired,
    csrfToken: PropTypes.string.isRequired,
    columns: PropTypes.array.isRequired,
    onChange: PropTypes.func,
    paged: PropTypes.bool,
    readOnly: PropTypes.bool,
    value: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

GlobalConfigurationSelectInput.defaultProps = {
    id: 'GlobalConfigurationSelectInputId',
    onChange: () => {
    },
    paged: false,
    readOnly: false,
    value: null,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default GlobalConfigurationSelectInput;
