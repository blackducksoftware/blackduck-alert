import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Redirect, Route, withRouter } from 'react-router-dom';
import Navigation from 'Navigation';
import AboutInfo from 'component/AboutInfo';
import DistributionConfiguration from 'distribution/Index';
import LogoutConfirmation from 'component/common/LogoutConfirmation';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import GlobalConfiguration from 'dynamic/GlobalConfiguration';
import { getDescriptors } from 'store/actions/descriptors';
import DescriptorContentLoader from 'dynamic/loaded/DescriptorContentLoader';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SlackGlobalConfiguration from 'global/channels/slack/SlackGlobalConfiguration';
import EmailGlobalConfiguration from 'global/channels/email/EmailGlobalConfiguration';
import JiraCloudGlobalConfiguration from 'global/channels/jira/cloud/JiraCloudGlobalConfiguration';
import JiraServerGlobalConfiguration from 'global/channels/jira/server/JiraServerGlobalConfiguration';
import { JIRA_CLOUD_INFO } from 'global/channels/jira/cloud/JiraCloudModel';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';
import { EMAIL_INFO } from 'global/channels/email/EmailModels';
import { JIRA_SERVER_INFO } from 'global/channels/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'global/channels/msteams/MSTeamsModel';
import MSTeamsGlobalConfiguration from 'global/channels/msteams/MSTeamsGlobalConfiguration';
import { AZURE_INFO } from 'global/channels/azure/AzureModel';
import AzureGlobalConfiguration from 'global/channels/azure/AzureGlobalConfiguration';
import { SCHEDULING_INFO } from 'global/components/scheduling/SchedulingModel';
import SchedulingConfiguration from 'global/components/scheduling/SchedulingConfiguration';
import { SETTINGS_INFO } from 'global/components/settings/SettingsModel';
import SettingsConfiguration from 'global/components/settings/SettingsConfiguration';
import { AUTHENTICATION_INFO } from 'global/components/auth/AuthenticationModel';
import AuthenticationConfiguration from 'global/components/auth/AuthenticationConfiguration';
import { BLACKDUCK_INFO, BLACKDUCK_URLS } from 'global/providers/blackduck/BlackDuckModel';
import BlackDuckProviderConfiguration from 'global/providers/blackduck/BlackDuckProviderConfiguration';
import BlackDuckConfiguration from 'global/providers/blackduck/BlackDuckConfiguration';
import { DISTRIBUTION_INFO, DISTRIBUTION_URLS } from 'distribution/DistributionModel';
import DistributionConfigurationV2 from 'distribution/DistributionConfigurationV2';
import DistributionConfigurationForm from 'distribution/DistributionConfigurationForm';

class MainPage extends Component {
    constructor(props) {
        super(props);

        this.createRoutesForDescriptors = this.createRoutesForDescriptors.bind(this);
        this.createRoute = this.createRoute.bind(this);
        this.createConfigurationPage = this.createConfigurationPage.bind(this);
    }

    componentDidMount() {
        this.props.getDescriptors();
    }

    createRoutesForDescriptors(descriptorType, context, uriPrefix) {
        const { descriptors } = this.props;
        if (!descriptors) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, descriptorType, context);

        if (!descriptorList || descriptorList.length === 0) {
            return null;
        }
        const routeList = descriptorList.map((component) => this.createConfigurationPage(component, uriPrefix));
        return routeList;
    }

    createConfigurationPage(component, uriPrefix) {
        const {
            urlName, name, automaticallyGenerateUI, componentNamespace, label, description
        } = component;
        if (!automaticallyGenerateUI) {
            return (
                <Route
                    exact
                    key={urlName}
                    path={`${uriPrefix}${urlName}`}
                    render={() => <DescriptorContentLoader componentNamespace={componentNamespace} label={label} description={description} />}
                />
            );
        }

        const { csrfToken } = this.props;

        // This is needed until all dynamic code is gone where we will then statically add all these components
        let renderComponent;
        switch (name) {
            case SLACK_INFO.key:
                renderComponent = <SlackGlobalConfiguration />;
                break;
            case MSTEAMS_INFO.key:
                renderComponent = <MSTeamsGlobalConfiguration />;
                break;
            case EMAIL_INFO.key:
                renderComponent = <EmailGlobalConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            case JIRA_CLOUD_INFO.key:
                renderComponent = <JiraCloudGlobalConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            case JIRA_SERVER_INFO.key:
                renderComponent = <JiraServerGlobalConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            case AZURE_INFO.key:
                renderComponent = <AzureGlobalConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            case SCHEDULING_INFO.key:
                renderComponent = <SchedulingConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            case SETTINGS_INFO.key:
                renderComponent = <SettingsConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            case AUTHENTICATION_INFO.key:
                renderComponent = <AuthenticationConfiguration csrfToken={csrfToken} readonly={component.readonly} />;
                break;
            default:
                renderComponent = <GlobalConfiguration key={name} descriptor={component} />;
        }

        return (
            <Route
                exact
                key={urlName}
                path={`${uriPrefix}${urlName}`}
                render={() => renderComponent}
            />
        );
    }

    createRoute(uriPrefix, urlName, component) {
        return (
            <Route
                exact
                key={urlName}
                path={`${uriPrefix}${urlName}`}
                render={() => component}
            />
        );
    }

    render() {
        const channels = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/');
        const components = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/components/');

        const spinner = (
            <div>
                <div className="loginContainer">
                    <div className="progressIcon">
                        <FontAwesomeIcon icon="spinner" className="alert-icon" size="5x" spin />
                    </div>
                </div>
            </div>
        );

        const { csrfToken, descriptors, autoRefresh } = this.props;
        const page = (
            <div className="contentArea">
                <Route
                    exact
                    key="blackduck-route"
                    path={`${BLACKDUCK_URLS.blackDuckConfigUrl}/:id?`}
                    render={() => <BlackDuckConfiguration csrfToken={csrfToken} readonly={false} />}
                />
                <Route
                    exact
                    key="distribution-route"
                    path={`${DISTRIBUTION_URLS.distributionConfigUrl}/:id?/:copy?`}
                    render={() => <DistributionConfigurationForm csrfToken={csrfToken} readonly={false} />}
                />
                <Route
                    exact
                    path="/alert/"
                    render={() => (
                        <Redirect to="/alert/general/about" />
                    )}
                />
                {this.createRoute('/alert/providers/', BLACKDUCK_INFO.url, <BlackDuckProviderConfiguration csrfToken={csrfToken} />)}
                {channels}
                {this.createRoute('/alert/jobs/', DISTRIBUTION_INFO.url, <DistributionConfigurationV2 csrfToken={csrfToken} descriptors={descriptors} showRefreshButton={!autoRefresh} />)}
                <Route exact path="/alert/jobs/distribution" component={DistributionConfiguration} />
                {components}
                <Route exact path="/alert/general/about" component={AboutInfo} />
            </div>
        );

        const content = (this.props.fetching) ? spinner : page;

        return (
            <div>
                <Navigation />
                {content}
                <div className="modalsArea">
                    <LogoutConfirmation />
                </div>
            </div>
        );
    }
}

MainPage.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    fetching: PropTypes.bool.isRequired,
    getDescriptors: PropTypes.func.isRequired,
    csrfToken: PropTypes.string.isRequired,
    autoRefresh: PropTypes.bool.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items,
    fetching: state.descriptors.fetching,
    csrfToken: state.session.csrfToken,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    getDescriptors: () => dispatch(getDescriptors())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
