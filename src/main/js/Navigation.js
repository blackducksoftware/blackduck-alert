import React from 'react';
import {connect} from "react-redux";
import {Link, NavLink, Route, withRouter} from "react-router-dom";
import FontAwesome from 'react-fontawesome';
import Logo from './component/common/Logo';

// TEMPORARY: This code belongs in Redux Action
const logout = (evt) => {
    evt.stopPropagation();
    evt.preventDefault();

    fetch('/api/logout', {
        method: 'POST',
        credentials: 'include',
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
    <div className="navigation">
        <div className="navigationLogo">
            <Logo />
        </div>
        <ul>
            <li className="navHeader">
                Providers
            </li>
            <li>
                <NavLink to="/providers/hub" activeClassName="activeNav">
                    <FontAwesome name="laptop" fixedWidth={true} /> Hub
                </NavLink>
            </li>
            <li className="navHeader">
                Channels
            </li>
            <li>
                <NavLink to="/channels/email" activeClassName="activeNav">
                    <FontAwesome name="envelope" fixedWidth={true} /> Email
                </NavLink>
            </li>
            <li>
                <NavLink to="/channels/hipchat" activeClassName="activeNav">
                    <FontAwesome name="comments" fixedWidth={true} /> HipChat
                </NavLink>
            </li>
            <li>
                <NavLink to="/channels/slack" activeClassName="activeNav">
                    <FontAwesome name="slack" fixedWidth={true} /> Slack
                </NavLink>
            </li>
            <li className="navHeader">
                General
            </li>
            <li>
                <NavLink to="/general/scheduling" activeClassName="activeNav">
                    <FontAwesome name="clock-o" fixedWidth={true} /> Scheduling
                </NavLink>
            </li>
            <li>
                <NavLink to="/general/distribution" activeClassName="activeNav">
                    <FontAwesome name="truck" fixedWidth={true} /> Distribution
                </NavLink>
            </li>
            <li>
                <NavLink to="/general/audit" activeClassName="activeNav">
                    <FontAwesome name="history" fixedWidth={true} /> Audit
                </NavLink>
            </li>

            <li className="logoutLink">
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
