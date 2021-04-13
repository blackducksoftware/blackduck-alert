import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { NavLink, withRouter } from 'react-router-dom';
import Logo from 'component/common/Logo';
import { confirmLogout } from 'store/actions/session';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';
import { EMAIL_INFO } from 'global/channels/email/EmailModels';
import { JIRA_CLOUD_INFO } from 'global/channels/jira/cloud/JiraCloudModel';
import { JIRA_SERVER_INFO } from 'global/channels/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'global/channels/msteams/MSTeamsModel';
import { AZURE_INFO } from 'global/channels/azure/AzureModel';
import { SCHEDULING_INFO } from 'global/components/scheduling/SchedulingModel';
import { SETTINGS_INFO } from 'global/components/settings/SettingsModel';
import { AUTHENTICATION_INFO } from 'global/components/auth/AuthenticationModel';

class Navigation extends Component {
    constructor(props) {
        super(props);
        this.createNavItemForDescriptors = this.createNavItemForDescriptors.bind(this);
    }

    createNavItemForDescriptors(descriptorType, context, uriPrefix, header) {
        const { descriptors } = this.props;
        if (!descriptors) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, descriptorType, context);
        if (!descriptorList) {
            return null;
        }

        const createStaticNavItem = (itemObject) => (
            <li key={itemObject.key}>
                <NavLink to={`${uriPrefix}${itemObject.url}`} activeClassName="activeNav">
                    {itemObject.label}
                </NavLink>
            </li>
        );

        const contentList = descriptorList.map(({ name, urlName, label }) => {
            // Removes these channels from the dynamic setup and manually inserts the static information
            switch (name) {
                case SLACK_INFO.key:
                    return createStaticNavItem(SLACK_INFO);
                case MSTEAMS_INFO.key:
                    return createStaticNavItem(MSTEAMS_INFO);
                case EMAIL_INFO.key:
                    return createStaticNavItem(EMAIL_INFO);
                case JIRA_CLOUD_INFO.key:
                    return createStaticNavItem(JIRA_CLOUD_INFO);
                case JIRA_SERVER_INFO.key:
                    return createStaticNavItem(JIRA_SERVER_INFO);
                case AZURE_INFO.key:
                    return createStaticNavItem(AZURE_INFO);
                case SCHEDULING_INFO.key:
                    return createStaticNavItem(SCHEDULING_INFO);
                case SETTINGS_INFO.key:
                    return createStaticNavItem(SETTINGS_INFO);
                case AUTHENTICATION_INFO.key:
                    return createStaticNavItem(AUTHENTICATION_INFO);
                default:
                    return (
                        <li key={name}>
                            <NavLink to={`${uriPrefix}${urlName}`} activeClassName="activeNav">
                                {label}
                            </NavLink>
                        </li>
                    );
            }
        });

        if (header && contentList && contentList.length > 0) {
            contentList.unshift(<li className="navHeader" key={header}>
                {header}
            </li>);
        }
        return contentList;
    }

    render() {
        const channelGlobals = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/', 'Channels');
        const providers = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/providers/', 'Providers');
        const components = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/components/');

        const nav = (
            <>
                {providers}
                {channelGlobals}
                <li className="navHeader">
                    Jobs
                </li>
                <li>
                    <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                        Distribution
                    </NavLink>
                </li>
                <li className="divider" />
                {components}
            </>
        );

        const rows = (this.props.fetching) ? null : nav;

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
                        {rows}
                        <li className="logoutLink">
                            <a
                                role="button"
                                tabIndex={0}
                                onClick={(evt) => {
                                    evt.preventDefault();
                                    this.props.confirmLogout();
                                }}
                            >
                                Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        );
    }
}

Navigation.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    confirmLogout: PropTypes.func.isRequired,
    fetching: PropTypes.bool.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items,
    fetching: state.descriptors.fetching
});

const mapDispatchToProps = (dispatch) => ({
    confirmLogout: () => dispatch(confirmLogout())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
