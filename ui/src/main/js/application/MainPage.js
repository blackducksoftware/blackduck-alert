import React, { useEffect, useState } from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { Navigate, Route, Routes } from 'react-router-dom';
import Navigation from 'application/Navigation';
import TopNavBar from 'application/TopNavBar';
import LogoutConfirmation from 'common/component/LogoutConfirmation';
import { getDescriptors } from 'store/actions/descriptors';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SlackGlobalConfiguration from 'page/channel/slack/SlackGlobalConfiguration';
import JiraCloudGlobalConfiguration from 'page/channel/jira/cloud/JiraCloudGlobalConfiguration';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import JiraServerPageLayout from 'page/channel/jira/server/JiraServerPageLayout';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import MSTeamsGlobalConfiguration from 'page/channel/msteams/MSTeamsGlobalConfiguration';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';
import AzureBoardsPageLayout from 'page/channel/azure/AzureBoardsPageLayout';
import { SCHEDULING_INFO } from 'page/scheduling/SchedulingModel';
import SchedulingConfiguration from 'page/scheduling/SchedulingConfiguration';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import SettingsConfiguration from 'page/settings/standalone/SettingsConfiguration';
import { AUTHENTICATION_INFO } from 'application/auth/AuthenticationModel';
import AuthenticationPageLayout from 'application/auth/AuthenticationPageLayout';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import ProviderPageLayout from 'page/provider/ProviderPageLayout';
import { AUDIT_INFO } from 'page/audit/AuditModel';
import AuditPageLayout from 'page/audit/AuditPageLayout';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';
import CertificatesPageLayout from 'page/certificates/CertificatesPageLayout';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';
import TaskManagementPageLayout from 'page/task/TaskManagementPageLayout';
import { USER_MANAGEMENT_INFO } from 'page/usermgmt/UserModel';
import UserManagement from 'page/usermgmt/UserManagement';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import DistributionConfiguration from 'page/distribution/DistributionConfiguration';
import DistributionConfigurationForm from 'page/distribution/DistributionConfigurationForm';
import { unauthorized } from 'store/actions/session';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import DescriptorRoute from 'common/component/descriptor/DescriptorRoute';
import EmailGlobalConfiguration from 'page/channel/email/EmailGlobalConfiguration';
import AboutLayout from 'page/about/AboutLayout';

const useStyles = createUseStyles({
    blackDuckAlertApp: {
        display: 'grid',
        gridTemplateAreas: `
            "topnav topnav"
            "sidenav main"
            "footer footer"
        `,
        gridTemplateRows: 'auto 1fr auto',
        gridTemplateColumns: 'auto 1fr',
        height: '100%',
        width: '100vw',
        overflow: 'auto'
    },
    topnav: {
        gridArea: 'topnav',
        height: '50px',
        zIndex: 10001 // above modal gray background
    },
    appSidenav: {
        gridArea: 'sidenav',
        width: '80px'
    },
    main: {
        gridArea: 'main'
    }
});

