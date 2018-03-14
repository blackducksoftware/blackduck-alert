import React from 'react';
import { connect } from 'react-redux';
import { Redirect, Route, Link, NavLink, withRouter } from 'react-router-dom';

import Navigation from './Navigation';
import Audit from './component/general/audit/Index';
import DistributionConfiguration from './component/general/distribution/Index';
import SchedulingConfiguration from './component/general/SchedulingConfiguration';
import EmailConfiguration from './component/channels/EmailConfiguration';
import SlackConfiguration from './component/channels/SlackConfiguration';
import HubConfiguration from './component/providers/HubConfiguration';
import HipChatConfiguration from './component/channels/HipChatConfiguration';
import LogoutConfirmation from './component/common/LogoutConfirmation';

const MainPage = () => (
    <div>
        <Navigation />
        <div className="contentArea">
            <Route
                exact
                path="/"
                render={() => (
                    <Redirect to="/providers/hub" />
                )}
            />
            <Route path="/providers/hub" component={HubConfiguration} />
            <Route path="/channels/email" component={EmailConfiguration} />
            <Route path="/channels/hipchat" component={HipChatConfiguration} />
            <Route path="/channels/slack" component={SlackConfiguration} />
            <Route path="/jobs/scheduling" component={SchedulingConfiguration} />
            <Route path="/jobs/distribution" component={DistributionConfiguration} />
            <Route path="/general/audit" component={Audit} />
        </div>
        <div className="modalsArea">
            <LogoutConfirmation />
        </div>
    </div>
);

// Redux mappings to be used later....
const mapStateToProps = state => ({});
const mapDispatchToProps = dispatch => ({});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
