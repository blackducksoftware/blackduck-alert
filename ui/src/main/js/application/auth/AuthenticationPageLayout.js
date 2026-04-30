import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import PageLayout from 'common/component/PageLayout';
import ViewTabs from 'common/component/navigation/ViewTabs';
import LdapForm from 'application/auth/LdapForm';
import SamlForm from 'application/auth/SamlForm';

import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as GlobalRequestHelper from 'common/configuration/global/GlobalRequestHelper';

import { Tab } from 'react-bootstrap';
import { AUTHENTICATION_INFO } from 'application/auth/AuthenticationModel';
import { CONTEXT_TYPE, OPERATIONS, isOperationAssigned } from 'common/util/descriptorUtilities';
import useGetPermissions from 'common/hooks/useGetPermissions';

const AuthenticationPageLayout = ({
    csrfToken, errorHandler, descriptor, globalDescriptorMap
}) => {
    const { readOnly, canTest, canSave } = useGetPermissions(descriptor);
    const fileRead = isOperationAssigned(globalDescriptorMap[AUTHENTICATION_INFO.key], OPERATIONS.UPLOAD_FILE_READ);
    const fileWrite = isOperationAssigned(globalDescriptorMap[AUTHENTICATION_INFO.key], OPERATIONS.UPLOAD_FILE_WRITE);
    const fileDelete = isOperationAssigned(globalDescriptorMap[AUTHENTICATION_INFO.key], OPERATIONS.UPLOAD_FILE_DELETE);

    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, AUTHENTICATION_INFO.key));

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(AUTHENTICATION_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    useEffect(() => {
        retrieveData();
    }, []);

    return (
        <PageLayout
            title={AUTHENTICATION_INFO.label}
            description="This page allows you to configure user authentication for Alert."
            lastUpdated={formData.lastUpdated}
            headerIcon="fingerprint"
        >
            <ViewTabs defaultActiveKey={1} id="authentication-tabs">
                <Tab eventKey={1} title="LDAP">
                    <LdapForm
                        csrfToken={csrfToken}
                        readonly={readOnly}
                        errorHandler={errorHandler}
                        displayTest={canTest}
                    />
                </Tab>
                <Tab eventKey={2} title="SAML">
                    <SamlForm
                        csrfToken={csrfToken}
                        readonly={readOnly}
                        displayTest={canTest}
                        displaySave={canSave}
                        errorHandler={errorHandler}
                        fileRead={fileRead}
                        fileWrite={fileWrite}
                        fileDelete={fileDelete}
                    />
                </Tab>
            </ViewTabs>
        </PageLayout>
    );
};

AuthenticationPageLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readonly: PropTypes.bool,
    descriptor: PropTypes.object,
    globalDescriptorMap: PropTypes.object
};

export default AuthenticationPageLayout;
