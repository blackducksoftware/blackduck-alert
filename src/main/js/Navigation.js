import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { NavLink, withRouter } from 'react-router-dom';
import Logo from 'component/common/Logo';
import { confirmLogout } from 'store/actions/session';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class Navigation extends Component {
    constructor(props) {
        super(props);
        this.createNavItemForDescriptors = this.createNavItemForDescriptors.bind(this);
    }

    createNavItemForDescriptors(descriptorType, context, uriPrefix, header) {
        const { descriptors } = this.props;
        if (!descriptors) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, descriptorType, context);
        if (!descriptorList) {
            return null;
        }

        const contentList = descriptorList.map(component =>
            (<li key={component.name}>
                <NavLink to={`${uriPrefix}${component.urlName}`} activeClassName="activeNav">
                    {component.label}
                </NavLink>
            </li>)
        );

        if (header && contentList && contentList.length > 0) {
            contentList.unshift(<li className="navHeader" key={header}>
                {header}
            </li>);
        }
        return contentList;
    }

    render() {
        const channelGlobals = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/', 'Channels');
        const providers = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/providers/', 'Providers');
        const components = this.createNavItemForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/components/');

        const nav = (
            <Fragment>
                <li className="navHeader">
                    Providers
                </li>
                <li>
                    <NavLink to="/alert/providers" activeClassName="activeNav">
                        Black Duck
                    </NavLink>
                </li>
                {providers}
                {channelGlobals}
                <li className="navHeader">
                    Jobs
                </li>
                <li>
                    <NavLink to="/alert/jobs/distribution" activeClassName="activeNav">
                        Distribution
                    </NavLink>
                </li>
                <li className="divider" />
                {components}
            </Fragment>
        );

        const rows = (this.props.fetching) ? null : nav;

        return (
            <div className="navigation">
                <div className="navigationContent">
                    <ul>
                        <li>
                            <NavLink to="/alert/general/about" activeClassName="activeNav">
                                <Logo />
                            </NavLink>
                        </li>
                        <li className="divider" />
                        {rows}
                        <li className="logoutLink">
                            <a
                                role="button"
                                tabIndex={0}
                                onClick={(evt) => {
                                    evt.preventDefault();
                                    this.props.confirmLogout();
                                }}
                            >
                                Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </div>);
    }
}

Navigation.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    confirmLogout: PropTypes.func.isRequired,
    fetching: PropTypes.bool.isRequired
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    descriptors: state.descriptors.items,
    fetching: state.descriptors.fetching
});

const mapDispatchToProps = dispatch => ({
    confirmLogout: () => dispatch(confirmLogout())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navigation));
