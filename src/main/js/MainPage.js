import React from 'react';
import {connect} from 'react-redux';
import {Redirect, Route, withRouter} from 'react-router-dom';

import Navigation from './Navigation';
import Audit from './component/general/audit/Index';
import DistributionConfiguration from './component/general/distribution/Index';
import SchedulingConfiguration from './component/general/SchedulingConfiguration';
import SlackConfiguration from './component/channels/SlackConfiguration';
import EmailConfiguration from './component/channels/EmailConfiguration';
import HipChatConfiguration from './component/channels/HipChatConfiguration';
import LogoutConfirmation from './component/common/LogoutConfirmation';
import BlackDuckConfiguration from "./component/providers/BlackDuckConfiguration";


const MainPage = () => (
    <div>
        <Navigation/>
        <div className="contentArea">
            <Route
                exact
                path="/alert/"
                render={() => (
                    <Redirect to="/alert/providers/blackduck"/>
                )}
            />
            <Route path="/alert/providers/blackduck" component={BlackDuckConfiguration}/>
            <Route path="/alert/channels/email" component={EmailConfiguration}/>
            <Route path="/alert/channels/hipchat" component={HipChatConfiguration}/>
            <Route path="/alert/channels/slack" component={SlackConfiguration}/>
            <Route path="/alert/jobs/scheduling" component={SchedulingConfiguration}/>
            <Route path="/alert/jobs/distribution" component={DistributionConfiguration}/>
            <Route path="/alert/general/audit" component={Audit}/>
        </div>
        <div className="modalsArea">
            <LogoutConfirmation/>
        </div>
    </div>
);

// Redux mappings to be used later....
const mapStateToProps = state => ({});
const mapDispatchToProps = dispatch => ({});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
