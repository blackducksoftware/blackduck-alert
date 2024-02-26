import React, { useState } from 'react';
import PropTypes from 'prop-types';

import { createUseStyles } from 'react-jss';
import { createEmptyErrorObject } from 'common/util/httpErrorUtilities';
import { CLIENT_CERTIFICATE_URL, createDeleteRequest, createNewConfigurationRequest, createReadRequest } from 'common/util/configurationRequestBuilder';

import PasswordInput from 'common/component/input/PasswordInput';
import TextArea from 'common/component/input/TextArea';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';

const useStyles = createUseStyles({
    layout: {
        margin: '20px 20px 0 20px'
    },
    certificateHeader: {
        fontSize: '17px',
        borderBottom: 'solid 1px #dddddd',
        marginBottom: '16px',
        marginTop: '4px',
        paddingBottom: '4px'
    },
    certificateDescription: {
        marginBottom: '20px'
    },
    connectionDialog: {
        backgroundColor: '#ADE8F4',
        color: '#271F30',
        border: 'solid 1px #00B4D8',
        borderRadius: '3px',
        width: '82%',
        margin: '0 auto 20px',
        padding: '16px',
        boxShadow: 'rgba(0, 0, 0, 0.1) 0px 4px 6px -1px, rgba(0, 0, 0, 0.06) 0px 2px 4px -1px'
    }
});

const MTLSCertificateLayout = ({ csrfToken, errorHandler }) => {
    const classes = useStyles();
    const [errors, setErrors] = useState(createEmptyErrorObject());
    const [formData, setFormData] = useState({});
    const [isDeleteDisabled, setIsDeleteDisabled] = useState(true);
    const [isSaveDisabled, setIsSaveDisabled] = useState(false);

    function getDisplayValue(field) {
        if (formData) {
            return formData[field];
        }
        return '';
    }

    const handleOnChange = (label) => (
        ({ target: { value } }) => {
            setFormData((clientCertificateData) => {
                if (value.trim() === '') {
                    const { [label]: removeKey, ...data } = clientCertificateData;
                    return data;
                }

                return { ...clientCertificateData, [label]: value };
            });
        }
    );

    const fetchData = async () => {
        const response = await createReadRequest(CLIENT_CERTIFICATE_URL, csrfToken);
        if (response.ok) {
            setFormData({
                keyPassword: '***********',
                keyContent: '***********',
                clientCertificateContent: '***********'
            });
            setIsDeleteDisabled(false);
            setIsSaveDisabled(true);
        }
    };

    const formHasData = (formData.hasOwnProperty('keyPassword') || formData.hasOwnProperty('keyContent') || formData.hasOwnProperty('clientCertificateContent'));
    const certificateEnabled = formHasData && isSaveDisabled;

    function onSaveSuccess() {
        setIsSaveDisabled(true);
    }

    function onDeleteSuccess() {
        // We have to manually set form data back to an empty object since a GET request will return a 404.  If the GET request returned an empty object and not a 404
        // this onDeleteSuccess could be reduced to `fetchData()`
        setFormData({});
        setIsDeleteDisabled(true);
        setIsSaveDisabled(false);
    }

    return (
        <section className={classes.layout}>
            <header className={classes.certificateHeader}>Client Certificate</header>
            <div className={classes.certificateDescription}>Filling out the fields below will enable Mutual TLS (MTLS) communication, if required by any one of the Channels. This certificate will be used to provide valid authentication between a Channel and Alert.</div>

            { certificateEnabled && (
                <div className={classes.connectionDialog}>
                    Client Certificate currently enabled.  Delete the current configuration to connect a new one.
                </div>
            )}

            <ConcreteConfigurationForm
                formDataId="MTLSFormID"
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchData}
                updateRequest={() => createNewConfigurationRequest(CLIENT_CERTIFICATE_URL, csrfToken, formData)}
                deleteRequest={() => createDeleteRequest(CLIENT_CERTIFICATE_URL, csrfToken)}
                afterSuccessfulSave={onSaveSuccess}
                postDeleteAction={onDeleteSuccess}
                ignoreValidation
                displayTest={false}
                errorHandler={errorHandler}
                deleteLabel="Delete"
                submitLabel="Save"
                isSaveDisabled={!formHasData || isSaveDisabled}
                isDeleteDisabled={isDeleteDisabled}
            >
                <PasswordInput
                    id="keyPassword"
                    name="keyPassword"
                    label="Key Password"
                    description="The passphrase/password used for the encryption/decryption of the client certificate private key."
                    onChange={handleOnChange('keyPassword')}
                    value={getDisplayValue('keyPassword')}
                    errorName="keyPassword"
                    errorValue={errors.fieldErrors.keyPassword}
                    isDisabled={!isDeleteDisabled}
                />
                <TextArea
                    id="keyContent"
                    name="keyContent"
                    label="Key Content"
                    description="The full text content of the client certificate private key."
                    readOnly={false}
                    onChange={handleOnChange('keyContent')}
                    value={getDisplayValue('keyContent')}
                    errorName="keyContent"
                    errorValue={errors.fieldErrors.keyContent}
                    sizeClass="col-sm-8 flex-column p-2"
                    isDisabled={!isDeleteDisabled}
                    rows={certificateEnabled ? 1 : 8}
                />
                <TextArea
                    id="clientCertificateContent"
                    name="clientCertificateContent"
                    label="Certificate Content"
                    description="The full text content of the client certificate."
                    onChange={handleOnChange('clientCertificateContent')}
                    value={getDisplayValue('clientCertificateContent')}
                    errorName="clientCertificateContent"
                    errorValue={errors.fieldErrors.clientCertificateContent}
                    sizeClass="col-sm-8 flex-column p-2"
                    isDisabled={!isDeleteDisabled}
                    rows={certificateEnabled ? 1 : 8}
                />
            </ConcreteConfigurationForm>
        </section>
    );
};

MTLSCertificateLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired
};

export default MTLSCertificateLayout;
