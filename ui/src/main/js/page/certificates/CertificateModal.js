import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { saveCertificate, validateCertificate } from 'store/actions/certificates';
import Modal from 'common/component/modal/Modal';
import ReadOnlyField from 'common/component/input/field/ReadOnlyField';
import TextArea from 'common/component/input/TextArea';
import TextInput from 'common/component/input/TextInput';

const useStyles = createUseStyles({
    content: {
        display: 'flex',
        flexDirection: 'column',
        maxWidth: '80%'
    }
});

const CertificateModal = ({ data, isOpen, toggleModal, type }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [certificateData, setCertificateData] = useState(data ? data : null);

    const fieldErrors = useSelector(state => state.certificates.error.fieldErrors);
    const inProgress = useSelector(state => state.certificates.inProgress);
    const saveStatus = useSelector(state => state.certificates.saveStatus);

    useEffect(() => {
        if ( saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        }
    }, [saveStatus, inProgress]);

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setCertificateData(certData => ({...certData, [label]: value }));
        }
    };

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
            return certificateData[field]
        }
        return ''
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title={type === 'create' ? "Create Certificate" : "Edit Certificate"}
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText={type === 'create' ? "Create New Certificate" : "Confirm Edit"}
        >
            <div className={classes.content}>

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
                    description="The certificate alias name."
                    required
                    onChange={handleOnChange('alias')}
                    value={getDisplayValue('alias')}
                    errorName="alias"
                    errorValue={fieldErrors['alias']}
                />
                <TextArea
                    id="certificate-textAreaId"
                    name="certificateContent"
                    label="Certificate Content"
                    description="The certificate content text."
                    required
                    onChange={handleOnChange('certificateContent')}
                    value={getDisplayValue('certificateContent')}
                    errorName="certificateContent"
                    errorValue={fieldErrors['certificateContent']}
                />
            </div>
        </Modal>
    );
};

export default CertificateModal;