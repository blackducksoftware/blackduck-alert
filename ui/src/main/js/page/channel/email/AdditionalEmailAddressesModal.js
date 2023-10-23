import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Modal from 'common/component/modal/Modal';
import Table from 'common/component/table/Table';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { EMAIL_DISTRIBUTION_FIELD_KEYS } from 'page/channel/email/EmailModels';

const COLUMNS = [{
    key: 'emailAddress',
    label: 'Email Address',
    sortable: false
}];

export default function AdditionalEmailAddressesModal({ isOpen, handleClose, csrfToken, createAdditionalEmailRequestBody, handleSubmit, formData }) {
    const [data, setData] = useState({});
    const [pageNumber, setPageNumber] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchTerm, setSearchTerm] = useState('');
    const [selected, setSelected] = useState(FieldModelUtilities.getFieldModelValues(formData, EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses));
    const [errorMessage, setErrorMessage] = useState({ message: 'No Email Addresses Available' });

    function fetchAdditionalEmails() {
        const apiUrl = `/alert/api/function/email.additional.addresses?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`;
        const request = createNewConfigurationRequest(apiUrl, csrfToken, createAdditionalEmailRequestBody());
        request.then((response) => {
            if (response.ok) {
                response.json().then((resJson) => {
                    setData(resJson);
                });
            } else {
                response.json().then((resJson) => {
                    setErrorMessage(resJson);
                });
            }
        });
    }

    const handleFieldValueChange = (option) => {
        const name = EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses;
        handleSubmit({
            target: {
                name,
                value: option
            }
        });
        handleClose();
    };

    useEffect(() => {
        fetchAdditionalEmails();
    }, [pageNumber, pageSize, searchTerm]);

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="lg"
                title="Additional Email Addresses"
                closeModal={handleClose}
                handleSubmit={() => { handleFieldValueChange(selected); }}
                submitText="Submit"
            >
                <div style={{ paddingLeft: 15, paddingRight: 15 }}>
                    <Table
                        tableData={data?.models}
                        columns={COLUMNS}
                        multiSelect
                        searchBarPlaceholder="Search Email Address..."
                        handleSearchChange={(newSearchTerm) => {
                            setSearchTerm(newSearchTerm);
                        }}
                        selected={selected}
                        onSelected={(selectedRow) => { setSelected(selectedRow); }}
                        onPage={(newPageNumber) => { setPageNumber(newPageNumber); }}
                        onPageSize={(newPageSize) => { setPageSize(newPageSize); }}
                        pageSize={data?.pageSize}
                        showPageSize
                        data={data}
                        emptyTableConfig={errorMessage}
                        cellId="emailAddress"
                        defaultSearchValue=""
                    />
                </div>
            </Modal>
        </>
    );
}

AdditionalEmailAddressesModal.propTypes = {
    isOpen: PropTypes.bool,
    handleClose: PropTypes.func,
    csrfToken: PropTypes.string,
    createAdditionalEmailRequestBody: PropTypes.func,
    handleSubmit: PropTypes.func,
    formData: PropTypes.object
};
