import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';

import CheckboxInput from 'common/component/input/CheckboxInput';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';

import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';

import { AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS, AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS } from 'application/auth/AuthenticationModel';

const useStyles = createUseStyles({
    ldapForm: {
        padding: [0, '20px']
    }
});

const AUTH_TYPES = [
    { label: 'Digest-MD5', value: 'digest' },
    { label: 'None', value: 'none' },
    { label: 'Simple', value: 'simple' }
];

const REFERRAL_TYPES = [
    { label: 'Follow', value: 'follow' },
    { label: 'Ignore', value: 'ignore' },
    { label: 'Throw', value: 'throw' }
];

const LdapForm = ({ csrfToken, errorHandler, readonly, displayTest }) => {
    const classes = useStyles();
    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [testFormData, setTestFormData] = useState({});
    const ldapRequestUrl = `${ConfigurationRequestBuilder.AUTHENTICATION_LDAP_API_URL}`;

    function processFormData() {
        if (formData?.authenticationType && formData?.authenticationType.length > 0 && Array.isArray(formData?.authenticationType)) {
            formData.authenticationType = formData.authenticationType[0];
        } else {
            formData.authenticationType = 'simple';
        }

        if (formData?.referral && formData?.referral.length > 0 && Array.isArray(formData?.referral)) {
            formData.referral = formData.referral[0];
        }

        if (formData.managerPassword && !formData.isManagerPasswordSet) {
            formData.isManagerPasswordSet = true;
        }

        setFormData(formData);
    }

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(ldapRequestUrl, csrfToken);
        const data = await response.json();

        if (data.status !== 404) {
            setFormData(data);
            setTestFormData({ ldapConfigModel: data });
        } else {
            setFormData({});
        }
    };

    function updateData() {
        return ConfigurationRequestBuilder.createUpdateRequest(ldapRequestUrl, csrfToken, undefined, formData);
    }

    function deleteData() {
        return ConfigurationRequestBuilder.createDeleteRequest(ldapRequestUrl, csrfToken);
    }

    function postData() {
        return ConfigurationRequestBuilder.createNewConfigurationRequest(ldapRequestUrl, csrfToken, formData);
    }

    function handleTestRequest() {
        return ConfigurationRequestBuilder.createTestRequest(ldapRequestUrl, csrfToken, testFormData);
    }

    function testButtonClicked() {
        processFormData();
        setTestFormData({ ldapConfigModel: formData });
    }

    function handleValidation() {
        processFormData();
        return ConfigurationRequestBuilder.createValidateRequest(ldapRequestUrl, csrfToken, formData);
    }

    const isTestFormComplete = () => {
        // This check will determine whether we enable or disable the Test Submit button within the Test Configuration modal
        return (testFormData[AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPUsername]?.length > 0 &&
            testFormData[AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPPassword]?.length > 0);
    };

    // Revise this to its own modal when we overhaul the modal changes.
    const testFields = (
        <div>
            <h2>LDAP Configuration</h2>
            <TextInput
                id={AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPUsername}
                name={AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPUsername}
                label="User Name"
                customDescription="The user name to test LDAP authentication; if LDAP authentication is enabled."
                readOnly={false}
                onChange={FieldModelUtilities.handleConcreteModelChange(testFormData, setTestFormData)}
                value={testFormData[AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPUsername]}
            />
            <PasswordInput
                id={AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPPassword}
                name={AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPPassword}
                label="Password"
                customDescription="The password to test LDAP authentication; if LDAP authentication is enabled."
                readOnly={false}
                onChange={FieldModelUtilities.handleConcreteModelChange(testFormData, setTestFormData)}
                value={testFormData[AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS.testLDAPPassword]}
            />
        </div>
    );

    return (
        <div className={classes.ldapForm}>
            <h2>LDAP Configuration</h2>
            <ConcreteConfigurationForm
                formDataId={formData.id}
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchData}
                deleteRequest={deleteData}
                updateRequest={updateData}
                createRequest={postData}
                testRequest={handleTestRequest}
                validateRequest={handleValidation}
                displayDelete={formData.status !== 404}
                displayTest={displayTest}
                errorHandler={errorHandler}
                deleteLabel="Delete LDAP Configuration"
                submitLabel="Save LDAP Configuration"
                testLabel="Test LDAP Configuration"
                buttonIdPrefix="ldap-config"
                testFields={testFields}
                testButtonClicked={testButtonClicked}
                disableTestModalSubmit={!isTestFormComplete()}
                modalSubmitText="Test User Connection"
                testModalButtonTitle="Enter Username and Password to enable test"
            >
                <CheckboxInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.enabled}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.enabled}
                    label="LDAP Enabled"
                    description="If true, Alert will attempt to authenticate using the LDAP configuration."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    isChecked={formData.enabled}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.enabled)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.enabled]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.serverName}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.serverName}
                    label="LDAP Server URL"
                    description="The URL of the LDAP Server."
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.serverName] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.serverName)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.serverName]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerDn}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerDn}
                    label="LDAP Distinguished Manager Name"
                    description="The distinguished manager name of the LDAP server."
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerDn] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerDn)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerDn]}
                />
                <PasswordInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerPassword}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerPassword}
                    label="LDAP Manager Password"
                    description="The password of the LDAP manager."
                    readOnly={readonly}
                    required
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.managerPassword || undefined}
                    isSet={formData.isManagerPasswordSet}
                    errorName={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerPassword}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.managerPassword]}
                />
                <DynamicSelectInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.authenticationType}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.authenticationType}
                    label="LDAP Authentication Type"
                    description="The type of authentication required to connect to the LDAP server."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    options={AUTH_TYPES}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.authenticationType] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.authenticationType)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.authenticationType]}
                />
                <DynamicSelectInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.referral}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.referral}
                    label="LDAP Referral"
                    description="Set the method to handle referrals."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    options={REFERRAL_TYPES}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.referral] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.referral)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.referral]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchBase}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchBase}
                    label="LDAP User Search Base"
                    description="The part of the LDAP directory in which user searches should be done."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchBase] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchBase)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchBase]}
                />

                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchFilter}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchFilter}
                    label="LDAP User Search Filter"
                    description="The filter used to search for user membership."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchFilter] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchFilter)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userSearchFilter]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userDnPatterns}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userDnPatterns}
                    label="LDAP User DN Patterns"
                    description="The pattern used used to supply a DN for the user. The pattern should be the name relative to the root DN."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userDnPatterns] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userDnPatterns)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userDnPatterns]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userAttributes}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userAttributes}
                    label="LDAP User Attributes"
                    description="User attributes to retrieve for users."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userAttributes] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userAttributes)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.userAttributes]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchBase}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchBase}
                    label="LDAP Group Search Base"
                    description="The part of the LDAP directory in which group searches should be done."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchBase] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchBase)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchBase]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchFilter}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchFilter}
                    label="LDAP Group Search Filter"
                    description="The filter used to search for group membership."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchFilter]}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchFilter)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupSearchFilter]}
                />
                <TextInput
                    id={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupRoleAttribute}
                    name={AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupRoleAttribute}
                    label="LDAP Group Role Attribute"
                    description="The ID of the attribute which contains the role name for a group."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleConcreteModelChange(formData, setFormData)}
                    value={formData[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupRoleAttribute] || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupRoleAttribute)}
                    errorValue={errors.fieldErrors[AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS.groupRoleAttribute]}
                />
            </ConcreteConfigurationForm>
        </div>
    );
};

LdapForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool
};

export default LdapForm;
