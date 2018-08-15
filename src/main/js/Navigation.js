import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {NavLink, withRouter} from 'react-router-dom';
import FontAwesome from 'react-fontawesome';
import Logo from './component/common/Logo';
import {confirmLogout} from './store/actions/session';
import {getDescriptorByType} from './store/actions/descriptors';

class Navigation extends Component {
    constructor(props) {
        super(props);
        this.createNavItemForDescriptors = this.createNavItemForDescriptors.bind(this);
    }

    componentDidMount() {
        this.props.getDescriptorByType('CHANNEL_GLOBAL_CONFIG'),
        this.props.getDescriptorByType('PROVIDER_CONFIG')
    }

    createNavItemForDescriptors(decriptorTypeKey, uriPrefix) {
        const {descriptors} = this.props;
        if(!descriptors.items) {
            return null;
        } else {
            const descriptorList = descriptors.items[decriptorTypeKey];
            if(!descriptorList) {
                return null;
            } else {
                return descriptorList.sort((first, second) => first.label > second.label)
                .map((component) =>
                    <li>
                        <NavLink to={`${uriPrefix}${component.urlName}`} activeClassName="activeNav">
                            <FontAwesome name={component.fontAwesomeIcon} fixedWidth/>
                        {component.label}
                        </NavLink>
                    </li>);
            }
        }
    }

    render() {
        const globals = this.createNavItemForDescriptors('CHANNEL_GLOBAL_CONFIG','/alert/channels/');
        const providers = this.createNavItemForDescriptors('PROVIDER_CONFIG','/alert/providers/');

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
    confirmLogout: PropTypes.func.isRequired,
    getDescriptorByType: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    confirmLogout: () => dispatch(confirmLogout()),
    getDescriptorByType: (descriptorType) => dispatch(getDescriptorByType(descriptorType))
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
