import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { NavLink, withRouter } from 'react-router-dom';
import Logo from 'component/common/Logo';
import { confirmLogout } from 'store/actions/session';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';
import { EMAIL_INFO } from 'global/channels/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'global/channels/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'global/channels/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'global/channels/msteams/MSTeamsModel';
import { AZURE_INFO } from 'global/channels/azure/AzureModel';
import { SCHEDULING_INFO } from 'global/components/scheduling/SchedulingModel';
import { SETTINGS_INFO } from 'global/components/settings/SettingsModel';
import { AUTHENTICATION_INFO } from 'global/components/auth/AuthenticationModel';
import { BLACKDUCK_INFO } from 'global/providers/blackduck/BlackDuckModel';
import { AUDIT_INFO } from 'global/components/audit/AuditModel';
import { CERTIFICATE_INFO } from 'global/components/certificates/CertificateModel';
import { TASK_MANAGEMENT_INFO } from 'global/components/task/TaskManagementModel';
import { USER_MANAGEMENT_INFO } from 'global/components/user/UserModel';

const Navigation = ({ confirmLogoutPressed, descriptorMap }) => {
    const createStaticNavItem = (uriPrefix, itemObject) => (
        <li key={itemObject.key}>
            <NavLink to={`${uriPrefix}${itemObject.url}`} activeClassName="activeNav">
                {itemObject.label}
            </NavLink>
        </li>
    );

    const channelUri = '/alert/channels/';
    const providerUri = '/alert/providers/';
    const componentUri = '/alert/components/';

    const doesDescriptorExist = (key) => Object.prototype.hasOwnProperty.call(descriptorMap, key);

    return (
        <div className="navigation">
            <div className="navigationContent">
                <ul>
                    <li>
                        <NavLink to="/alert/general/about" activeClassName="activeNav">
                            <Logo />
                        </NavLink>
                    </li>
                    <li className="divider" />
                    <li className="navHeader" key="providers">
                        Provider
                    </li>
                    {doesDescriptorExist(BLACKDUCK_INFO.key) && createStaticNavItem(providerUri, BLACKDUCK_INFO)}
                    <li className="navHeader" key="channels">
                        Channels
                    </li>
                    {doesDescriptorExist(AZURE_INFO.key) && createStaticNavItem(channelUri, AZURE_INFO)}
                    {doesDescriptorExist(EMAIL_INFO.key) && createStaticNavItem(channelUri, EMAIL_INFO)}
                    {doesDescriptorExist(JIRA_CLOUD_INFO.key) && createStaticNavItem(channelUri, JIRA_CLOUD_INFO)}
                    {doesDescriptorExist(JIRA_SERVER_INFO.key) && createStaticNavItem(channelUri, JIRA_SERVER_INFO)}
                    {doesDescriptorExist(MSTEAMS_INFO.key) && createStaticNavItem(channelUri, MSTEAMS_INFO)}
                    {doesDescriptorExist(SLACK_INFO.key) && createStaticNavItem(channelUri, SLACK_INFO)}
                    <li className="navHeader">
                        Jobs
                    </li>
                    <li>
                        <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                            Distribution
                        </NavLink>
                    </li>
                    <li className="divider" />
                    {doesDescriptorExist(AUDIT_INFO.key) && createStaticNavItem(componentUri, AUDIT_INFO)}
                    {doesDescriptorExist(AUTHENTICATION_INFO.key) && createStaticNavItem(componentUri, AUTHENTICATION_INFO)}
                    {doesDescriptorExist(CERTIFICATE_INFO.key) && createStaticNavItem(componentUri, CERTIFICATE_INFO)}
                    {doesDescriptorExist(SCHEDULING_INFO.key) && createStaticNavItem(componentUri, SCHEDULING_INFO)}
                    {doesDescriptorExist(SETTINGS_INFO.key) && createStaticNavItem(componentUri, SETTINGS_INFO)}
                    {doesDescriptorExist(TASK_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, TASK_MANAGEMENT_INFO)}
                    {doesDescriptorExist(USER_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, USER_MANAGEMENT_INFO)}
                    <li className="logoutLink">
                        <a
                            role="button"
                            tabIndex={0}
                            onClick={(evt) => {
                                evt.preventDefault();
                                confirmLogoutPressed();
                            }}
                        >
                            Logout
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    );
};

Navigation.propTypes = {
    confirmLogoutPressed: PropTypes.func.isRequired,
    descriptorMap: PropTypes.object.isRequired
};

const mapDispatchToProps = (dispatch) => ({
    confirmLogoutPressed: () => dispatch(confirmLogout())
});

export default withRouter(connect(null, mapDispatchToProps)(Navigation));
