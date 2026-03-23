import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import PageHeader from 'common/component/navigation/PageHeader';
import LdapForm from 'application/auth/LdapForm';
import SamlForm from 'application/auth/SamlForm';

import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as GlobalRequestHelper from 'common/configuration/global/GlobalRequestHelper';

import { Tab, Tabs } from 'react-bootstrap';
import { AUTHENTICATION_INFO } from 'application/auth/AuthenticationModel';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';

const AuthenticationPageLayout = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, fileRead, fileWrite, fileDelete
}) => {
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
        <div>
            <PageHeader
                title={AUTHENTICATION_INFO.label}
                description="This page allows you to configure user authentication for Alert."
                lastUpdated={formData.lastUpdated}
                icon="fingerprint"
            />
            <Tabs defaultActiveKey={1} id="user-management-tabs">
                <Tab eventKey={1} title="LDAP">
                    <LdapForm
                        csrfToken={csrfToken}
                        readonly={readonly}
                        errorHandler={errorHandler}
                        displayTest={displayTest}
                    />
                </Tab>
                <Tab eventKey={2} title="SAML">
                    <SamlForm
                        csrfToken={csrfToken}
                        readonly={readonly}
                        displayTest={displayTest}
                        displaySave={displaySave}
                        errorHandler={errorHandler}
                        fileRead={fileRead}
                        fileWrite={fileWrite}
                        fileDelete={fileDelete}
                    />
                </Tab>
            </Tabs>
        </div>
    );
};

AuthenticationPageLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    fileRead: PropTypes.bool,
    fileWrite: PropTypes.bool,
    fileDelete: PropTypes.bool
};

export default AuthenticationPageLayout;
