import React from 'react';
import { connect } from 'react-redux';
import { Route, Link, NavLink, withRouter } from 'react-router-dom';
import Audit from './component/audit/Audit';
import DistributionConfiguration from './component/distribution/DistributionConfiguration';
import ServerContent from './component/server/ServerContent';
import Navigation from './Navigation';

import styles from '../css/main.css';

const MainPage = () => (
    <div>
        <Navigation />
        <div className={styles.contentArea}>
            <Route path="/settings" render={props => (
                <ServerContent />
            )}/>
            <Route path="/distribution" render={props => (
                <DistributionConfiguration />
            )}/>
            <Route path="/audit" render={props => (
                <Audit />
            )}/>
        </div>
    </div>
);

// Redux mappings to be used later....
const mapStateToProps = state => ({});
const mapDispatchToProps = dispatch => ({});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
