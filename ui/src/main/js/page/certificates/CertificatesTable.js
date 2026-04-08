import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { fetchCertificates } from 'store/actions/certificates';
import Table from 'common/component/table/Table';
import CertificatesRowActionsCell from 'page/certificates/CertificatesRowActionsCell';
import CertificatesTableActions from 'page/certificates/CertificatesTableActions';
import TimestampCell from 'common/component/table/cell/TimestampCell';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create a Certificate to use this table.'
};

const CertificatesTable = ({ readOnly }) => {
    const dispatch = useDispatch();
    const refreshStatus = JSON.parse(window.localStorage.getItem('CERTIFICATES_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [tableData, setTableData] = useState();
    const [sortConfig, setSortConfig] = useState();
    const [search, setNewSearch] = useState('');
    const [selected, setSelected] = useState([]);
    const certificates = useSelector((state) => state.certificates.data);

    const columns = [{
        key: 'alias',
        label: 'Certificate Alias',
        sortable: true
    }, {
        key: 'lastUpdated',
        label: 'Last Updated',
        sortable: true,
        customCell: TimestampCell
    }, {
        key: 'certificatesRowActions',
        label: '',
        sortable: false,
        customCell: CertificatesRowActionsCell,
        settings: { alignment: 'center', readOnly }
    }];

    useEffect(() => {
        dispatch(fetchCertificates());
    }, []);

    useEffect(() => {
        localStorage.setItem('CERTIFICATES_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchCertificates()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        // Added for 'consistent-return' lint rule
        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (searchValue) => {
        setNewSearch(searchValue);
    };

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
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
        let data = certificates;

        if (sortConfig) {
            const { name, direction } = sortConfig;
            data = [...data].sort((a, b) => {
                if (a[name] === null) return 1;
                if (b[name] === null) return -1;
                if (a[name] === null && b[name] === null) return 0;
                return (
                    a[name].toString().localeCompare(b[name].toString(), 'en', { numeric: true }) * (direction === 'ASC' ? 1 : -1)
                );
            });
        }

        // Endpoints without search will need to look through ui column values
        setTableData(!search ? data : data.filter((certificate) => certificate.alias.toLowerCase().includes(search.toLowerCase())
            || certificate.lastUpdated?.toLowerCase().includes(search.toLowerCase())));
    }, [certificates, search, sortConfig]);

    return (
        <Table
            tableData={tableData}
            columns={columns}
            searchBarPlaceholder="Search Certificates..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            multiSelect
            selected={selected}
            onSelected={onSelected}
            onSort={onSort}
            sortConfig={sortConfig}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <CertificatesTableActions data={tableData} selected={selected} setSelected={setSelected} />}
        />
    );
};

CertificatesTable.propTypes = {
    readOnly: PropTypes.bool.isRequired
}

export default CertificatesTable;
