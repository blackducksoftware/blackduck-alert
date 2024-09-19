import React from 'react';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { NavLink, withRouter } from 'react-router-dom';
import Logo from 'common/component/Logo';
import { cancelLogout, confirmLogout } from 'store/actions/session';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';
import { SCHEDULING_INFO } from 'page/scheduling/SchedulingModel';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import { AUTHENTICATION_INFO } from 'application/auth/AuthenticationModel';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { AUDIT_INFO } from 'page/audit/AuditModel';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';
import { USER_MANAGEMENT_INFO } from 'page/usermgmt/UserModel';
import { DESCRIPTOR_TYPE, doesDescriptorExist } from 'common/util/descriptorUtilities';
import { DISTRIBUTION_INFO, DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';

import SideNavItem from 'common/component/navigation/SideNavItem';
import { AZURE_BOARDS_URLS } from 'page/channel/azure/AzureBoardsModel';
import { EMAIL_URLS } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_URLS } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_URLS } from 'page/channel/jira/server/JiraServerModel';
import { SLACK_URLS } from 'page/channel/slack/SlackModels';
import { MSTEAMS_URLS } from 'page/channel/msteams/MSTeamsModel';

const useStyles = createUseStyles({
    sideNavContent: {
        display: 'flex',
        height: '100%',
        border: 'solid 1px',
        backgroundImage: 'linear-gradient(180deg, #222, #222 30%, #5a2d83 95%, #564c9d)'
    },
    sideNav: {
        flexGrow: 1,
        listStyle: 'none',
        paddingLeft: 0,
        color: '#ffffff'
    }
});

