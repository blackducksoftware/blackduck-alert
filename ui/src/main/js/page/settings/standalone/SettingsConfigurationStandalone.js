import React from 'react';
import * as PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import SettingsEncryptionConfiguration from 'page/settings/standalone/SettingsEncryptionConfiguration.js';
import SettingsProxyConfiguration from 'page/settings/standalone/SettingsProxyConfiguration';

const SettingsConfigurationStandalone = ({
    csrfToken, errorHandler, readOnly, displaySave, displayDelete
}) => (
    <CommonGlobalConfiguration
        label={`${SETTINGS_INFO.label} BETA (WIP)`}
        description="This page allows you to configure the global settings. (WIP: Everything on this page is currently in development)"
    >
        <Tabs defaultActiveKey={1} id="settings-tabs">
            <Tab eventKey={1} title="SettingsEncryption">
                <SettingsEncryptionConfiguration
                    csrfToken={csrfToken}
                    errorHandler={errorHandler}
                    readonly={readOnly}
                    displaySave={displaySave}
                />
            </Tab>
            <Tab eventKey={2} title="SettingsProxy">
                <SettingsProxyConfiguration
                    csrfToken={csrfToken}
                    errorHandler={errorHandler}
                    readOnly={readOnly}
                    displaySave={displaySave}
                    displayDelete={displayDelete}
                />
            </Tab>
        </Tabs>
    </CommonGlobalConfiguration>
);

SettingsConfigurationStandalone.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readOnly: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

SettingsConfigurationStandalone.defaultProps = {
    readOnly: false,
    displaySave: true,
    displayDelete: true
};

export default SettingsConfigurationStandalone;
