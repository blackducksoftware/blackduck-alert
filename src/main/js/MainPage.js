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


class MainPage extends Component {
    constructor(props) {
        super(props);

        this.createRoutesForDescriptors = this.createRoutesForDescriptors.bind(this);
        this.createConfigurationPage = this.createConfigurationPage.bind(this);
    }

    componentDidMount() {
        this.props.getDescriptors();
    }

    createRoutesForDescriptors(descriptorType, context, uriPrefix) {
        const { items } = this.props.descriptors;
        if (!items) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(items, descriptorType, context);

        if (!descriptorList || descriptorList.length === 0) {
            return null;
        }
        const routeList = descriptorList.map(component => this.createConfigurationPage(component, uriPrefix));
        return routeList;
    }

    createConfigurationPage(component, uriPrefix) {
        const {
            urlName, name, automaticallyGenerateUI, componentNamespace
        } = component;
        if (automaticallyGenerateUI) {
            return (<Route
                key={urlName}
                path={`${uriPrefix}${urlName}`}
                render={() => <DescriptorContentLoader componentNamespace={componentNamespace} />}
            />);
        }

        return (<Route
            key={urlName}
            path={`${uriPrefix}${urlName}`}
            render={() => <GlobalConfiguration key={name} descriptor={component} />}
        />);
    }

    render() {
        const channels = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/');
        const providers = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/providers/');
        const components = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/components/');
        const spinner = (
            <div>
                Spinner
            </div>
        );
        const page = (
            <div>
                <Navigation />
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
                    <Route path="/alert/jobs/distribution" component={DistributionConfiguration} />
                    {components}
                    <Route path="/alert/general/about" component={AboutInfo} />
                </div>
                <div className="modalsArea">
                    <LogoutConfirmation />
                </div>
            </div>);

        console.log(this.props.descriptors);
        const content = (this.props.descriptors.fetching) ? spinner : page;
        return (
            <div>
                {content}
            </div>
        );
    }
}

MainPage.propTypes = {
    descriptors: PropTypes.object.isRequired,
    getDescriptors: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    getDescriptors: () => dispatch(getDescriptors())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
