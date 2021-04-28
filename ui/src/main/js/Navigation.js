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
import { doesDescriptorExist } from 'util/descriptorUtilities';
import { DISTRIBUTION_INFO, DISTRIBUTION_URLS } from './distribution/DistributionModel';

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
                    {doesDescriptorExist(descriptorMap, BLACKDUCK_INFO.key) && createStaticNavItem(providerUri, BLACKDUCK_INFO)}
                    <li className="navHeader" key="channels">
                        Channels
                    </li>
                    {doesDescriptorExist(descriptorMap, AZURE_INFO.key) && createStaticNavItem(channelUri, AZURE_INFO)}
                    {doesDescriptorExist(descriptorMap, EMAIL_INFO.key) && createStaticNavItem(channelUri, EMAIL_INFO)}
                    {doesDescriptorExist(descriptorMap, JIRA_CLOUD_INFO.key) && createStaticNavItem(channelUri, JIRA_CLOUD_INFO)}
                    {doesDescriptorExist(descriptorMap, JIRA_SERVER_INFO.key) && createStaticNavItem(channelUri, JIRA_SERVER_INFO)}
                    {doesDescriptorExist(descriptorMap, MSTEAMS_INFO.key) && createStaticNavItem(channelUri, MSTEAMS_INFO)}
                    {doesDescriptorExist(descriptorMap, SLACK_INFO.key) && createStaticNavItem(channelUri, SLACK_INFO)}
                    <li className="navHeader">
                        Jobs
                    </li>
                    <li>
                        <NavLink to={DISTRIBUTION_URLS.distributionTableUrl} activeClassName="activeNav">
                            {DISTRIBUTION_INFO.label}
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                            Distribution
                        </NavLink>
                    </li>
                    <li className="divider" />
                    {doesDescriptorExist(descriptorMap, AUDIT_INFO.key) && createStaticNavItem(componentUri, AUDIT_INFO)}
                    {doesDescriptorExist(descriptorMap, AUTHENTICATION_INFO.key) && createStaticNavItem(componentUri, AUTHENTICATION_INFO)}
                    {doesDescriptorExist(descriptorMap, CERTIFICATE_INFO.key) && createStaticNavItem(componentUri, CERTIFICATE_INFO)}
                    {doesDescriptorExist(descriptorMap, SCHEDULING_INFO.key) && createStaticNavItem(componentUri, SCHEDULING_INFO)}
                    {doesDescriptorExist(descriptorMap, SETTINGS_INFO.key) && createStaticNavItem(componentUri, SETTINGS_INFO)}
                    {doesDescriptorExist(descriptorMap, TASK_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, TASK_MANAGEMENT_INFO)}
                    {doesDescriptorExist(descriptorMap, USER_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, USER_MANAGEMENT_INFO)}
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
