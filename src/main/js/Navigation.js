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
            CHANNEL_GLOBAL_CONFIG: [],
            PROVIDER_CONFIG: []
        }
        this.retrieveComponentData = this.retrieveComponentData.bind(this);
    }

    componentDidMount() {
        this.retrieveComponentData('CHANNEL_GLOBAL_CONFIG'),
        this.retrieveComponentData('PROVIDER_CONFIG')
    }

    retrieveComponentData(distributionConfigType) {
        const getUrl = `/alert/api/descriptors/${distributionConfigType}`;
        fetch(getUrl, {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            return response.json().then((json) => {
                this.setState({
                    [distributionConfigType]: json
                });
            });
        }).catch(console.error);
    }

    render() {
        const globals = this.state.CHANNEL_GLOBAL_CONFIG
            .sort((first, second) => first.label > second.label)
            .map((component) =>
            <li>
                <NavLink to={`/alert/channels/${component.urlName}`} activeClassName="activeNav">
                    <FontAwesome name={component.fontAwesomeIcon} fixedWidth/>
                    {component.label}
                </NavLink>
            </li>);
        const providers = this.state.PROVIDER_CONFIG
            .sort((first, second) => first.label > second.label)
            .map((component) =>
            <li>
                <NavLink to={`/alert/providers/${component.urlName}`} activeClassName="activeNav">
                    <FontAwesome name={component.fontAwesomeIcon} fixedWidth/>
                    {component.label}
                </NavLink>
            </li>);

        return (
            <div className="navigation">
                <div className="navigationLogo">
                    <Logo/>
                </div>
                <div className="navigationContent">
                    <ul>
                        <li className="navHeader">
                            Providers
                        </li>
                        {providers}
                        <li className="navHeader">
                            Channels
                        </li>
                        {globals}
                        <li className="navHeader">
                            Jobs
                        </li>
                        <li>
                            <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                                <FontAwesome name="truck" fixedWidth/> Distribution
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/alert/jobs/scheduling" activeClassName="activeNav">
                                <FontAwesome name="clock-o" fixedWidth/> Scheduling
                            </NavLink>
                        </li>
                        <li className="divider"/>
                        <li>
                            <NavLink to="/alert/general/audit" activeClassName="activeNav">
                                <FontAwesome name="history" fixedWidth/> Audit
                            </NavLink>
                        </li>
                        <li>
                        <NavLink to="/alert/general/about" activeClassName="activeNav">
                            <FontAwesome name="info" fixedWidth/> About
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
                                <FontAwesome name="sign-out" fixedWidth/> Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </div>);
    }
}

Navigation.propTypes = {
    confirmLogout: PropTypes.func.isRequired
};

// TODO Add Redux to this page
const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken
});

const mapDispatchToProps = dispatch => ({
    confirmLogout: () => dispatch(confirmLogout())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
