import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCertificates } from 'store/actions/certificates';
import Table from 'common/component/table/Table';
import EditCertificateCell from 'page/certificates/EditCertificateCell';
import CertificatesTableActions from 'page/certificates/CertificatesTableActions';

const COLUMNS = [{
    key: 'alias',
    label: 'Certificate Alias',
    sortable: true
}, {
    key: 'lastUpdated',
    label: 'Last Updated',
    sortable: true
}, {
    key: 'editCertificate',
    label: 'Edit Certificate',
    sortable: false,
    customCell: EditCertificateCell,
    settings: { alignment: 'right' }
}];

const CertificatesTable = () => {
    const dispatch = useDispatch();
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [search, setNewSearch] = useState('');
    const [selected, setSelected] = useState([]);
    const certificates = useSelector((state) => state.certificates.data);

    useEffect(() => {
        dispatch(fetchCertificates());
    }, []);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchCertificates()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        // Added for 'consistent-return' lint rule
        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (e) => {
        setNewSearch(e.target.value);
    };

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    const getCertificates = () => (
        !search ? certificates : certificates.filter((certificate) => certificate.alias.toLowerCase().includes(search.toLowerCase()))
    );

    return (
        <Table
            tableData={getCertificates()}
            columns={COLUMNS}
            searchBarPlaceholder="Search Certificates..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            multiSelect
            selected={selected}
            onSelected={onSelected}
            tableActions={() => <CertificatesTableActions data={getCertificates()} selected={selected} />}
        />
    );
};

export default CertificatesTable;
