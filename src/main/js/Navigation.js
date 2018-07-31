import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {NavLink, withRouter} from 'react-router-dom';
import FontAwesome from 'react-fontawesome';
import Logo from './component/common/Logo';
import {confirmLogout} from './store/actions/session';

class Navigation extends Component {
    constructor(props) {
        super(props);
        this.state = {
            channelDescriptors: '',
            providerDescriptors: ''
        };
        this.getDescriptors = this.getDescriptors.bind(this);
    }


    getDescriptors(descriptorType) {
        var urlString = '/alert/api/descriptors';
        if (descriptorType) {
            urlString = `${urlString}/${descriptorType}`;
        }
        const self = this;
        fetch(urlString, {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            if (response.ok) {
                response.json().then((jsonArray) => {
                    if (jsonArray && jsonArray.length > 0) {
                        self.initializeValues(jsonArray[0]);
                    } else {
                        self.initializeValues(self.props);
                    }
                });
            } else {
                self.initializeValues(self.props);
            }
        }).catch(console.error);
    }

    render() {
        return (
            <div className="navigation">
                <div className="navigationLogo">
                    <Logo />
                </div>
                <div className="navigationContent">
                    <ul>
                        <li className="navHeader">
                            Providers
                        </li>
                        <li>
                            <NavLink to="/alert/providers/hub" activeClassName="activeNav">
                                <FontAwesome name="laptop" fixedWidth /> Hub
                            </NavLink>
                        </li>
                        <li className="navHeader">
                            Channels
                        </li>
                        <li>
                            <NavLink to="/alert/channels/email" activeClassName="activeNav">
                                <FontAwesome name="envelope" fixedWidth /> Email
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/alert/channels/hipchat" activeClassName="activeNav">
                                <FontAwesome name="comments" fixedWidth /> HipChat
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/alert/channels/slack" activeClassName="activeNav">
                                <FontAwesome name="slack" fixedWidth /> Slack
                            </NavLink>
                        </li>
                        <li className="navHeader">
                            Jobs
                        </li>
                        <li>
                            <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                                <FontAwesome name="truck" fixedWidth /> Distribution
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/alert/jobs/scheduling" activeClassName="activeNav">
                                <FontAwesome name="clock-o" fixedWidth /> Scheduling
                            </NavLink>
                        </li>
                        <li className="divider" />
                        <li>
                            <NavLink to="/alert/general/audit" activeClassName="activeNav">
                                <FontAwesome name="history" fixedWidth /> Audit
                            </NavLink>
                        </li>
                        <li className="logoutLink">
                            <a
                                role="button"
                                tabIndex={0}
                                onClick={(evt) => {
                                    evt.preventDefault();
                                    props.confirmLogout();
                                }}
                            >
                                <FontAwesome name="sign-out" fixedWidth /> Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        );
    }
}

Navigation.propTypes = {
    confirmLogout: PropTypes.func.isRequired
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

const mapDispatchToProps = dispatch => ({
    confirmLogout: () => dispatch(confirmLogout())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
