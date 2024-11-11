import Modal from 'common/component/modal/Modal';
import Table from 'common/component/table/Table';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import PropTypes from 'prop-types';
import React, { useEffect, useState } from 'react';
import { DISTRIBUTION_COMMON_FIELD_KEYS, DISTRIBUTION_URLS } from './DistributionModel';

const KEY_CELL = "name"

const COLUMNS = [{
    key: KEY_CELL,
    label: 'Project Name',
    sortable: false
}];

// Note: this Modal is near identical to <AdditionalEmailAddressesModal>.  Consider consolidation as a future IMPROVEMENT
export default function ProjectSelectModal({ isOpen, handleClose, csrfToken, projectRequestBody, handleSubmit, formData }) {
    const [data, setData] = useState({});
    const [pageNumber, setPageNumber] = useState(0);
    const [pageSize, setPageSize] = useState(25);
    const [searchTerm, setSearchTerm] = useState('');
    const [selected, setSelected] = useState(formData);
    const [showLoader, setShowLoader] = useState(false);
    const [errorMessage, setErrorMessage] = useState({ message: 'No Projects Available' });
    const selectedProjectNames = selected.map((project) => project.label);

    function handleFieldValueChange(option) {
        const name = DISTRIBUTION_COMMON_FIELD_KEYS.configuredProjects;
        handleSubmit({
            target: {
                name,
                value: option
            }
        });
        handleClose();
    }

    useEffect(() => {
        function fetchProjectList() {
            const apiUrl = `${DISTRIBUTION_URLS.distributionSelectTableUrl}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`;
            const request = createNewConfigurationRequest(apiUrl, csrfToken, projectRequestBody());
            setShowLoader(true);
            request.then((response) => {
                if (response.ok) {
                    response.json().then((resJson) => {
                        setData(resJson);
                        setShowLoader(false);
                    });
                } else {
                    response.json().then((resJson) => {
                        setErrorMessage(resJson);
                        setShowLoader(false);
                    });
                }
            });
        }

        fetchProjectList();
    }, [pageNumber, pageSize, searchTerm, csrfToken, projectRequestBody]);

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="lg"
                title="Projects"
                closeModal={handleClose}
                handleSubmit={() => { handleFieldValueChange(selected); }}
                submitText="Save"
            >
                <div style={{ paddingLeft: 15, paddingRight: 15 }}>
                    <Table
                        tableData={data?.models}
                        columns={COLUMNS}
                        multiSelect
                        searchBarPlaceholder="Filter Projects..."
                        handleSearchChange={(newSearchTerm) => {
                            setPageNumber(0);
                            setSearchTerm(newSearchTerm);
                        }}
                        selected={selectedProjectNames}
                        onSelected={(rowID, rowData) => {
                            if (selectedProjectNames.includes(rowData.name)) {
                                setSelected(selected.filter((project) => project.value !== rowData.href));
                            } else {
                                setSelected([...selected, { label: rowData.name, value: rowData.href }]);
                            }
                        }}
                        onPage={(newPageNumber) => {
                            setPageNumber(newPageNumber);
                        }}
                        onPageSize={(newPageSize) => {
                            setPageNumber(0);
                            setPageSize(newPageSize);
                        }}
                        pageSize={pageSize}
                        showPageSize
                        data={data}
                        emptyTableConfig={errorMessage}
                        cellId={KEY_CELL}
                        defaultSearchValue=""
                        isLoading={showLoader}
                    />
                </div>
            </Modal>
        </>
    );
}

ProjectSelectModal.propTypes = {
    isOpen: PropTypes.bool,
    handleClose: PropTypes.func,
    csrfToken: PropTypes.string,
    projectRequestBody: PropTypes.func,
    handleSubmit: PropTypes.func,
    formData: PropTypes.oneOf([PropTypes.object, PropTypes.array])
};
