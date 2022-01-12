import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Redirect, Route, withRouter } from 'react-router-dom';
import Navigation from 'application/Navigation';
import AboutInfo from 'page/about/AboutInfo';
import LogoutConfirmation from 'common/LogoutConfirmation';
import { getDescriptors } from 'store/actions/descriptors';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SlackGlobalConfiguration from 'page/channel/slack/SlackGlobalConfiguration';
import JiraCloudGlobalConfiguration from 'page/channel/jira/cloud/JiraCloudGlobalConfiguration';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import MSTeamsGlobalConfiguration from 'page/channel/msteams/MSTeamsGlobalConfiguration';
import { AZURE_INFO } from 'page/channel/azure/AzureModel';
import AzureGlobalConfiguration from 'page/channel/azure/AzureGlobalConfiguration';
import { SCHEDULING_INFO } from 'page/scheduling/SchedulingModel';
import SchedulingConfiguration from 'page/scheduling/SchedulingConfiguration';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import SettingsConfiguration from 'page/settings/standalone/SettingsConfiguration';
import { AUTHENTICATION_INFO } from 'application/auth/AuthenticationModel';
import AuthenticationConfiguration from 'application/auth/AuthenticationConfiguration';
import { BLACKDUCK_INFO, BLACKDUCK_URLS } from 'page/provider/blackduck/BlackDuckModel';
import BlackDuckProviderConfiguration from 'page/provider/blackduck/BlackDuckProviderConfiguration';
import BlackDuckConfiguration from 'page/provider/blackduck/BlackDuckConfiguration';
import { AUDIT_INFO } from 'page/audit/AuditModel';
import AuditPage from 'page/audit/AuditPage';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';
import CertificatesPage from 'page/certificates/CertificatesPage';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';
import TaskManagement from 'page/task/TaskManagement';
import { USER_MANAGEMENT_INFO } from 'page/user/UserModel';
import UserManagement from 'page/user/UserManagement';
import JiraServerGlobalConfiguration from 'page/channel/jira/server/JiraServerGlobalConfiguration';
import { CONTEXT_TYPE, isOperationAssigned, OPERATIONS } from 'common/util/descriptorUtilities';
import { DISTRIBUTION_INFO, DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import DistributionConfiguration from 'page/distribution/DistributionConfiguration';
import DistributionConfigurationForm from 'page/distribution/DistributionConfigurationForm';
import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import DescriptorRoute from 'common/DescriptorRoute';
import EmailGlobalConfiguration from 'page/channel/email/EmailGlobalConfiguration';

const MainPage = ({
    descriptors, fetching, getDescriptorsRedux, csrfToken, autoRefresh, unauthorizedFunction
}) => {
    const [globalDescriptorMap, setGlobalDescriptorMap] = useState({});
    const [distributionDescriptorMap, setDistributionDescriptorMap] = useState({});

    useEffect(() => {
        getDescriptorsRedux();
    }, []);

    useEffect(() => {
        const newGlobalDescriptorMap = {};
        const newDistributionDescriptorMap = {};
        descriptors.forEach((descriptor) => {
            if (descriptor.context === CONTEXT_TYPE.GLOBAL) {
                newGlobalDescriptorMap[descriptor.name] = descriptor;
            } else if (descriptor.context === CONTEXT_TYPE.DISTRIBUTION) {
                newDistributionDescriptorMap[descriptor.name] = descriptor;
            }
        });
        setGlobalDescriptorMap(newGlobalDescriptorMap);
        setDistributionDescriptorMap(newDistributionDescriptorMap);
    }, [descriptors]);

    const errorHandler = HTTPErrorUtils.createErrorHandler(unauthorizedFunction);

    const createRoute = (uriPrefix, urlName, component) => (
        <Route
            exact
            key={urlName}
            path={`${uriPrefix}${urlName}`}
        >
            {component}
        </Route>
    );
    const providerUri = '/alert/providers/';
    const channelUri = '/alert/channels/';
    const componentUri = '/alert/components/';

    const page = (
        <div className="contentArea">
            <Route
                exact
                path="/alert/"
                render={() => (
                    <Redirect to="/alert/general/about" />
                )}
            />
            <DescriptorRoute
                descriptor={globalDescriptorMap[BLACKDUCK_INFO.key]}
                urlName={BLACKDUCK_INFO.url}
                paths={[`${BLACKDUCK_URLS.blackDuckConfigUrl}/:id?`, `${BLACKDUCK_URLS.blackDuckConfigCopyUrl}/:id?`]}
                render={(readonly, showTest, showSave) => (
                    <BlackDuckConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readonly}
                        displayTest={showTest}
                        displaySave={showSave}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={providerUri}
                urlName={BLACKDUCK_INFO.url}
                descriptor={globalDescriptorMap[BLACKDUCK_INFO.key]}
                render={(readonly, showTest, showSave, showDelete) => (
                    <BlackDuckProviderConfiguration
                        csrfToken={csrfToken}
                        showRefreshButton={!autoRefresh}
                        readonly={readonly}
                        displayDelete={showDelete}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={channelUri}
                urlName={AZURE_INFO.url}
                descriptor={globalDescriptorMap[AZURE_INFO.key]}
                render={(readonly, showTest, showSave, showDelete) => (
                    <AzureGlobalConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readonly}
                        displayTest={showTest}
                        displaySave={showSave}
                        displayDelete={showDelete}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={channelUri}
                urlName={EMAIL_INFO.url}
                descriptor={globalDescriptorMap[EMAIL_INFO.key]}
                render={(readOnly, showTest, showSave, showDelete) => (
                    <EmailGlobalConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displayTest={showTest}
                        displaySave={showSave}
                        displayDelete={showDelete}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={channelUri}
                urlName={JIRA_CLOUD_INFO.url}
                descriptor={globalDescriptorMap[JIRA_CLOUD_INFO.key]}
                render={(readOnly, showTest, showSave, showDelete) => (
                    <JiraCloudGlobalConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displayTest={showTest}
                        displaySave={showSave}
                        displayDelete={showDelete}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={channelUri}
                urlName={JIRA_SERVER_INFO.url}
                descriptor={globalDescriptorMap[JIRA_SERVER_INFO.key]}
                render={(readOnly, showTest, showSave, showDelete) => (
                    <JiraServerGlobalConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displayTest={showTest}
                        displaySave={showSave}
                        displayDelete={showDelete}
                    />
                )}
            />
            <DescriptorRoute uriPrefix={channelUri} urlName={MSTEAMS_INFO.url} descriptor={globalDescriptorMap[MSTEAMS_INFO.key]} render={() => <MSTeamsGlobalConfiguration />} />
            <DescriptorRoute uriPrefix={channelUri} urlName={SLACK_INFO.url} descriptor={globalDescriptorMap[SLACK_INFO.key]} render={() => <SlackGlobalConfiguration />} />
            <Route
                exact
                key="distribution-route"
                path={[`${DISTRIBUTION_URLS.distributionConfigUrl}/:id?`, `${DISTRIBUTION_URLS.distributionConfigCopyUrl}/:id?`]}
            >
                <DistributionConfigurationForm csrfToken={csrfToken} readonly={false} descriptors={distributionDescriptorMap} errorHandler={errorHandler} />
            </Route>
            {createRoute('/alert/jobs/', DISTRIBUTION_INFO.url, <DistributionConfiguration csrfToken={csrfToken} descriptors={descriptors} errorHandler={errorHandler} showRefreshButton={!autoRefresh} />)}
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={AUDIT_INFO.url}
                descriptor={globalDescriptorMap[AUDIT_INFO.key]}
                render={() => (
                    <AuditPage />
                )}
            />
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={AUTHENTICATION_INFO.url}
                descriptor={globalDescriptorMap[AUTHENTICATION_INFO.key]}
                hasTestFields
                render={(readOnly, showTest, showSave) => (
                    <AuthenticationConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displayTest={showTest}
                        displaySave={showSave}
                        fileRead={isOperationAssigned(globalDescriptorMap[AUTHENTICATION_INFO.key], OPERATIONS.UPLOAD_FILE_READ)}
                        fileWrite={isOperationAssigned(globalDescriptorMap[AUTHENTICATION_INFO.key], OPERATIONS.UPLOAD_FILE_WRITE)}
                        fileDelete={isOperationAssigned(globalDescriptorMap[AUTHENTICATION_INFO.key], OPERATIONS.UPLOAD_FILE_DELETE)}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={CERTIFICATE_INFO.url}
                descriptor={globalDescriptorMap[CERTIFICATE_INFO.key]}
                render={() => (
                    <CertificatesPage />
                )}
            />
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={SCHEDULING_INFO.url}
                descriptor={globalDescriptorMap[SCHEDULING_INFO.key]}
                render={(readOnly, showTest, showSave) => (
                    <SchedulingConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displaySave={showSave}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={SETTINGS_INFO.url}
                descriptor={globalDescriptorMap[SETTINGS_INFO.key]}
                render={(readOnly, showTest, showSave) => (
                    <SettingsConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displayTest={isOperationAssigned(globalDescriptorMap[SETTINGS_INFO.key], OPERATIONS.EXECUTE)}
                        displaySave={showSave}
                        displayDelete={isOperationAssigned(globalDescriptorMap[SETTINGS_INFO.key], OPERATIONS.DELETE)}
                    />
                )}
            />
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={TASK_MANAGEMENT_INFO.url}
                descriptor={globalDescriptorMap[TASK_MANAGEMENT_INFO.key]}
                render={() => (
                    <TaskManagement />
                )}
            />
            <DescriptorRoute
                uriPrefix={componentUri}
                urlName={USER_MANAGEMENT_INFO.url}
                descriptor={globalDescriptorMap[USER_MANAGEMENT_INFO.key]}
                render={() => (
                    <UserManagement />
                )}
            />
            <Route exact path="/alert/general/about">
                <AboutInfo globalDescriptorMap={globalDescriptorMap} distributionDescriptorMap={distributionDescriptorMap} />
            </Route>
        </div>
    );

    const spinner = (
        <div>
            <div className="loginContainer">
                <div className="progressIcon">
                    <FontAwesomeIcon icon="spinner" className="alert-icon" size="5x" spin />
                </div>
            </div>
        </div>
    );

    const content = (fetching) ? spinner : page;

    return (
        <div>
            <Navigation globalDescriptorMap={globalDescriptorMap} />
            {content}
            <div className="modalsArea">
                <LogoutConfirmation />
            </div>
        </div>
    );
};

MainPage.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    fetching: PropTypes.bool.isRequired,
    getDescriptorsRedux: PropTypes.func.isRequired,
    csrfToken: PropTypes.string.isRequired,
    autoRefresh: PropTypes.bool.isRequired,
    unauthorizedFunction: PropTypes.func.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items,
    fetching: state.descriptors.fetching,
    csrfToken: state.session.csrfToken,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    getDescriptorsRedux: () => dispatch(getDescriptors()),
    unauthorizedFunction: () => dispatch(unauthorized())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
