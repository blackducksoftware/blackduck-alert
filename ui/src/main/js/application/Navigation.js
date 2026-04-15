import React from 'react';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import SideNavItem from 'common/component/navigation/SideNavItem';
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

    const hasType = (descriptorType) => Object.values(globalDescriptorMap).some((descriptor) => descriptorType === descriptor.type);

    const channelGroup = [{
        id: 'azure_boards',
        label: 'Azure Boards',
        href: AZURE_BOARDS_URLS.mainUrl,
        showOption: doesDescriptorExist(globalDescriptorMap, AZURE_BOARDS_INFO.key)
    }, {
        id: 'email',
        label: 'Email',
        href: EMAIL_URLS.mainUrl,
        showOption: doesDescriptorExist(globalDescriptorMap, EMAIL_INFO.key)
    }, {
        id: 'jira_cloud',
        label: 'Jira Cloud',
        href: JIRA_CLOUD_URLS.mainUrl,
        showOption: doesDescriptorExist(globalDescriptorMap, JIRA_CLOUD_INFO.key)
    }, {
        id: 'jira_server',
        label: 'Jira Server',
        href: JIRA_SERVER_URLS.jiraServerUrl,
        showOption: doesDescriptorExist(globalDescriptorMap, JIRA_SERVER_INFO.key)
    }, {
        id: 'microsoft_teams',
        label: 'Microsoft Teams',
        href: MSTEAMS_URLS.mainUrl,
        showOption: doesDescriptorExist(globalDescriptorMap, MSTEAMS_INFO.key)
    }, {
        id: 'slack',
        label: 'Slack',
        href: SLACK_URLS.mainUrl,
        showOption: doesDescriptorExist(globalDescriptorMap, SLACK_INFO.key)
    }];

    const manageGroup = [{
        id: 'audit_failures',
        label: 'Audit Failures',
        href: '/alert/components/audit',
        showOption: doesDescriptorExist(globalDescriptorMap, AUDIT_INFO.key)
    }, {
        id: 'authentication',
        label: 'Authentication',
        href: '/alert/components/authentication',
        showOption: doesDescriptorExist(globalDescriptorMap, AUTHENTICATION_INFO.key)
    }, {
        id: 'certificates',
        label: 'Certificates',
        href: '/alert/components/certificates',
        showOption: doesDescriptorExist(globalDescriptorMap, CERTIFICATE_INFO.key)
    }, {
        id: 'schedule',
        label: 'Scheduling',
        href: '/alert/components/scheduling',
        showOption: doesDescriptorExist(globalDescriptorMap, SCHEDULING_INFO.key)
    }, {
        id: 'task_management',
        label: 'Task Management',
        href: '/alert/components/tasks',
        showOption: doesDescriptorExist(globalDescriptorMap, TASK_MANAGEMENT_INFO.key)
    }, {
        id: 'user_management',
        label: 'User Management',
        href: '/alert/components/users',
        showOption: doesDescriptorExist(globalDescriptorMap, USER_MANAGEMENT_INFO.key)
    }];

    return (
        <div className={classes.sideNavContent}>
            <ul className={classes.sideNav}>
                <SideNavItem href="/alert/general/about" icon="home" id="home" label="Home" type="link" />

                { (hasType(DESCRIPTOR_TYPE.PROVIDER) && doesDescriptorExist(globalDescriptorMap, BLACKDUCK_INFO.key)) ? (
                    <SideNavItem href="/alert/providers/blackduck" icon="truck" id="providers" label="Provider" type="link" />
                ) : null }
                
                { hasType(DESCRIPTOR_TYPE.CHANNEL) ? (
                    <SideNavItem hasSubMenu subMenuItems={channelGroup} label="Channels" id="channels" icon="stream" />
                ) : null }
                
                <SideNavItem href="/alert/jobs/distribution" icon="archive" id="archive" label="Jobs" type="link" />

                { hasType(DESCRIPTOR_TYPE.COMPONENT) ? (
                    <SideNavItem hasSubMenu subMenuItems={manageGroup} label="Manage" id="manage" icon="toolbox" />
                ) : null }
                
                { doesDescriptorExist(globalDescriptorMap, SETTINGS_INFO.key) ? (
                    <SideNavItem href="/alert/components/settings" label="Settings" id="settings" icon="cog" type="link" />
                ) :  null }
                
                <SideNavItem label="Logout" id="logout" icon="sign-out-alt" onClick={() => confirmLogoutPressed()} />
            </ul>
        </div>
    );
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
