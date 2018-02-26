import React from 'react';
import {connect} from "react-redux";
import {Link, NavLink, Route, withRouter} from "react-router-dom";
import FontAwesome from 'react-fontawesome';
import Logo from './component/common/Logo';

import { activeNav as activeNavItem, navigationLogo, navigation as navStyle, navHeader, logoutLink } from '../css/main.css';

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
            <li className={navHeader}>
                Providers
            </li>
            <li>
                <NavLink to="/providers/hub" activeClassName={activeNavItem}>
                    <FontAwesome name="laptop" fixedWidth={true} /> Hub
                </NavLink>
            </li>
            <li className={navHeader}>
                Channels
            </li>
            <li>
                <NavLink to="/channels/email" activeClassName={activeNavItem}>
                    <FontAwesome name="envelope" fixedWidth={true} /> Email
                </NavLink>
            </li>
            <li>
                <NavLink to="/channels/hipchat" activeClassName={activeNavItem}>
                    <FontAwesome name="comments" fixedWidth={true} /> HipChat
                </NavLink>
            </li>
            <li>
                <NavLink to="/channels/slack" activeClassName={activeNavItem}>
                    <FontAwesome name="slack" fixedWidth={true} /> Slack
                </NavLink>
            </li>
            <li className={navHeader}>
                General
            </li>
            <li>
                <NavLink to="/general/scheduling" activeClassName={activeNavItem}>
                    <FontAwesome name="clock-o" fixedWidth={true} /> Scheduling
                </NavLink>
            </li>
            <li>
                <NavLink to="/general/distribution" activeClassName={activeNavItem}>
                    <FontAwesome name="truck" fixedWidth={true} /> Distribution
                </NavLink>
            </li>
            <li>
                <NavLink to="/general/audit" activeClassName={activeNavItem}>
                    <FontAwesome name="history" fixedWidth={true} /> Audit
                </NavLink>
            </li>

            <li className={logoutLink}>
                <a href="#" onClick={logout}>
                    <FontAwesome name="sign-out" fixedWidth={true} /> Logout
                </a>
            </li>
        </ul>
    </div>
)

// Redux mappings to be used later....
const mapStateToProps = state => ({});
const mapDispatchToProps = dispatch => ({});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
