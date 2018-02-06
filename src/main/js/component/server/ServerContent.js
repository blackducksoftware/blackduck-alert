'use strict';

import React, { Component } from 'react';
import { connect } from "react-redux";
import { Route, withRouter } from 'react-router-dom';

import HubConfiguration from './HubConfiguration';
import SchedulingConfiguration from './SchedulingConfiguration';
import EmailConfiguration from './EmailConfiguration';
import HipChatConfiguration from './HipChatConfiguration';
import SlackConfiguration from './SlackConfiguration';

class ServerContent extends Component {
	render() {
		return (
		    <div>
                <Route path="/settings/hub" component={HubConfiguration} />
                <Route path="/settings/scheduling" component={SchedulingConfiguration} />
                <Route path="/settings/email" component={EmailConfiguration} />
                <Route path="/settings/hipchat" component={HipChatConfiguration} />
                <Route path="/settings/slack" component={SlackConfiguration} />
            </div>
		)
	}
}

// Redux Bits
const mapStateToProps = state => ({});
const mapDispatchToProps = dispatch => ({});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ServerContent));
