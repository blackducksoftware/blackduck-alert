import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import CollapsiblePane from 'common/component/CollapsiblePane';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';
import CheckboxInput from 'common/component/input/CheckboxInput';
import RadioInput from '../../common/component/input/RadioInput';
import TextInput from 'common/component/input/TextInput';
import UploadFileButtonField from 'common/component/input/field/UploadFileButtonField';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS } from 'application/auth/AuthenticationModel';
import BlackDuckSSOConfigImportModal from './BlackDuckSSOConfigImportModal';
import LabeledField from '../../common/component/input/field/LabeledField';
import GeneralButton from '../../common/component/button/GeneralButton';

const radioOptions = [{
    name: 'url',
    value: 'URL',
    label: 'URL'
}, {
    name: 'xml',
    value: 'FILE',
    label: 'XML File'
}]

const useStyles = createUseStyles({
    samlForm: {
        padding: [0, '20px']
    },
    fillForm: {
        padding: '0.5rem',
        display: 'inline-flex'
    }
});

const SamlForm = ({ csrfToken, errorHandler, readonly, displayTest, fileDelete, fileRead, fileWrite }) => {
    const classes = useStyles();
    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [samlEnabled, setSamlEnabled] = useState(FieldModelUtilities.getFieldModelBooleanValue(formData, AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled));
    const [showBlackDuckSSOImportModal, setShowBlackDuckSSOImportModal] = useState(false);
    const samlRequestUrl = `${ConfigurationRequestBuilder.AUTHENTICATION_SAML_API_URL}`;

    const importBlackDuckSSOConfigLabel = 'Retrieve Black Duck SAML Configuration';
    const importBlackDuckSSOConfigDescription = 'Fills in some of the form fields based on the SAML configuration from the chosen Black Duck server (if a SAML configuration exists).';

    useEffect(() => {
        setSamlEnabled(formData.enabled);        
    }, [formData.enabled])

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(samlRequestUrl, csrfToken);
        const data = await response.json();
        if (data) {
            setFormData(data);
        }

        if (data.status === 404) {
            setFormData({ ...data, metadataMode: 'URL' });
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
        if (formData.status === 404) {
            delete formData.status;
            delete formData.message;
            delete formData.error;
            delete formData.path;
        }
        setFormData(formData);
        return ConfigurationRequestBuilder.createValidateRequest(samlRequestUrl, csrfToken, formData);
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
                validateRequest={handleValidation}
                displayDelete={formData.status !== 404}
                errorHandler={errorHandler}
                deleteLabel="Delete SAML Configuration"
                submitLabel="Save SAML Configuration"
                testLabel="Test SAML Configuration"
            >
                <CheckboxInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.enabled}
                    label="SAML Enabled"
                    description="If true, Alert will attempt to authenticate using the SAML configuration."
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
                    description="Select the type of SAML authentication."
                    readOnly={!samlEnabled}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    radioOptions={radioOptions}
                    checked={formData.metadataMode}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataMode)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataMode]}
                />

                { (formData.metadataMode === 'URL' || !formData.metadataMode)
                    && (
                        <>
                            <TextInput
                                id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl}
                                name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl}
                                label="Identity Provider Metadata URL"
                                description="The Metadata URL from the external Identity Provider."
                                readOnly={!samlEnabled}
                                onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                                value={formData[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl] || undefined}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl)}
                                errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataUrl]}
                            />
                            <LabeledField label={importBlackDuckSSOConfigLabel} description={importBlackDuckSSOConfigDescription}>
                                <div className={classes.fillForm}>
                                    <GeneralButton id="blackduck-sso-import-button" disabled={!samlEnabled} onClick={() => setShowBlackDuckSSOImportModal(true)}>Fill Form</GeneralButton>
                                </div>
                            </LabeledField>
                            <BlackDuckSSOConfigImportModal
                                label={importBlackDuckSSOConfigLabel}
                                csrfToken={csrfToken}
                                readOnly={readonly || !samlEnabled}
                                show={showBlackDuckSSOImportModal}
                                onHide={() => setShowBlackDuckSSOImportModal(false)}
                                initialSSOFieldData={formData}
                                updateSSOFieldData={(data) => setFormData(data)}
                            />
                        </>
                    )
                }

                { formData.metadataMode === 'FILE' &&
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFilePath}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFilePath}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFilePath}
                        label="Identity Provider Metadata File"
                        description="The file to upload to the server containing the Metadata from the external Identity Provider."
                        readOnly={!samlEnabled && !displayTest}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload"
                        endpoint="/alert/api/authentication/saml/upload/metadata"
                        customEndpoint="/alert/api/authentication/saml/upload/metadata"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={[
                            'text/xml',
                            'application/xml',
                            '.xml'
                        ]}
                        currentConfig={formData}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFilePath)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFilePath)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.metadataFilePath]}
                    />
                }

                <CheckboxInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.wantAssertionsSigned}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.wantAssertionsSigned}
                    label="Sign Assertions"
                    description="If true, signature verification will be performed in SAML when communicating with server."
                    readOnly={!samlEnabled}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    isChecked={formData.wantAssertionsSigned}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.wantAssertionsSigned)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.wantAssertionsSigned]}
                />
                <CheckboxInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth}
                    label="Force Auth"
                    description="If true, the forceAuthn flag is set to true in the SAML request to the IDP. Please check the IDP if this is supported."
                    readOnly={!samlEnabled}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    isChecked={formData.forceAuth}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.forceAuth]}
                />
                <TextInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityId}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityId}
                    label="Entity ID"
                    description="The Entity ID of the Service Provider. EX: This should be the Audience defined in Okta."
                    readOnly={!samlEnabled}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityId] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityId)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityId]}
                />
                <TextInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityBaseUrl}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityBaseUrl}
                    label="Entity Base URL"
                    description="This should be the URL of the Alert system."
                    readOnly={!samlEnabled}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityBaseUrl] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityBaseUrl)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.entityBaseUrl]}
                />
                <TextInput
                    id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.roleAttributeMapping}
                    name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.roleAttributeMapping}
                    label="SAML Role Attribute Mapping"
                    description="The SAML attribute in the Attribute Statements that contains the roles for the user logged into Alert. The roles contained in the Attribute Statement can be the role names defined in the mapping fields above."
                    readOnly={!samlEnabled}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.roleAttributeMapping)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.roleAttributeMapping)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.roleAttributeMapping]}
                />
                <CollapsiblePane
                    id="authentication-saml-advanced"
                    title="Advanced SAML Configuration"
                    expanded={false}
                >
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFilePath}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFilePath}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFilePath}
                        label="Encryption Cert Metadata File"
                        description="Upload an Encryption type cert file to configure SAML."
                        readOnly={!samlEnabled && !displayTest}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload"
                        endpoint="/alert/api/authentication/saml/upload/encryption-cert"
                        customEndpoint="/alert/api/authentication/saml/upload/encryption-cert"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={[
                            'text/xml',
                            'application/xml',
                            '.xml'
                        ]}
                        currentConfig={formData}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFilePath)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFilePath)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.encryptionCertFilePath]}
                    />
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFilePath}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFilePath}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFilePath}
                        label="Signing Cert Metadata File"
                        description="Upload a Signing type cert file to configure SAML."
                        readOnly={!samlEnabled && !displayTest}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload"
                        endpoint="/alert/api/authentication/saml/upload/signing-cert"
                        customEndpoint="/alert/api/authentication/saml/upload/signing-cert"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={[
                            'text/xml',
                            'application/xml',
                            '.xml'
                        ]}
                        currentConfig={formData}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFilePath)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFilePath)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.signingCertFilePath]}
                    />
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFilePath}
                        name={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFilePath}
                        fieldKey={AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFilePath}
                        label="Verification Cert Metadata File"
                        description="Upload an Verification type cert file to configure SAML."
                        readOnly={!samlEnabled && !displayTest}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                        buttonLabel="Upload"
                        endpoint="/alert/api/authentication/saml/upload/verification-cert"
                        customEndpoint="/alert/api/authentication/saml/upload/verification-cert"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={[
                            'text/xml',
                            'application/xml',
                            '.xml'
                        ]}
                        currentConfig={formData}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFilePath)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFilePath)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS.verificationCertFilePath]}
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
    displayTest: PropTypes.bool,
    fileRead: PropTypes.bool,
    fileWrite: PropTypes.bool,
    fileDelete: PropTypes.bool
};

export default SamlForm;
