import React from 'react';
import {connect} from "react-redux";
import {Link, NavLink, Route, withRouter} from "react-router-dom";
import FontAwesome from 'react-fontawesome';
import Logo from './component/common/Logo';

import { activeNav as activeNavItem, navigationLogo, navigation as navStyle } from '../css/main.css';

// TEMPORARY: This code belongs in Redux Action
const logout = (evt) => {
    evt.stopPropagation();
    evt.preventDefault();

    fetch('/api/logout', {
        method: 'POST',
        credentials: "same-origin",
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(function(response) {
        if (response.ok) {
            window.location.reload();
        }
    }).catch(function(error) {
        console.log(error);
    });
};

const Navigation = () => (
    <div className={navStyle}>
        <div className={navigationLogo}>
            <Logo />
        </div>
        <ul>
            <li>
                <Link to="/settings/hub">
                    Server Configuration
                </Link>
                <Route path="/settings" exact={false} render={props => (
                    <ul>
                        <li>
                            <NavLink to="/settings/hub" activeClassName={activeNavItem}>
                                <FontAwesome name="laptop" fixedWidth={true} /> Hub
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/settings/scheduling" activeClassName={activeNavItem}>
                                <FontAwesome name="clock-o" fixedWidth={true} /> Scheduling
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/settings/email" activeClassName={activeNavItem}>
                                <FontAwesome name="envelope" fixedWidth={true} /> Email
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/settings/hipchat" activeClassName={activeNavItem}>
                                <FontAwesome name="comments" fixedWidth={true} /> HipChat
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/settings/slack" activeClassName={activeNavItem}>
                                <FontAwesome name="slack" fixedWidth={true} /> Slack
                            </NavLink>
                        </li>
                    </ul>
                )}/>
            </li>
            <li><NavLink to="/distribution" activeClassName={activeNavItem}>Distribution Configuration</NavLink></li>
            <li><NavLink to="/audit" activeClassName={activeNavItem}>Audit</NavLink></li>
            <li><a href="#" onClick={logout}>Logout</a></li>
        </ul>
    </div>
)

// Redux mappings to be used later....
const mapStateToProps = state => ({});
const mapDispatchToProps = dispatch => ({});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
