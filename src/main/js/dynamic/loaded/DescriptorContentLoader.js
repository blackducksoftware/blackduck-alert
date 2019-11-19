import React, { Component } from 'react';
import PropTypes from 'prop-types';
import AuditPage from 'dynamic/loaded/audit/AuditPage';
import UserManagement from 'dynamic/loaded/users/UserManagement';

const ComponentRegistry = {
    'audit.AuditPage': AuditPage,
    'users.UserManagement': UserManagement
};

export class DescriptorContentLoader extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const Component = ComponentRegistry[this.props.componentNamespace];
        return (
            <Component />
        );
    }
}

DescriptorContentLoader.propTypes = {
    componentNamespace: PropTypes.string.isRequired
};

export default DescriptorContentLoader;
