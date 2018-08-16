import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Redirect, Route, withRouter} from 'react-router-dom';

import {getDescriptorByType} from './store/actions/descriptors';
import Navigation from './Navigation';
import Audit from './component/general/audit/Index';
import AboutInfo from './component/general/AboutInfo';
import DistributionConfiguration from './component/general/distribution/Index';
import SchedulingConfiguration from './component/general/SchedulingConfiguration';
import SlackConfiguration from './component/channels/SlackConfiguration';
import EmailConfiguration from './component/channels/EmailConfiguration';
import HipChatConfiguration from './component/channels/HipChatConfiguration';
import LogoutConfirmation from './component/common/LogoutConfirmation';
import BlackDuckConfiguration from "./component/providers/BlackDuckConfiguration";


class MainPage extends Component {
    constructor(props) {
        super(props);
        this.createRoutesForDescriptors = this.createRoutesForDescriptors.bind(this);
    }

    componentDidMount() {
        this.props.getDescriptorByType('CHANNEL_GLOBAL_CONFIG');
        this.props.getDescriptorByType('PROVIDER_CONFIG');
    }

    createRoutesForDescriptors(decriptorTypeKey, uriPrefix) {
        const {descriptors} = this.props;
        if(!descriptors.items) {
            return null;
        } else {
            const descriptorList = descriptors.items[decriptorTypeKey];
            if(!descriptorList) {
                return null;
            } else {
                const routeList = descriptorList.map((component) => {
                        if(component.urlName === 'blackduck') {
                            return <Route path={`${uriPrefix}${component.urlName}`} component={BlackDuckConfiguration}/>
                        } else if(component.urlName === 'email') {
                            return <Route path={`${uriPrefix}${component.urlName}`} component={EmailConfiguration}/>
                        } else if (component.urlName === 'hipchat') {
                            return <Route path={`${uriPrefix}${component.urlName}`} component={HipChatConfiguration}/>
                        } else if (component.urlName === 'slack') {
                            return <Route path={`${uriPrefix}${component.urlName}`} component={SlackConfiguration}/>
                        } else {
                            return null;
                        }
                });

                routeList.unshift(
                    <Route
                        exact
                        path="/alert/"
                        render={() => (
                            <Redirect to={`${uriPrefix}${descriptorList[0].urlName}`}/>
                        )} />
                );
                return routeList;
            }
        }
    }

    render() {
        const channels = this.createRoutesForDescriptors('CHANNEL_GLOBAL_CONFIG','/alert/channels/');
        const providers = this.createRoutesForDescriptors('PROVIDER_CONFIG','/alert/providers/');
        return (
            <div>
            <Navigation/>
            <div className="contentArea">
                {providers}
                {channels}
                <Route path="/alert/jobs/scheduling" component={SchedulingConfiguration}/>
                <Route path="/alert/jobs/distribution" component={DistributionConfiguration}/>
                <Route path="/alert/general/audit" component={Audit}/>
                <Route path="/alert/general/about" component={AboutInfo}/>
            </div>
            <div className="modalsArea">
                <LogoutConfirmation/>
            </div>
        </div>);
    }
}

MainPage.propTypes = {
    getDescriptorByType: PropTypes.func.isRequired
};
const mapStateToProps = state => ({
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    getDescriptorByType: (descriptorType) => dispatch(getDescriptorByType(descriptorType))
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
