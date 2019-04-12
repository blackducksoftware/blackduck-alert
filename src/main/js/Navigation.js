import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { NavLink, withRouter } from 'react-router-dom';
import FontAwesome from 'react-fontawesome';
import Logo from 'component/common/Logo';
import { confirmLogout } from 'store/actions/session';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class Navigation extends Component {
    constructor(props) {
        super(props);
        this.createNavItemForDescriptors = this.createNavItemForDescriptors.bind(this);
    }

    createNavItemForDescriptors(decriptorType, context, uriPrefix) {
        const { descriptors } = this.props;
        if (!descriptors) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, decriptorType, context);
        if (!descriptorList) {
            return null;
        }
        return descriptorList.map(component =>
            (<li>
                <NavLink to={`${uriPrefix}${component.urlName}`} activeClassName="activeNav">
                    <FontAwesome name={component.fontAwesomeIcon} fixedWidth /> {component.label}
                </NavLink>
            </li>));
    }

    render() {
        const channelGlobals = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/');
        const providers = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/providers/');
        const components = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/components/');

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
                        {providers}
                        <li className="navHeader">
                            Channels
                        </li>
                        {channelGlobals}
                        <li className="navHeader">
                            Jobs
                        </li>
                        <li>
                            <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                                <FontAwesome name="truck" fixedWidth /> Distribution
                            </NavLink>
                        </li>
                        <li className="divider" />
                        {components}
                        <li>
                            <NavLink to="/alert/general/audit" activeClassName="activeNav">
                                <FontAwesome name="history" fixedWidth /> Audit
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/alert/general/about" activeClassName="activeNav">
                                <FontAwesome name="info" fixedWidth /> About
                            </NavLink>
                        </li>
                        <li className="logoutLink">
                            <a
                                role="button"
                                tabIndex={0}
                                onClick={(evt) => {
                                    evt.preventDefault();
                                    this.props.confirmLogout();
                                }}
                            >
                                <FontAwesome name="sign-out" fixedWidth /> Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </div>);
    }
}

Navigation.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    confirmLogout: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    confirmLogout: () => dispatch(confirmLogout())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
