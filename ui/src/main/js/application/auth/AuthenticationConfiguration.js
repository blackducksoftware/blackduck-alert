import React, { useState } from 'react';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import TextInput from 'common/input/TextInput';
import { AUTHENTICATION_INFO, AUTHENTICATION_LDAP_FIELD_KEYS, AUTHENTICATION_SAML_FIELD_KEYS, AUTHENTICATION_TEST_FIELD_KEYS } from 'application/auth/AuthenticationModel';
import CheckboxInput from 'common/input/CheckboxInput';
import PasswordInput from 'common/input/PasswordInput';
import DynamicSelectInput from 'common/input/DynamicSelectInput';
import CollapsiblePane from 'common/CollapsiblePane';
import UploadFileButtonField from 'common/input/field/UploadFileButtonField';
import ReadOnlyField from 'common/input/field/ReadOnlyField';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import GeneralButton from 'common/button/GeneralButton';
import LabeledField from 'common/input/field/LabeledField';
import BlackDuckSSOConfigImportModal from './BlackDuckSSOConfigImportModal';

const AuthenticationConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, fileRead, fileDelete, fileWrite
}) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, AUTHENTICATION_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [testFieldData, setTestFieldData] = useState({});
    const [showBlackDuckSSOImportModal, setShowBlackDuckSSOImportModal] = useState(false);

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(AUTHENTICATION_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    const testFields = (
        <div>
            <h2>LDAP Configuration</h2>
            <TextInput
                id={AUTHENTICATION_TEST_FIELD_KEYS.username}
                name={AUTHENTICATION_TEST_FIELD_KEYS.username}
                label="User Name"
                description="The user name to test LDAP authentication; if LDAP authentication is enabled."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleTestChange(testFieldData, setTestFieldData)}
                value={testFieldData[AUTHENTICATION_TEST_FIELD_KEYS.username]}
            />
            <PasswordInput
                id={AUTHENTICATION_TEST_FIELD_KEYS.password}
                name={AUTHENTICATION_TEST_FIELD_KEYS.password}
                label="Password"
                description="The password to test LDAP authentication; if LDAP authentication is enabled."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleTestChange(testFieldData, setTestFieldData)}
                value={testFieldData[AUTHENTICATION_TEST_FIELD_KEYS.password]}
            />
            <h2>SAML Configuration</h2>
            <ReadOnlyField
                id={AUTHENTICATION_TEST_FIELD_KEYS.noInput}
                name={AUTHENTICATION_TEST_FIELD_KEYS.noInput}
                label="No Input Required"
                description="No input required here. SAML metadata fields will be tested by the server."
            />
        </div>
    );

    if (!FieldModelUtilities.hasKey(formData, AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned)) {
        const defaultValueModel = FieldModelUtilities.updateFieldModelSingleValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned, true);
        setFormData(defaultValueModel);
    }

    const authTypes = [
        { label: 'Simple', value: 'simple' },
        { label: 'None', value: 'none' },
        { label: 'Digest-MD5', value: 'digest' }
    ];

    const referralTypes = [
        { label: 'Ignore', value: 'ignore' },
        { label: 'Follow', value: 'follow' },
        { label: 'Throw', value: 'throw' }
    ];

    const hasLdapConfig = Object.keys(AUTHENTICATION_LDAP_FIELD_KEYS).some((key) => FieldModelUtilities.hasValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS[key]));
    const hasSamlConfig = Object.keys(AUTHENTICATION_SAML_FIELD_KEYS)
        .filter((key) => key !== 'wantAssertionsSigned')
        .some((key) => FieldModelUtilities.hasValue(formData, AUTHENTICATION_SAML_FIELD_KEYS[key]));

    const importBlackDuckSSOConfigLabel = 'Retrieve Black Duck SAML Configuration';
    const importBlackDuckSSOConfigDescription = 'Fills in some of the form fields based on the SAML configuration from the chosen Black Duck server (if a SAML configuration exists).';

    return (
        <CommonGlobalConfiguration
            label={AUTHENTICATION_INFO.label}
            description="This page allows you to configure user authentication for Alert."
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                displayDelete={false}
                testFields={testFields}
                testFormData={testFieldData}
                setTestFormData={(values) => setTestFieldData(values)}
                buttonIdPrefix={AUTHENTICATION_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                errorHandler={errorHandler}
            >
                <CollapsiblePane
                    id="ldap-configuration"
                    title="LDAP Configuration"
                    expanded={hasLdapConfig}
                >
                    <h2>LDAP Configuration</h2>
                    <CheckboxInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.enabled}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.enabled}
                        label="LDAP Enabled"
                        description="If true, Alert with attempt to authenticate using the LDAP configuration."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.enabled)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.enabled)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.enabled]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.server}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.server}
                        label="LDAP Server"
                        description="The URL of the LDAP server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.server)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.server)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.server]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.managerDn}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.managerDn}
                        label="LDAP Manager DN"
                        description="The distinguished name of the LDAP manager."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.managerDn)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.managerDn)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.managerDn]}
                    />
                    <PasswordInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.managerPassword}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.managerPassword}
                        label="LDAP Manager Password"
                        description="The password of the LDAP manager."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.managerPassword)}
                        isSet={FieldModelUtilities.isFieldModelValueSet(formData, AUTHENTICATION_LDAP_FIELD_KEYS.managerPassword)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.managerPassword)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.managerPassword]}
                    />
                    <DynamicSelectInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.authenticationType}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.authenticationType}
                        label="LDAP Authentication Type"
                        description="The type of authentication required to connect to the LDAP server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        options={authTypes}
                        value={FieldModelUtilities.getFieldModelValues(formData, AUTHENTICATION_LDAP_FIELD_KEYS.authenticationType)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.authenticationType)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.authenticationType]}
                    />
                    <DynamicSelectInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.referral}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.referral}
                        label="LDAP Referral"
                        description="Set the method to handle referrals."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        options={referralTypes}
                        value={FieldModelUtilities.getFieldModelValues(formData, AUTHENTICATION_LDAP_FIELD_KEYS.referral)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.referral)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.referral]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.userSearchBase}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.userSearchBase}
                        label="LDAP User Search Base"
                        description="The part of the LDAP directory in which user searches should be done."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.userSearchBase)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.userSearchBase)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.userSearchBase]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.userSearchFilter}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.userSearchFilter}
                        label="LDAP User Search Filter"
                        description="The filter used to search for user membership."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.userSearchFilter)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.userSearchFilter)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.userSearchFilter]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.userDnPatterns}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.userDnPatterns}
                        label="LDAP User DN Patterns"
                        description="The pattern used used to supply a DN for the user. The pattern should be the name relative to the root DN."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.userDnPatterns)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.userDnPatterns)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.userDnPatterns]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.userAttributes}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.userAttributes}
                        label="LDAP User Attributes"
                        description="User attributes to retrieve for users."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.userAttributes)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.userAttributes)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.userAttributes]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchBase}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchBase}
                        label="LDAP Group Search Base"
                        description="The part of the LDAP directory in which group searches should be done."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchBase)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchBase)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchBase]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchFilter}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchFilter}
                        label="LDAP Group Search Filter"
                        description="The filter used to search for group membership."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValueOrDefault(formData, AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchFilter, 'uniqueMember={0}')}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchFilter)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.groupSearchFilter]}
                    />
                    <TextInput
                        id={AUTHENTICATION_LDAP_FIELD_KEYS.groupRoleAttribute}
                        name={AUTHENTICATION_LDAP_FIELD_KEYS.groupRoleAttribute}
                        label="LDAP Group Role Attribute"
                        description="The ID of the attribute which contains the role name for a group."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValueOrDefault(formData, AUTHENTICATION_LDAP_FIELD_KEYS.groupRoleAttribute)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_FIELD_KEYS.groupRoleAttribute)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_FIELD_KEYS.groupRoleAttribute]}
                    />
                </CollapsiblePane>
                <CollapsiblePane
                    id="saml-configuration"
                    title="SAML Configuration"
                    expanded={hasSamlConfig}
                >
                    <h2>SAML Configuration</h2>
                    <LabeledField label={importBlackDuckSSOConfigLabel} description={importBlackDuckSSOConfigDescription}>
                        <div className="d-inline-flex p-2">
                            <GeneralButton id="blackduck-sso-import-button" onClick={() => setShowBlackDuckSSOImportModal(true)}>Fill Form</GeneralButton>
                        </div>
                    </LabeledField>
                    <CheckboxInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.enabled}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.enabled}
                        label="SAML Enabled"
                        description="If true, Alert will attempt to authenticate using the SAML configuration."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.enabled)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.enabled)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.enabled]}
                    />
                    <CheckboxInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned}
                        label="Sign Assertions"
                        description="If true, signature verification will be performed in SAML when communicating with server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.wantAssertionsSigned]}
                    />
                    <CheckboxInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.forceAuth}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.forceAuth}
                        label="Force Auth"
                        description="If true, the forceAuthn flag is set to true in the SAML request to the IDP. Please check the IDP if this is supported."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.forceAuth)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.forceAuth)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.forceAuth]}
                    />
                    <TextInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl}
                        label="Identity Provider Metadata URL"
                        description="The Metadata URL from the external Identity Provider."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl]}
                    />
                    <UploadFileButtonField
                        id={AUTHENTICATION_SAML_FIELD_KEYS.metadataFile}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.metadataFile}
                        fieldKey={AUTHENTICATION_SAML_FIELD_KEYS.metadataFile}
                        label="Identity Provider Metadata File"
                        description="The file to upload to the server containing the Metadata from the external Identity Provider."
                        readOnly={readonly && !displayTest}
                        permissions={{ read: fileRead, write: fileWrite, delete: fileDelete }}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        buttonLabel="Upload"
                        endpoint="/api/function/upload"
                        csrfToken={csrfToken}
                        capture=""
                        multiple={false}
                        accept={[
                            'text/xml',
                            'application/xml',
                            '.xml'
                        ]}
                        currentConfig={formData}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.metadataFile)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.metadataFile)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.metadataFile]}
                    />
                    <TextInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.entityId}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.entityId}
                        label="Entity ID"
                        description="The Entity ID of the Service Provider. EX: This should be the Audience defined in Okta."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.entityId)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.entityId)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.entityId]}
                    />
                    <TextInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl}
                        label="Entity Base URL"
                        description="This should be the URL of the Alert system."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl]}
                    />
                    <TextInput
                        id={AUTHENTICATION_SAML_FIELD_KEYS.roleAttributeMapping}
                        name={AUTHENTICATION_SAML_FIELD_KEYS.roleAttributeMapping}
                        label="SAML Role Attribute Mapping"
                        description="The SAML attribute in the Attribute Statements that contains the roles for the user logged into Alert. The roles contained in the Attribute Statement can be the role names defined in the mapping fields above."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, AUTHENTICATION_SAML_FIELD_KEYS.roleAttributeMapping)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_SAML_FIELD_KEYS.roleAttributeMapping)}
                        errorValue={errors.fieldErrors[AUTHENTICATION_SAML_FIELD_KEYS.roleAttributeMapping]}
                    />
                </CollapsiblePane>
                <BlackDuckSSOConfigImportModal
                    label={importBlackDuckSSOConfigLabel}
                    csrfToken={csrfToken}
                    readOnly={readonly}
                    show={showBlackDuckSSOImportModal}
                    onHide={() => setShowBlackDuckSSOImportModal(false)}
                    initialSSOFieldData={formData}
                    updateSSOFieldData={(data) => setFormData(data)}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

AuthenticationConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    fileRead: PropTypes.bool,
    fileWrite: PropTypes.bool,
    fileDelete: PropTypes.bool
};

AuthenticationConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    fileRead: true,
    fileWrite: true,
    fileDelete: true
};

export default AuthenticationConfiguration;
