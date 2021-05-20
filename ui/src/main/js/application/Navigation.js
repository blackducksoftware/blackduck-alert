import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { NavLink, withRouter } from 'react-router-dom';
import Logo from 'common/Logo';
import { confirmLogout } from 'store/actions/session';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';
import { EMAIL_INFO } from 'page/channel/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'page/channel/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';
import { AZURE_INFO } from 'page/channel/azure/AzureModel';
import { SCHEDULING_INFO } from 'page/scheduling/SchedulingModel';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import { AUTHENTICATION_INFO } from 'application/auth/AuthenticationModel';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { AUDIT_INFO } from 'page/audit/AuditModel';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';
import { USER_MANAGEMENT_INFO } from 'page/user/UserModel';
import { DESCRIPTOR_TYPE, doesDescriptorExist } from 'common/util/descriptorUtilities';
import { DISTRIBUTION_INFO, DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';

const Navigation = ({ confirmLogoutPressed, globalDescriptorMap }) => {
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

    const hasType = (descriptorType) => Object.values(globalDescriptorMap).some((descriptor) => descriptorType === descriptor.type);

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
                    {hasType(DESCRIPTOR_TYPE.PROVIDER)
                    && (
                        <li className="navHeader" key="providers">
                            Provider
                        </li>
                    )}
                    {doesDescriptorExist(globalDescriptorMap, BLACKDUCK_INFO.key) && createStaticNavItem(providerUri, BLACKDUCK_INFO)}
                    {hasType(DESCRIPTOR_TYPE.CHANNEL)
                    && (
                        <li className="navHeader" key="channels">
                            Channels
                        </li>
                    )}
                    {doesDescriptorExist(globalDescriptorMap, AZURE_INFO.key) && createStaticNavItem(channelUri, AZURE_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, EMAIL_INFO.key) && createStaticNavItem(channelUri, EMAIL_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, JIRA_CLOUD_INFO.key) && createStaticNavItem(channelUri, JIRA_CLOUD_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, JIRA_SERVER_INFO.key) && createStaticNavItem(channelUri, JIRA_SERVER_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, MSTEAMS_INFO.key) && createStaticNavItem(channelUri, MSTEAMS_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, SLACK_INFO.key) && createStaticNavItem(channelUri, SLACK_INFO)}
                    <li className="navHeader">
                        Jobs
                    </li>
                    <li>
                        <NavLink to={DISTRIBUTION_URLS.distributionTableUrl} activeClassName="activeNav">
                            {DISTRIBUTION_INFO.label}
                        </NavLink>
                    </li>
                    <li className="divider" />
                    {doesDescriptorExist(globalDescriptorMap, AUDIT_INFO.key) && createStaticNavItem(componentUri, AUDIT_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, AUTHENTICATION_INFO.key) && createStaticNavItem(componentUri, AUTHENTICATION_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, CERTIFICATE_INFO.key) && createStaticNavItem(componentUri, CERTIFICATE_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, SCHEDULING_INFO.key) && createStaticNavItem(componentUri, SCHEDULING_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, SETTINGS_INFO.key) && createStaticNavItem(componentUri, SETTINGS_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, TASK_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, TASK_MANAGEMENT_INFO)}
                    {doesDescriptorExist(globalDescriptorMap, USER_MANAGEMENT_INFO.key) && createStaticNavItem(componentUri, USER_MANAGEMENT_INFO)}
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
    globalDescriptorMap: PropTypes.object.isRequired
};

const mapDispatchToProps = (dispatch) => ({
    confirmLogoutPressed: () => dispatch(confirmLogout())
});

export default withRouter(connect(null, mapDispatchToProps)(Navigation));
