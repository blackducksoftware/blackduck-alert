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
import ProviderTable from 'providers/ProviderTable';
import SlackGlobalConfiguration from 'global/channels/slack/SlackGlobalConfiguration';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';

class MainPage extends Component {
    constructor(props) {
        super(props);

        this.createRoutesForDescriptors = this.createRoutesForDescriptors.bind(this);
        this.createRoutesForProviders = this.createRoutesForProviders.bind(this);
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

    createRoutesForProviders() {
        const { descriptors } = this.props;
        if (!descriptors) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);

        if (!descriptorList || descriptorList.length === 0) {
            return null;
        }

        const routesList = descriptorList.map((descriptor) => {
            const { urlName, name } = descriptor;
            return (
                <Route
                    exact
                    path={`/alert/providers/${urlName}`}
                    render={() => <ProviderTable descriptorName={name} />}
                />
            );
        });
        return routesList;
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

        const renderComponent = (name === SLACK_INFO.key) ? <SlackGlobalConfiguration /> : <GlobalConfiguration key={name} descriptor={component} />;

        return (
            <Route
                exact
                key={urlName}
                path={`${uriPrefix}${urlName}`}
                render={() => renderComponent}
            />
        );
    }

    render() {
        const channels = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/');
        const providers = this.createRoutesForProviders();
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

        const page = (
            <div className="contentArea">
                <Route
                    exact
                    path="/alert/"
                    render={() => (
                        <Redirect to="/alert/general/about" />
                    )}
                />
                {providers}
                {channels}
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
    getDescriptors: PropTypes.func.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items,
    fetching: state.descriptors.fetching
});

const mapDispatchToProps = (dispatch) => ({
    getDescriptors: () => dispatch(getDescriptors())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
