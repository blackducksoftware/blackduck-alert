import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

import { AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS } from 'application/auth/AuthenticationModel';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';

import CollapsiblePane from 'common/component/CollapsiblePane';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';

import CheckboxInput from 'common/component/input/CheckboxInput';
import LabeledField from 'common/component/input/field/LabeledField';
import RadioInput from 'common/component/input/RadioInput';
import TextInput from 'common/component/input/TextInput';
import UploadFileButtonField from 'common/component/input/field/UploadFileButtonField';
import Button from 'common/component/button/Button';

import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';

import BlackDuckSSOConfigImportModal from './BlackDuckSSOConfigImportModal';

const CERT_FILE_TYPES = ['.crt', '.cer', '.der', '.cert', '.pem'];
const PRIVATE_KEY_FILE_TYPES = ['.p8', '.p8e', '.pem'];
const XML_FILE_TYPES = ['text/xml', 'application/xml', '.xml'];

const radioOptions = [{
    name: 'url',
    value: 'URL',
    label: 'URL'
}, {
    name: 'xml',
    value: 'FILE',
    label: 'XML File'
}];

const useStyles = createUseStyles({
    samlForm: {
        padding: [0, '20px']
    },
    fillForm: {
        padding: '0.5rem',
        display: 'inline-flex'
    }
});