const MainPage = () => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { fetching, items: descriptors } = useSelector((state) => state.descriptors);
    const { csrfToken, showLogoutConfirm } = useSelector((state) => state.session);
    const { autoRefresh } = useSelector((state) => state.refresh);
    
    const [globalDescriptorMap, setGlobalDescriptorMap] = useState({});
    const [distributionDescriptorMap, setDistributionDescriptorMap] = useState({});

    useEffect(() => {
        dispatch(getDescriptors());
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

    const errorHandler = HTTPErrorUtils.createErrorHandler(() => dispatch(unauthorized()));

    const page = (
        <>
            <Route
                exact
                path="/alert/"
                element={<Navigate to="/alert/general/about" />}
            />

            {/* Home Page */}
            <Route
                exact   
                path="/alert/general/about"
                element={<AboutLayout globalDescriptorMap={globalDescriptorMap} distributionDescriptorMap={distributionDescriptorMap} />}
            />

            {/* Provider Page */}
            <Route
                exact
                path="/alert/providers/blackduck"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[BLACKDUCK_INFO.key]}
                        element={<ProviderPageLayout descriptor={globalDescriptorMap[BLACKDUCK_INFO.key]} />}
                    />
                )}
            />

            {/* Channel - Azure Boards Page */}
            <Route
                exact
                path="/alert/channels/azure_boards"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[AZURE_BOARDS_INFO.key]}
                        element={<AzureBoardsPageLayout descriptor={globalDescriptorMap[AZURE_BOARDS_INFO.key]} />}
                    />
                )}
            />

            {/* Channel - Email Page */}
            <Route
                exact
                path="/alert/channels/email"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[EMAIL_INFO.key]}
                        element={<EmailGlobalConfiguration csrfToken={csrfToken} errorHandler={errorHandler} descriptor={globalDescriptorMap[EMAIL_INFO.key]} />}
                    />
                )}
            />

            {/* Channel - Jira Cloud Page */}
            <Route
                exact
                path="/alert/channels/jira"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[JIRA_CLOUD_INFO.key]}
                        element={<JiraCloudGlobalConfiguration csrfToken={csrfToken} errorHandler={errorHandler} descriptor={globalDescriptorMap[JIRA_CLOUD_INFO.key]} />}
                    />
                )}
            />

            {/* Channel - Jira Server Page */}
            <Route
                exact
                path="/alert/channels/jira_server"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[JIRA_SERVER_INFO.key]}
                        element={<JiraServerPageLayout descriptor={globalDescriptorMap[JIRA_SERVER_INFO.key]} />}
                    />
                )}
            />

            {/* Channel - MS Teams Page */}
            <Route
                exact
                path="/alert/channels/msteams"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[MSTEAMS_INFO.key]}
                        element={<MSTeamsGlobalConfiguration />}
                    />
                )}
            />

            {/* Channel - Slack Page */}
            <Route
                exact
                path="/alert/channels/slack"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[SLACK_INFO.key]}
                        element={<SlackGlobalConfiguration />}
                    />
                )}
            />

            {/* Create Distribution Job Page */}
            <Route
                exact
                path="/alert/jobs/distribution"
                element={<DistributionConfiguration csrfToken={csrfToken} descriptors={descriptors} errorHandler={errorHandler} showRefreshButton={!autoRefresh} />}
            />

            {/* Edit Distribution Job Page */}
            <Route
                exact
                key="distribution-route-edit"
                path="/alert/jobs/distribution/edit/:id?"
                element={<DistributionConfigurationForm csrfToken={csrfToken} readonly={false} descriptors={distributionDescriptorMap} errorHandler={errorHandler} />}
            />

            {/* Copy Distribution Job Page */}
            <Route
                exact
                key="distribution-route-copy"
                path="/alert/jobs/distribution/copy/:id?"
                element={<DistributionConfigurationForm csrfToken={csrfToken} readonly={false} descriptors={distributionDescriptorMap} errorHandler={errorHandler} />}
            />

            {/* Audit Information Page */}
            <Route
                exact
                path="/alert/components/audit"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[AUDIT_INFO.key]}
                        element={<AuditPageLayout />}
                    />
                )}
            />

            {/* Authentication Page */}
            <Route
                exact
                path="/alert/components/authentication"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[AUTHENTICATION_INFO.key]}
                        element={<AuthenticationPageLayout csrfToken={csrfToken} errorHandler={errorHandler} descriptor={globalDescriptorMap[AUTHENTICATION_INFO.key]} globalDescriptorMap={globalDescriptorMap} />}
                    />
                )}
            />

            {/* Certificates Page */}
            <Route
                exact
                path="/alert/components/certificates"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[CERTIFICATE_INFO.key]}
                        element={<CertificatesPageLayout csrfToken={csrfToken} errorHandler={errorHandler} readOnly={false} />}
                    />
                )}
            />

            {/* Scheduling Page */}
            <Route
                exact
                path="/alert/components/scheduling"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[SCHEDULING_INFO.key]}
                        element={<SchedulingConfiguration csrfToken={csrfToken} errorHandler={errorHandler} descriptor={globalDescriptorMap[SCHEDULING_INFO.key]} />}
                    />
                )}
            />

            {/* Settings Page */}
            <Route
                exact
                path="/alert/components/settings"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[SETTINGS_INFO.key]}
                        element={<SettingsConfiguration csrfToken={csrfToken} errorHandler={errorHandler} descriptor={globalDescriptorMap[SETTINGS_INFO.key]} />}
                    />
                )}
            />

            {/* Task Management Page */}
            <Route
                exact
                path="/alert/components/tasks"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[TASK_MANAGEMENT_INFO.key]}
                        element={<TaskManagementPageLayout />}
                    />
                )}
            />

            {/* User Management Page */}
            <Route
                exact
                path="/alert/components/users"
                element={(
                    <DescriptorRoute
                        descriptor={globalDescriptorMap[USER_MANAGEMENT_INFO.key]}
                        element={<UserManagement />}
                    />
                )}
            />
        </>
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

    return (
        <div className={classes.blackDuckAlertApp}>
            <div className={classes.topnav}>
                <TopNavBar />
            </div>
            <div className={classes.appSidenav}>
                <Navigation globalDescriptorMap={globalDescriptorMap} />
            </div>
            <div className={classes.main}>
                {fetching ? spinner : (
                    <Routes>
                        {page}
                    </Routes>
                )}
            </div>
            <div className="modalsArea">
                <LogoutConfirmation showLogoutConfirm={showLogoutConfirm} />
            </div>
        </div>
    );
};

export default MainPage;
