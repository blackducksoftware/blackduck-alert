import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { saveCertificate, validateCertificate } from 'store/actions/certificates';
import Modal from 'common/component/modal/Modal';
import ReadOnlyField from 'common/component/input/field/ReadOnlyField';
import TextArea from 'common/component/input/TextArea';
import TextInput from 'common/component/input/TextInput';

const CertificateModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage }) => {
    const dispatch = useDispatch();
    const [certificateData, setCertificateData] = useState();
    const [showLoader, setShowLoader] = useState(false);
    const { submitText, title } = modalOptions;
    const { saveStatus, error } = useSelector((state) => state.certificates);

    useEffect(() => {
        setCertificateData(data);
    }, [data]);

    function handleClose() {
        toggleModal(false);
    }

    function handleSave() {
        dispatch(saveCertificate(certificateData));
        handleClose();
    }

    function handleSubmit() {
        dispatch(validateCertificate(certificateData));
    }

    function getDisplayValue(field) {
        if (certificateData) {
            return certificateData[field];
        }
        return '';
    }

    useEffect(() => {
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING') {
            setShowLoader(true);
        }

        if (saveStatus === 'VALIDATED') {
            handleSave();
        }

        if (saveStatus === 'SAVED') {
            setShowLoader(false);
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (saveStatus === 'ERROR') {
            setShowLoader(false);
        }
    }, [saveStatus]);

    const handleOnChange = (label) => (
        ({ target: { value } }) => {
            setCertificateData((certData) => ({ ...certData, [label]: value }));
        }
    );

    return (
        <Modal
            isOpen={isOpen}
            size="lg"
            title={title}
            submitText={submitText}
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            showLoader={showLoader}
        >
            { certificateData?.lastUpdated ? (
                <ReadOnlyField
                    id="lastUpdated-readOnlyFieldId"
                    label="Last Updated"
                    value={getDisplayValue('lastUpdated')}
                />
            ) : null}

            <TextInput
                id="alias-textInputId"
                name="alias"
                label="Alias"
                customDescription="The certificate alias name."
                required
                onChange={handleOnChange('alias')}
                value={getDisplayValue('alias')}
                errorName="alias"
                errorValue={error.fieldErrors.alias}
            />
            <TextArea
                id="certificate-textAreaId"
                name="certificateContent"
                label="Certificate Content"
                customDescription="The certificate content text."
                required
                onChange={handleOnChange('certificateContent')}
                value={getDisplayValue('certificateContent')}
                errorName="certificateContent"
                errorValue={error.fieldErrors.certificateContent}
            />
        </Modal>
    );
};

CertificateModal.propTypes = {
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    data: PropTypes.shape({
        alias: PropTypes.string,
        certificateContent: PropTypes.string,
        lastUpdated: PropTypes.string,
        id: PropTypes.string
    }),
    modalOptions: PropTypes.shape({
        submitText: PropTypes.string,
        title: PropTypes.string
    }),
    setStatusMessage: PropTypes.func,
    successMessage: PropTypes.string
};

export default CertificateModal;
