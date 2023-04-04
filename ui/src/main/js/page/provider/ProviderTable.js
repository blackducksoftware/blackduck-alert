import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import ProviderEditCell from 'page/provider/ProviderEditCell';
import ProviderCopyCell from 'page/provider/ProviderCopyCell';
import ProviderEnabledCell from 'page/provider/ProviderEnabledCell';
import PoviderTableActions from 'page/provider/PoviderTableActions';
import { fetchProviders } from '../../store/actions/provider';
import { BLACKDUCK_GLOBAL_FIELD_KEYS } from 'page/provider/blackduck/BlackDuckModel';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';

const COLUMNS = [{
    key: 'name',
    label: 'Name',
    sortable: true
}, {
    key: 'createdAt',
    label: 'Created At',
    sortable: true
}, {
    key: 'lastUpdated',
    label: 'Last Updated',
    sortable: true
}, {
    key: 'enabled',
    label: 'Enabled',
    sortable: false,
    customCell: ProviderEnabledCell,
    settings: { alignment: 'center' }
}, {
    key: 'editProvider',
    label: 'Edit',
    sortable: false,
    customCell: ProviderEditCell,
    settings: { alignment: 'center' }
}, {
    key: 'copyProvider',
    label: 'Copy',
    sortable: false,
    customCell: ProviderCopyCell,
    settings: { alignment: 'center' }
}];

function ProviderTable({ readonly }) {
    const dispatch = useDispatch();
    const [tableData, setTableData] = useState();
    const [search, setNewSearch] = useState('');
    const [selected, setSelected] = useState([]);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [sortConfig, setSortConfig] = useState();
    const { data } = useSelector((state) => state.provider);

    useEffect(() => {
        dispatch(fetchProviders());
    }, []);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchProviders(url)), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }
        return undefined;
    }, [autoRefresh]);

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    const handleSearchChange = (e) => {
        setNewSearch(e.target.value);
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
                if (a[name] === null)
                    return 1;
                if (b[name] === null)
                    return -1;
                if (a[name] === null && b[name] === null)
                    return 0;
                return (
                    a[name].toString().localeCompare(b[name].toString(), 'en', { numeric: true }) * (direction === 'ASC' ? 1 : -1)
                );
            });
        }
        setTableData(!search ? convertedTableData : convertedTableData.filter((provider) => provider.name.toLowerCase().includes(search.toLowerCase())));
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
            tableActions={() => <PoviderTableActions data={tableData} selected={selected} readonly={readonly} />} />
    );
}

ProviderTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default ProviderTable;