const Navigation = ({ confirmLogoutPressed, cancelLogout, globalDescriptorMap }) => {
    const classes = useStyles();
    const createStaticNavItem = (uriPrefix, itemObject) => (
        <li key={itemObject.key} onClick={() => cancelLogout()}>
            <NavLink to={`${uriPrefix}${itemObject.url}`} activeClassName="activeNav">
                {itemObject.label}
            </NavLink>
        </li>
    );

    const channelUri = '/alert/channels/';
    const providerUri = '/alert/providers/';
    const componentUri = '/alert/components/';

    const hasType = (descriptorType) => Object.values(globalDescriptorMap).some((descriptor) => descriptorType === descriptor.type);

    const channelGroup = [{
        id: 'azure_boards',
        label: 'Azure Boards',
        href: AZURE_BOARDS_URLS.mainUrl
    }, {
        id: 'email',
        label: 'Email',
        href: EMAIL_URLS.mainUrl
    }, {
        id: 'jira_cloud',
        label: 'Jira Cloud',
        href: JIRA_CLOUD_URLS.mainUrl
    }, {
        id: 'jira_server',
        label: 'Jira Server',
        href: JIRA_SERVER_URLS.mainUrl
    }, {
        id: 'microsoft_teams',
        label: 'Microsoft Teams',
        href: MSTEAMS_URLS.mainUrl
    }, {
        id: 'slack',
        label: 'Slack',
        href: SLACK_URLS.mainUrl
    }];

    const manageGroup = [{
        id: 'audit_failures',
        label: 'Audit Failures',
        href: '/alert/components/audit'
    }, {
        id: 'authentication',
        label: 'Authentication',
        href: '/alert/components/authentication'
    }, {
        id: 'certificates',
        label: 'Certificates',
        href: '/alert/components/certificates'
    }, {
        id: 'schedule',
        label: 'Scheduling',
        href: '/alert/components/scheduling'
    }, {
        id: 'task_management',
        label: 'Task Management',
        href: '/alert/components/tasks'
    }, {
        id: 'user_management',
        label: 'User Management',
        href: '/alert/components/users'
    }]

    return (
        <div className={classes.sideNavContent}>
            <ul className={classes.sideNav}>
                <SideNavItem href="/alert/general/about" icon="home" id="1" label="Home" />
                <SideNavItem href="/alert/providers/blackduck" icon="truck" id="1" label="Provider" />
                <SideNavItem hasSubMenu subMenuItems={channelGroup} label="Channels" icon="stream" />
                <SideNavItem href="/alert/jobs/distribution" icon="archive" id="1" label="Jobs" />
                <SideNavItem hasSubMenu subMenuItems={manageGroup} label="Manage" icon="toolbox" />
                <SideNavItem href="/alert/components/settings" label="Settings" icon="cog" />
            </ul>
        </div>
    );
    // return (
    //     <div className="navigation">
    //         <div className="navigationContent">
    //             <ul>
    //                 <li>
    //                     <NavLink to="/alert/general/about" activeClassName="activeNav">
    //                         <Logo />
    //                     </NavLink>
    //                 </li>
    //                 <li className="divider" />
    //                 {hasType(DESCRIPTOR_TYPE.PROVIDER)
    //                 && (
    //                     <li className="navHeader" key="providers">
    //                         Provider
    //                     </li>
    //                 )}
    //                 {doesDescriptorExist(globalDescriptorMap, BLACKDUCK_INFO.key) && createStaticNavItem(providerUri, BLACKDUCK_INFO)}
    //                 {hasType(DESCRIPTOR_TYPE.CHANNEL)
    //                 && (
    //                     <li className="navHeader" key="channels">
    //                         Channels
    //                     </li>
    //                 )}
    //                 {doesDescriptorExist(globalDescriptorMap, AZURE_BOARDS_INFO.key) && createStaticNavItem(channelUri, AZURE_BOARDS_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, EMAIL_INFO.key) && createStaticNavItem(channelUri, EMAIL_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, JIRA_CLOUD_INFO.key) && createStaticNavItem(channelUri, JIRA_CLOUD_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, JIRA_SERVER_INFO.key) && createStaticNavItem(channelUri, JIRA_SERVER_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, MSTEAMS_INFO.key) && createStaticNavItem(channelUri, MSTEAMS_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, SLACK_INFO.key) && createStaticNavItem(channelUri, SLACK_INFO)}
    //                 <li className="navHeader">
    //                     Jobs
    //                 </li>
    //                 <li>
    //                     <NavLink to={DISTRIBUTION_URLS.distributionTableUrl} activeClassName="activeNav">
    //                         {DISTRIBUTION_INFO.label}
    //                     </NavLink>
    //                 </li>
    //                 <li className="divider" />
    //                 {doesDescriptorExist(globalDescriptorMap, AUDIT_INFO.key) && createStaticNavItem(componentUri, AUDIT_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, AUTHENTICATION_INFO.key) && createStaticNavItem(componentUri, AUTHENTICATION_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, CERTIFICATE_INFO.key) && createStaticNavItem(componentUri, CERTIFICATE_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, SCHEDULING_INFO.key) && createStaticNavItem(componentUri, SCHEDULING_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, SETTINGS_INFO.key) && createStaticNavItem(componentUri, SETTINGS_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, TASK_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, TASK_MANAGEMENT_INFO)}
    //                 {doesDescriptorExist(globalDescriptorMap, USER_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, USER_MANAGEMENT_INFO)}
    //                 <li className="logoutLink">
    //                     <a
    //                         role="button"
    //                         tabIndex={0}
    //                         onClick={(evt) => {
    //                             evt.preventDefault();
    //                             confirmLogoutPressed();
    //                         }}
    //                     >
    //                         Logout
    //                     </a>
    //                 </li>
    //             </ul>
    //         </div>
    //     </div>
    // );
};

Navigation.propTypes = {
    confirmLogoutPressed: PropTypes.func.isRequired,
    globalDescriptorMap: PropTypes.object.isRequired
};

const mapDispatchToProps = (dispatch) => ({
    confirmLogoutPressed: () => dispatch(confirmLogout()),
    cancelLogout: () => dispatch(cancelLogout())
});

export default withRouter(connect(null, mapDispatchToProps)(Navigation));