const SamlForm = ({ csrfToken, errorHandler, readonly, fileDelete, fileRead, fileWrite }) => {
    const classes = useStyles();
    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [showBlackDuckSSOImportModal, setShowBlackDuckSSOImportModal] = useState(false);
    const [providerModel, setProviderModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key));

    const [triggerClearUploaded, setTriggerClearUploaded] = useState(false);
    const samlRequestUrl = `${ConfigurationRequestBuilder.AUTHENTICATION_SAML_API_URL}`;

    const importBlackDuckSSOConfigLabel = 'Retrieve Black Duck SAML Configuration';
    const importBlackDuckSSOConfigDescription = 'Fills the following fields based on the SAML configuration from the chosen Black Duck server (if a SAML configuration exists): SAML Enabled, Identity Provider Metadata URL';

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(samlRequestUrl, csrfToken);
        const data = await response.json();

        if (data.status === 404 || !data.metadataMode) {
            setFormData({ ...data, metadataMode: 'URL' });
        } else {
            setFormData(data);
        }
    };

    function updateData() {
        return ConfigurationRequestBuilder.createUpdateRequest(samlRequestUrl, csrfToken, undefined, formData);
    }

    function deleteData() {
        return ConfigurationRequestBuilder.createDeleteRequest(samlRequestUrl, csrfToken);
    }

    function postData() {
        return ConfigurationRequestBuilder.createNewConfigurationRequest(samlRequestUrl, csrfToken, formData);
    }

    function handleValidation() {
        return ConfigurationRequestBuilder.createValidateRequest(samlRequestUrl, csrfToken, formData);
    }

    function clearUploadedButtonsPostDelete() {
        setTriggerClearUploaded(!triggerClearUploaded);
    }

    function handleShowModal() {
        setShowBlackDuckSSOImportModal(true);
    }

    return (
        <div className={classes.samlForm}>
            <h2>SAML Configuration</h2>
            <ConcreteConfigurationForm
                formDataId={formData.id}
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchData}
                deleteRequest={deleteData}
                updateRequest={updateData}
                createRequest={postData}
                testRequest={handleValidation}
                validateRequest={handleValidation}
                displayDelete={formData.status !== 404}
                errorHandler={errorHandler}
                deleteLabel="Delete SAML Configuration"
                submitLabel="Save SAML Configuration"
                testLabel="Validate SAML Configuration"
                buttonIdPrefix="saml-config"
                postDeleteAction={clearUploadedButtonsPostDelete}
            >
                <CheckboxInput
                    id={`saml-enabled${AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled}`}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled}
                    label="SAML Enabled"
                    description="If true, Alert will present a Login with SAML option using the SAML configuration."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    isChecked={formData.enabled}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled]}
                />
                <RadioInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataMode}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataMode}
                    label="SAML Identity Provider"
                    description="Select the type of SAML metadata."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    radioOptions={radioOptions}
                    checked={formData.metadataMode}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataMode)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataMode]}
                    required
                />

                { (formData.metadataMode === 'URL' || !formData.metadataMode) && (
                    <>
                        <TextInput
                            id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl}
                            name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl}
                            label="Identity Provider Metadata URL"
                            description="The metadata URL from the external Identity Provider."
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                            value={formData[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl] || undefined}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl)}
                            errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl]}
                            required
                        />
                        <LabeledField label={importBlackDuckSSOConfigLabel} description={importBlackDuckSSOConfigDescription}>
                            <div className={classes.fillForm}>
                                <Button
                                    id="blackduck-sso-import-button"
                                    onClick={handleShowModal}
                                    text="Fill Form"
                                />
                            </div>
                        </LabeledField>
                        {showBlackDuckSSOImportModal && (
                            <BlackDuckSSOConfigImportModal
                                csrfToken={csrfToken}
                                initialSSOFieldData={formData}
                                isOpen={showBlackDuckSSOImportModal}
                                providerModel={providerModel}
                                readOnly={readonly}
                                setProviderModel={setProviderModel}
                                toggleModal={setShowBlackDuckSSOImportModal}
                                updateSSOFieldData={(data) => setFormData(data)}
                            />
                        )}
                    </>
                )}

                { formData.metadataMode === 'FILE' && (
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFileName}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFileName}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFileName}
                        label="Identity Provider Metadata File"
                        description="The file to upload to the server containing the metadata from the external Identity Provider."
                        readOnly={readonly}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload"
                        endpoint="/alert/api/authentication/saml/upload/metadata"
                        customEndpoint="/alert/api/authentication/saml/upload/metadata"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={XML_FILE_TYPES}
                        currentConfig={formData}
                        value={formData.metadataFileName}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFileName)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFileName]}
                        required
                        valueToCheckFileExistsOnChange={triggerClearUploaded}
                    />
                )}
                <CheckboxInput
                    id={`saml-force-auth${AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth}`}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth}
                    label="Force Auth"
                    description="If true, the forceAuthn flag is set to true in the SAML request to the IDP. Check the Identity Provider settings to see if this is supported."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    isChecked={formData.forceAuth}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth]}
                />
                <CollapsiblePane
                    id="authentication-saml-advanced"
                    title="Advanced SAML Configuration"
                    expanded={false}
                    isDisabled={readonly}
                >
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFileName}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFileName}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFileName}
                        label="Encryption Certificate File"
                        description="Upload an Encryption type certificate file to configure SAML."
                        readOnly={readonly}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload Certificate"
                        endpoint="/alert/api/authentication/saml/upload/encryption-cert"
                        customEndpoint="/alert/api/authentication/saml/upload/encryption-cert"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={CERT_FILE_TYPES}
                        currentConfig={formData}
                        value={formData.encryptionCertFileName}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFileName)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFileName]}
                        valueToCheckFileExistsOnChange={triggerClearUploaded}
                    />
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionPrivateKeyFileName}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionPrivateKeyFileName}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionPrivateKeyFileName}
                        label="Encryption Cert Private Key File"
                        description="Upload a PKCS8 Encryption private key file for the encryption certificate."
                        readOnly={readonly}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload Private Key"
                        endpoint="/alert/api/authentication/saml/upload/encryption-private-key"
                        customEndpoint="/alert/api/authentication/saml/upload/encryption-private-key"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={PRIVATE_KEY_FILE_TYPES}
                        currentConfig={formData}
                        value={formData.encryptionPrivateKeyFileName}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionPrivateKeyFileName)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionPrivateKeyFileName]}
                        valueToCheckFileExistsOnChange={triggerClearUploaded}
                    />

                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFileName}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFileName}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFileName}
                        label="Signing Certificate File"
                        description="Upload a Signing type certificate file to configure SAML."
                        readOnly={readonly}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload Certificate"
                        endpoint="/alert/api/authentication/saml/upload/signing-cert"
                        customEndpoint="/alert/api/authentication/saml/upload/signing-cert"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={CERT_FILE_TYPES}
                        currentConfig={formData}
                        value={formData.signingCertFileName}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFileName)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFileName]}
                        valueToCheckFileExistsOnChange={triggerClearUploaded}
                    />
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingPrivateKeyFileName}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingPrivateKeyFileName}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingPrivateKeyFileName}
                        label="Signing Cert Private Key File"
                        description="Upload a PKCS8 Signing private key file for the signing certificate."
                        readOnly={readonly}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload Private Key"
                        endpoint="/alert/api/authentication/saml/upload/signing-private-key"
                        customEndpoint="/alert/api/authentication/saml/upload/signing-private-key"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={PRIVATE_KEY_FILE_TYPES}
                        currentConfig={formData}
                        value={formData.signingPrivateKeyFileName}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingPrivateKeyFileName)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingPrivateKeyFileName]}
                        valueToCheckFileExistsOnChange={triggerClearUploaded}
                    />

                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFileName}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFileName}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFileName}
                        label="Verification Certificate File"
                        description="Upload an Verification type certificate file to configure SAML."
                        readOnly={readonly}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload Certificate"
                        endpoint="/alert/api/authentication/saml/upload/verification-cert"
                        customEndpoint="/alert/api/authentication/saml/upload/verification-cert"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={CERT_FILE_TYPES}
                        currentConfig={formData}
                        value={formData.verificationCertFileName}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFileName)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFileName]}
                        valueToCheckFileExistsOnChange={triggerClearUploaded}
                    />
                </CollapsiblePane>
            </ConcreteConfigurationForm>
        </div>
    );
};

SamlForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readonly: PropTypes.bool,
    fileRead: PropTypes.bool,
    fileWrite: PropTypes.bool,
    fileDelete: PropTypes.bool
};

export default SamlForm;
