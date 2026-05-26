import React from 'react';
import * as PropTypes from 'prop-types';
import { Tab } from 'react-bootstrap';
import PageLayout from 'common/component/PageLayout';
import ViewTabs from 'common/component/navigation/ViewTabs';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import SettingsEncryptionConfiguration from 'page/settings/standalone/SettingsEncryptionConfiguration.js';
import SettingsProxyConfiguration from 'page/settings/standalone/SettingsProxyConfiguration';
import useGetPermissions from 'common/hooks/useGetPermissions';

const SettingsConfiguration = ({
    csrfToken, errorHandler, descriptor
}) => {
    const { readOnly, canTest, canSave, canDelete } = useGetPermissions(descriptor);

    return (
        <PageLayout
            title={SETTINGS_INFO.label}
            description="This page allows you to configure the global settings."
            headerIcon="cog"
        >
            <ViewTabs id="settings-tabs">
                <Tab eventKey={1} title="Encryption">
                    <SettingsEncryptionConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displaySave={canSave}
                    />
                </Tab>
                <Tab eventKey={2} title="Proxy">
                    <SettingsProxyConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readOnly={readOnly}
                        displayTest={canTest}
                        displaySave={canSave}
                        displayDelete={canDelete}
                    />
                </Tab>
            </ViewTabs>
        </PageLayout>
    );
};

SettingsConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    descriptor: PropTypes.object
};

export default SettingsConfiguration;
