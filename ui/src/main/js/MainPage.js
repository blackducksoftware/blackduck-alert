import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Redirect, Route, withRouter } from 'react-router-dom';
import Navigation from 'Navigation';
import AboutInfo from 'global/components/about/AboutInfo';
import LogoutConfirmation from 'common/LogoutConfirmation';
import { getDescriptors } from 'store/actions/descriptors';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SlackGlobalConfiguration from 'channel/slack/SlackGlobalConfiguration';
import EmailGlobalConfiguration from 'channel/email/EmailGlobalConfiguration';
import JiraCloudGlobalConfiguration from 'channel/jira/cloud/JiraCloudGlobalConfiguration';
import { JIRA_CLOUD_INFO } from 'channel/jira/cloud/JiraCloudModel';
import { SLACK_INFO } from 'channel/slack/SlackModels';
import { EMAIL_INFO } from 'channel/email/EmailModels';
import { JIRA_SERVER_INFO } from 'channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'channel/msteams/MSTeamsModel';
import MSTeamsGlobalConfiguration from 'channel/msteams/MSTeamsGlobalConfiguration';
import { AZURE_INFO } from 'channel/azure/AzureModel';
import AzureGlobalConfiguration from 'channel/azure/AzureGlobalConfiguration';
import { SCHEDULING_INFO } from 'global/components/scheduling/SchedulingModel';
import SchedulingConfiguration from 'global/components/scheduling/SchedulingConfiguration';
import { SETTINGS_INFO } from 'global/components/settings/SettingsModel';
import SettingsConfiguration from 'global/components/settings/SettingsConfiguration';
import { AUTHENTICATION_INFO } from 'global/components/auth/AuthenticationModel';
import AuthenticationConfiguration from 'global/components/auth/AuthenticationConfiguration';
import { BLACKDUCK_INFO, BLACKDUCK_URLS } from 'provider/blackduck/BlackDuckModel';
import BlackDuckProviderConfiguration from 'provider/blackduck/BlackDuckProviderConfiguration';
import BlackDuckConfiguration from 'provider/blackduck/BlackDuckConfiguration';
import { AUDIT_INFO } from 'global/components/audit/AuditModel';
import AuditPage from 'global/components/audit/AuditPage';
import { CERTIFICATE_INFO } from 'global/components/certificates/CertificateModel';
import CertificatesPage from 'global/components/certificates/CertificatesPage';
import { TASK_MANAGEMENT_INFO } from 'global/components/task/TaskManagementModel';
import TaskManagement from 'global/components/task/TaskManagement';
import { USER_MANAGEMENT_INFO } from 'global/components/user/UserModel';
import UserManagement from 'global/components/user/UserManagement';
import JiraServerGlobalConfiguration from 'channel/jira/server/JiraServerGlobalConfiguration';
import { CONTEXT_TYPE, doesDescriptorExist } from 'common/util/descriptorUtilities';
import { DISTRIBUTION_INFO, DISTRIBUTION_URLS } from 'distribution/DistributionModel';
import DistributionConfiguration from 'distribution/DistributionConfiguration';
import DistributionConfigurationForm from 'distribution/DistributionConfigurationForm';
import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const MainPage = ({
    descriptors, fetching, getDescriptorsRedux, csrfToken, autoRefresh, unauthorizedFunction
}) => {
    const [globalDescriptorMap, setGlobalDescriptorMap] = useState({});
    const [distributionDescriptorMap, setDistributionDescriptorMap] = useState({});
    const [errorHandler, setErrorHandler] = useState({});
    useEffect(() => {
        getDescriptorsRedux();
        setErrorHandler(HTTPErrorUtils.createErrorHandler(unauthorizedFunction));
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
            <Route
                exact
                key="blackduck-route"
                path={[`${BLACKDUCK_URLS.blackDuckConfigUrl}/:id?`, `${BLACKDUCK_URLS.blackDuckConfigCopyUrl}/:id?`]}
            >
                {doesDescriptorExist(globalDescriptorMap, BLACKDUCK_INFO.key) && <BlackDuckConfiguration csrfToken={csrfToken} errorHandler={errorHandler} readonly={globalDescriptorMap[BLACKDUCK_INFO.key].readOnly} />}
            </Route>
            {doesDescriptorExist(globalDescriptorMap, BLACKDUCK_INFO.key) && createRoute(providerUri, BLACKDUCK_INFO.url, <BlackDuckProviderConfiguration
                csrfToken={csrfToken}
                showRefreshButton={!autoRefresh}
                readonly={globalDescriptorMap[BLACKDUCK_INFO.key].readOnly}
            />)}
            {doesDescriptorExist(globalDescriptorMap, AZURE_INFO.key) && createRoute(channelUri, AZURE_INFO.url, <AzureGlobalConfiguration
                csrfToken={csrfToken}
                errorHandler={errorHandler}
                readonly={globalDescriptorMap[AZURE_INFO.key].readOnly}
            />)}
            {doesDescriptorExist(globalDescriptorMap, EMAIL_INFO.key) && createRoute(channelUri, EMAIL_INFO.url, <EmailGlobalConfiguration
                csrfToken={csrfToken}
                errorHandler={errorHandler}
                readonly={globalDescriptorMap[EMAIL_INFO.key].readOnly}
            />)}
            {doesDescriptorExist(globalDescriptorMap, JIRA_CLOUD_INFO.key) && createRoute(channelUri, JIRA_CLOUD_INFO.url, <JiraCloudGlobalConfiguration
                csrfToken={csrfToken}
                errorHandler={errorHandler}
                readonly={globalDescriptorMap[JIRA_CLOUD_INFO.key].readOnly}
            />)}
            {doesDescriptorExist(globalDescriptorMap, JIRA_SERVER_INFO.key) && createRoute(channelUri, JIRA_SERVER_INFO.url, <JiraServerGlobalConfiguration
                csrfToken={csrfToken}
                errorHandler={errorHandler}
                readonly={globalDescriptorMap[JIRA_SERVER_INFO.key].readOnly}
            />)}
            {doesDescriptorExist(globalDescriptorMap, MSTEAMS_INFO.key) && createRoute(channelUri, MSTEAMS_INFO.url, <MSTeamsGlobalConfiguration />)}
            {doesDescriptorExist(globalDescriptorMap, SLACK_INFO.key) && createRoute(channelUri, SLACK_INFO.url, <SlackGlobalConfiguration />)}
            <Route
                exact
                key="distribution-route"
                path={[`${DISTRIBUTION_URLS.distributionConfigUrl}/:id?`, `${DISTRIBUTION_URLS.distributionConfigCopyUrl}/:id?`]}
            >
                <DistributionConfigurationForm csrfToken={csrfToken} readonly={false} descriptors={distributionDescriptorMap} errorHandler={errorHandler} />
            </Route>
            {createRoute('/alert/jobs/', DISTRIBUTION_INFO.url, <DistributionConfiguration csrfToken={csrfToken} descriptors={descriptors} errorHandler={errorHandler} showRefreshButton={!autoRefresh} />)}
            {doesDescriptorExist(globalDescriptorMap, AUDIT_INFO.key) && createRoute(componentUri, AUDIT_INFO.url, <AuditPage />)}
            {doesDescriptorExist(globalDescriptorMap, AUTHENTICATION_INFO.key) && createRoute(componentUri, AUTHENTICATION_INFO.url, <AuthenticationConfiguration csrfToken={csrfToken} readonly={globalDescriptorMap[AUTHENTICATION_INFO.key].readOnly} />)}
            {doesDescriptorExist(globalDescriptorMap, CERTIFICATE_INFO.key) && createRoute(componentUri, CERTIFICATE_INFO.url, <CertificatesPage />)}
            {doesDescriptorExist(globalDescriptorMap, SCHEDULING_INFO.key) && createRoute(componentUri, SCHEDULING_INFO.url, <SchedulingConfiguration csrfToken={csrfToken} readonly={globalDescriptorMap[SCHEDULING_INFO.key].readOnly} />)}
            {doesDescriptorExist(globalDescriptorMap, SETTINGS_INFO.key) && createRoute(componentUri, SETTINGS_INFO.url, <SettingsConfiguration csrfToken={csrfToken} readonly={globalDescriptorMap[SETTINGS_INFO.key].readOnly} />)}
            {doesDescriptorExist(globalDescriptorMap, TASK_MANAGEMENT_INFO.key) && createRoute(componentUri, TASK_MANAGEMENT_INFO.url, <TaskManagement />)}
            {doesDescriptorExist(globalDescriptorMap, USER_MANAGEMENT_INFO.key) && createRoute(componentUri, USER_MANAGEMENT_INFO.url, <UserManagement />)}
            <Route exact path="/alert/general/about" component={AboutInfo} />
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
