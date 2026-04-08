import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import EnabledCell from 'common/component/table/cell/EnabledCell';
import ProviderRowActionsCell from 'page/provider/ProviderRowActionsCell';
import ProviderTableActions from 'page/provider/ProviderTableActions';
import { fetchProviders } from 'store/actions/provider';
import { BLACKDUCK_GLOBAL_FIELD_KEYS } from 'page/provider/blackduck/BlackDuckModel';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import TimestampCell from 'common/component/table/cell/TimestampCell';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create a Provider connection to use this table.'
};

function ProviderTable({ readonly }) {
    const dispatch = useDispatch();
    const refreshStatus = JSON.parse(window.localStorage.getItem('PROVIDER_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [tableData, setTableData] = useState();
    const [search, setNewSearch] = useState('');
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();
    const { data } = useSelector((state) => state.provider);

    const COLUMNS = [{
        key: 'name',
        label: 'Name',
        sortable: true
    }, {
        key: 'createdAt',
        label: 'Created At',
        sortable: true,
        customCell: TimestampCell
    }, {
        key: 'lastUpdated',
        label: 'Last Updated',
        sortable: true,
        customCell: TimestampCell
    }, {
        key: 'enabled',
        label: 'Enabled',
        sortable: false,
        customCell: EnabledCell,
        settings: { alignment: 'center' }
    }, {
        key: 'providerRowActions',
        label: '',
        sortable: false,
        customCell: ProviderRowActionsCell,
        settings: { alignment: 'center', readonly }
    }];

    useEffect(() => {
        dispatch(fetchProviders());
    }, []);

    useEffect(() => {
        localStorage.setItem('PROVIDER_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchProviders()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }
        return undefined;
    }, [autoRefresh]);

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    const handleSearchChange = (searchValue) => {
        setNewSearch(searchValue);
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    const onSort = (name) => {
        if (name !== sortConfig?.name || !sortConfig) {
            return setSortConfig({ name, direction: 'ASC' });
        }

        if (name === sortConfig?.name && sortConfig?.direction === 'DESC') {
            return setSortConfig();
        }

        if (name === sortConfig?.name) {
            return setSortConfig({ name, direction: 'DESC' });
        }

        return setSortConfig();
    };

    useEffect(() => {
        let convertedTableData = [];
        const filteredFieldModels = data ? data.filter((model) => FieldModelUtilities.hasAnyValuesExcludingId(model)) : [];

        convertedTableData = filteredFieldModels.map((fieldModel) => ({
            id: FieldModelUtilities.getFieldModelId(fieldModel),
            name: FieldModelUtilities.getFieldModelSingleValue(fieldModel, BLACKDUCK_GLOBAL_FIELD_KEYS.name),
            enabled: FieldModelUtilities.getFieldModelBooleanValue(fieldModel, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled),
            lastUpdated: fieldModel.lastUpdated,
            createdAt: fieldModel.createdAt,
            url: FieldModelUtilities.getFieldModelSingleValue(fieldModel, BLACKDUCK_GLOBAL_FIELD_KEYS.url),
            timeout: FieldModelUtilities.getFieldModelSingleValue(fieldModel, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)
        }));

        if (sortConfig) {
            const { name, direction } = sortConfig;
            convertedTableData = [...convertedTableData].sort((a, b) => {
                if (a[name] === null) {
                    return 1;
                }
                if (b[name] === null) {
                    return -1;
                }
                if (a[name] === null && b[name] === null) {
                    return 0;
                }
                return (
                    a[name].toString().localeCompare(b[name].toString(), 'en', { numeric: true }) * (direction === 'ASC' ? 1 : -1)
                );
            });
        }

        // Endpoints without search will need to look through ui column values
        setTableData(!search ? convertedTableData : convertedTableData?.filter((provider) => provider.name.toLowerCase().includes(search.toLowerCase())
            || provider.createdAt?.toLowerCase().includes(search.toLowerCase())
            || provider.lastUpdated?.toLowerCase().includes(search.toLowerCase())));
    }, [data, search, sortConfig]);

    return (
        <Table
            tableData={tableData}
            columns={COLUMNS}
            multiSelect
            selected={selected}
            onSelected={onSelected}
            onSort={onSort}
            sortConfig={sortConfig}
            searchBarPlaceholder="Search Providers..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <ProviderTableActions data={tableData} selected={selected} readonly={readonly} setSelected={setSelected} />}
        />
    );
}

ProviderTable.propTypes = {
    readonly: PropTypes.bool
};

export default ProviderTable;
